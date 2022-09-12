package mandioca.bitcoin.network.node;

import mandioca.ioc.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import static mandioca.bitcoin.network.NetworkConstants.MAX_MESSAGE_SIZE;
import static mandioca.bitcoin.network.NetworkProperties.HANDSHAKE_TIME_TO_LIVE;
import static mandioca.bitcoin.network.NetworkProperties.SUB_REACTOR_SELECT_TIMEOUT;
import static mandioca.bitcoin.network.node.HandshakeResponses.isHandshakeResponse;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.channelInfo;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.channelToCacheKey;
import static mandioca.bitcoin.util.HexUtils.HEX;


/**
 * The SubReactor (handler) dispatches processing to POOL_SIZE io-worker threads.
 */
@SuppressWarnings("unused")
public class SubReactor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(SubReactor.class);

    // don't borrow pooled buffers in this long running thread
    private final ByteBuffer inputBuffer = ByteBuffer.allocate(1024);
    private final ByteBuffer outputBuffer = ByteBuffer.allocate(1024);

    private final ExecutorService ioWorkerPool = NodeExecutorServices.createIOWorkerPool(true); // TODO property?

    @Inject
    private String nodeName;   // this node's name, not the peer's name (for logging, debugging)
    @Inject
    private int port;
    @Inject
    private Selectors selectors;
    @Inject
    private SocketChannelQueues socketChannelQueues;
    @Inject
    private HandshakeCache handshakeCache;

    private int index;

    public SubReactor() {
    }

    SubReactor setIndex(int index) {
        this.index = index;
        return this;
    }

    @Override
    public void run() {
        try {
            final Selector selector = Selector.open();
            log.debug("{} add sub reactor {}", nodeName, selector);
            selectors.add(index, selector);
            runDispatchLoop(selector);
            ioWorkerPool.shutdown();
            if (selector.isOpen()) {
                selector.close();
            }
        } catch (IOException e) {
            log.error("{} dispatch loop failure", nodeName, e);
        }
    }

    private void runDispatchLoop(Selector selector) throws IOException {
        // Each child thread's IO completion msg sending queue avoids concurrent ops problems.
        Queue<SelectionKey> queue = new LinkedBlockingQueue<>();
        // Each child thread's channelSocket queue evenly distributes the number of requests.
        Queue<SocketChannel> socketChannels = socketChannelQueues.get(index);
        int counter = 0; // SubReactor loop counter
        while (!Thread.interrupted()) {
            counter++;
            // Blocks until 1 channel is selected, selector.wakeup invoked, thread interrupt, or time out; whichever's 1st.
            int select = selector.select(SUB_REACTOR_SELECT_TIMEOUT);
            if (select != 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey sk : selectionKeys) {
                    dispatch(sk, queue);
                }
                selectionKeys.clear();
            }
            if (log.isDebugEnabled() && (counter & 0x7F) == 0) {  // 0x7F = 2's complement 127
                log.debug("{} SUB_REACTOR_LOOPS select counter {} and select {}", nodeName, counter, select);
            }
            // TODO check ban list here?
            registerChannelsForRead(socketChannels, selector);
        }
        if (log.isDebugEnabled()) {
            log.debug("{} interrupted", nodeName);
        }
    }

    private void registerChannelsForRead(Queue<SocketChannel> socketChannels, Selector selector) {
        SocketChannel socketChannel = socketChannels.poll(); // retrieves & removes head of queue
        do {
            if (Objects.nonNull(socketChannel)) {
                try {
                    // register new socketChannel to this selector
                    log.debug("{} registering socketChannel to selector : {}", nodeName, selector);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
        } while (Objects.nonNull(socketChannel = socketChannels.poll()));
    }


    private void dispatch(SelectionKey selectionKey, final Queue<SelectionKey> queue) {
        SelectableChannel selectableChannel = selectionKey.channel();
        if (selectionKey.isReadable()) {
            SocketChannel channel = (SocketChannel) selectableChannel;
            if (!channel.isConnected()) {
                log.debug("{} channel is closed {}", nodeName, channel);
                return;
            }
            try {
                spawnProcessor(selectionKey, queue, channel);
            } catch (IOException e) {
                try {
                    log.error("{} closing channel for selectionKey {}", nodeName, selectionKey, e);
                    selectionKey.channel().close();
                } catch (IOException ex) {
                    log.error("{} selectionKey channel close error {}", nodeName, selectionKey, ex);
                }
            }
        } else if (selectionKey.isWritable()) {
            send(selectionKey, queue);
        }
    }

    private void spawnProcessor(SelectionKey selectionKey, final Queue<SelectionKey> queue, SocketChannel channel) throws IOException {
        inputBuffer.clear();
        int read = channel.read(inputBuffer);
        if (read == -1) {
            if (log.isDebugEnabled()) {
                log.debug("{} reached end-of-stream, closing channel {}", nodeName, channel);
            }
            channel.close();
        } else if (read > 0 && read <= MAX_MESSAGE_SIZE) {
            byte[] request = getInputPayload();
            if (log.isTraceEnabled()) {
                log.trace("{} processing req '{}'", request);
            }
            ioWorkerPool.submit(new Processor(nodeName, request, selectionKey, queue));
        } else if (read > MAX_MESSAGE_SIZE) {
            log.warn("{} ignoring too many bytes {} for channel {}", nodeName, read, channel);
        } else {
            log.warn("{} read 0 bytes for channel {}", nodeName, channel);
        }
    }

    private byte[] getInputPayload() {
        inputBuffer.flip();
        int remaining = inputBuffer.remaining();
        byte[] bytes = new byte[remaining];
        inputBuffer.get(bytes);
        return bytes;
    }


    private void send(SelectionKey selectionKey, Queue<SelectionKey> queue) {
        // Send payload the Processor attached to SelectionKey in Queue
        if (queue.isEmpty()) { // queue should not be empty
            return;
        }
        byte[] payload = getOutputPayload(selectionKey, queue);
        if (Objects.isNull(payload)) {
            return;
        }
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        try {
            if (isHandshakeResponse.test(payload)) {
                sendHandshakePayload(selectionKey, channel, payload);
            } else {
                sendPayload(selectionKey, channel, payload);
            }
            removeSelectionKeyWriteInterest(selectionKey);
        } catch (Exception e) {
            log.error("{} byteBuffer or channel write error {}", nodeName, selectionKey, e);
            try {
                channel.close();
            } catch (IOException ex) {
                log.error("{} channel close error {}", nodeName, channel, ex);
            }
        }
    }

    private void sendHandshakePayload(SelectionKey selectionKey, SocketChannel channel, byte[] payload) throws IOException {
        // send two envelopes in one response to complete 'remote' side of handshake: version + verack (as per bitcoind)
        HandshakeResponses handshakeResponses = HandshakeResponses.parse(payload);
        byte[] twoEnvelopes = handshakeResponses.serialize();
        loadOutputBuffer(twoEnvelopes);  // sending two serialized envelopes in one socket write
        if (log.isDebugEnabled()) {
            log.debug("{} sending handshake version + verack response '{}' via channel {}",
                    nodeName, HEX.encode(twoEnvelopes),
                    channelInfo.apply(selectionKey.channel()));
        }
        channel.write(outputBuffer);
        String cacheKey = channelToCacheKey.apply(channel);
        long ttl = System.currentTimeMillis() + HANDSHAKE_TIME_TO_LIVE;
        handshakeCache.add(cacheKey, ttl);
    }

    private void removeSelectionKeyWriteInterest(SelectionKey selectionKey) {
        selectionKey.attach(null);   // clear sk's attachment
        selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
    }

    private void sendPayload(SelectionKey selectionKey, SocketChannel channel, byte[] payload) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("{} sending response '{}' via channel {}", nodeName, payload, channelInfo.apply(selectionKey.channel()));
        }
        loadOutputBuffer(payload);
        channel.write(outputBuffer);
    }

    private byte[] getOutputPayload(SelectionKey selectionKey, Queue<SelectionKey> queue) {
        int size = queue.size();
        boolean removed = queue.remove(selectionKey);
        if (log.isDebugEnabled()) {
            log.debug("{} queue size {}, removed selectionKey {}", nodeName, size, removed);
        }
        return (byte[]) selectionKey.attachment(); // the outgoing payload is attached to sk
    }

    private void loadOutputBuffer(byte[] payload) {
        outputBuffer.clear();
        try {
            outputBuffer.put(payload);
        } catch (BufferOverflowException b) {
            outputBuffer.put("ERROR".getBytes());
        }
        outputBuffer.flip();
    }
}
