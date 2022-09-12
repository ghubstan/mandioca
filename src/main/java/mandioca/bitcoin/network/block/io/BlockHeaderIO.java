package mandioca.bitcoin.network.block.io;

import mandioca.bitcoin.network.node.NetworkEnvelope;
import mandioca.bitcoin.util.ByteArrayInOutStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static mandioca.bitcoin.function.ByteArrayFunctions.subarray;
import static mandioca.bitcoin.network.NetworkProperties.*;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.channelInfo;
import static mandioca.bitcoin.pbbl.ByteBufferLender.borrowBuffer;
import static mandioca.bitcoin.pbbl.ByteBufferLender.returnBuffer;

class BlockHeaderIO {

    private static final Logger log = LoggerFactory.getLogger(BlockHeaderIO.class);

    protected static final int END_OF_STREAM = -1; // indicates socket error

    protected final int maxChunkSize = NODE_STREAMING_MAX_CHUNKSIZE;

    // hopefully done; don't yet know of any other way to determine if stream is exhausted
    protected final Predicate<Integer> doneReadingSocket = (z) -> z >= CLIENT_SOCKET_ZEROREAD_LIMIT;
    protected final Predicate<Integer> socketEndOfStreamError = (c) -> c == END_OF_STREAM;
    protected final Predicate<Integer> readNoBytes = (c) -> c == 0;

    protected final SocketChannel socketChannel;

    private int zeroReadCount = 0; // number of times socket.read returned 0 bytes during a download session
    private long accumulatedReadCount = 0; // how many bytes read during the life of this instance

    public BlockHeaderIO(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    protected NetworkEnvelope getEnvelope(int bufferSize) throws Exception {
        ByteBuffer byteBuffer = getBuffer(bufferSize);
        try {
            List<byte[]> payloads;
            if (socketChannel.isConnected()) {
                socketChannel.configureBlocking(false);  // do this or socket.read may block forever, regardless of timeout
                payloads = new ArrayList<>(getPayloads(byteBuffer));
            } else {
                throw new RuntimeException("socket channel " + channelInfo.apply(socketChannel) + " not connected");
            }
            if (payloads.size() > 0) {
                return parsePayloads(payloads);
            } else {
                throw new RuntimeException("no payloads found in socket");
            }
        } finally {
            socketChannel.configureBlocking(true);
            returnBuffer(byteBuffer);
        }
    }

    protected List<byte[]> getPayloads(ByteBuffer byteBuffer) throws IOException {
        setZeroReadCount(0);
        try {
            List<byte[]> payloads = new ArrayList<>();
            while (!doneReadingSocket.test(getZeroReadCount())) {
                Optional<byte[]> payload = readSocket(byteBuffer);
                payload.ifPresent(payloads::add);
            }
            return payloads;
        } catch (IOException e) {
            throw new RuntimeException("error reading block header payloads from socket "
                    + channelInfo.apply(socketChannel));
        } finally {
            socketChannel.configureBlocking(true);
        }
    }

    protected Optional<byte[]> readSocket(ByteBuffer byteBuffer) throws IOException {
        byteBuffer.clear();
        int readCount = socketChannel.read(byteBuffer);
        if (socketEndOfStreamError.test(readCount)) {
            throw new RuntimeException("socket channel " + channelInfo.apply(socketChannel)
                    + " read op caused end of stream error");
        } else if (readNoBytes.test(readCount)) {
            incrZeroReadCount();
            return Optional.empty();
        } else {
            return Optional.of(getPayload(readCount, byteBuffer));
        }
    }

    protected byte[] getPayload(int numBytes, ByteBuffer byteBuffer) {
        if (byteBuffer.position() != numBytes) {
            throw new RuntimeException("byte buffer did not read expected number of bytes");
        }
        byte[] payload = subarray.apply(byteBuffer.array(), 0, numBytes);
        byteBuffer.flip();
        incrAccumulatedReadCount(numBytes);
        if (log.isDebugEnabled()) {
            log.debug("read {} bytes from {}, total read = {} bytes",
                    numBytes, channelInfo.apply(socketChannel), getAccumulatedReadCount());
        }
        return payload;
    }

    protected NetworkEnvelope parsePayloads(List<byte[]> payloads) {
        try (ByteArrayInOutStream baos = new ByteArrayInOutStream()) {
            for (byte[] payload : payloads) {
                baos.write(payload);
            }
            return NetworkEnvelope.parse(baos.getInputStream(), NETWORK);
        } catch (IOException e) {
            throw new RuntimeException("error parsing block envelope", e);
        }
    }

    protected final int getZeroReadCount() {
        return zeroReadCount;
    }

    protected final void setZeroReadCount(int zeroReadCount) {
        this.zeroReadCount = zeroReadCount;
    }

    protected final void incrZeroReadCount() {
        this.zeroReadCount++;
    }

    protected final void incrAccumulatedReadCount(int delta) {
        this.accumulatedReadCount += delta;
    }

    public long getAccumulatedReadCount() {
        return this.accumulatedReadCount;
    }

    protected ByteBuffer getBuffer(int bufferSize) {
        if (bufferSize > maxChunkSize) {
            throw new RuntimeException("requested byte buffer size > max allowed streaming chunksize size " + maxChunkSize);
        }
        return borrowBuffer.apply(bufferSize);
    }


    protected void returnBuffer(ByteBuffer byteBuffer) {
        returnBuffer.accept(byteBuffer);
    }
}
