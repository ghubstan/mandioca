package mandioca.bitcoin.network.node;

import mandioca.bitcoin.network.message.MessageType;
import mandioca.bitcoin.network.message.NetworkCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static mandioca.bitcoin.function.ByteArrayFunctions.toByteArrayInputStream;
import static mandioca.bitcoin.function.StorageUnitConversionFunctions.byteCountString;
import static mandioca.bitcoin.network.NetworkProperties.CLIENT_SOCKET_TIMEOUT;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;
import static mandioca.bitcoin.network.NetworkType.isRegtest;
import static mandioca.bitcoin.network.message.MessageType.getMessageType;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.addressInfo;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.channelInfo;
import static mandioca.bitcoin.pbbl.ByteBufferLender.borrowBuffer;
import static mandioca.bitcoin.pbbl.ByteBufferLender.returnBuffer;
import static mandioca.bitcoin.util.HexUtils.HEX;


/**
 * a node's client  for communicating with other nodes, such as performing handshakes, ping/pong, downloading headers, etc.
 */
abstract class AbstractClient implements Client {

    private static final Logger log = LoggerFactory.getLogger(AbstractClient.class);

    protected final NetworkEnvelopeHelper envelopeHelper = new NetworkEnvelopeHelper();

    protected SocketChannel socketChannel = null;
    protected byte[] payload;

    protected final Function<byte[], ByteArrayInputStream> stream = toByteArrayInputStream;

    protected final BiFunction<NetworkCommand, MessageType[], Boolean> waitingForCommands = (cmd, types) ->
            cmd == null || stream(types).noneMatch(t -> t.equals(getMessageType.apply(cmd)));

    protected final Function<Integer, String> intToByteCountString = (n) -> byteCountString.apply(Long.valueOf(n));

    protected final boolean isPeerLocalRegtestNode = isRegtest.test(NETWORK);

    protected final String nodeName; // this node's name, not the peer's name (for logging, debugging)
    protected final InetSocketAddress peer;
    protected final ByteBuffer byteBuffer;

    public AbstractClient(InetSocketAddress peer, int byteBufferSize) {
        this("", peer, byteBufferSize);
    }

    public AbstractClient(String nodeName, InetSocketAddress peer, int byteBufferSize) {
        this.peer = peer;
        this.byteBuffer = borrowBuffer.apply(byteBufferSize);
        this.nodeName = nodeName;
    }

    @Override
    public void read() throws IOException {
        try {
            byteBuffer.clear();
            if (socketChannel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                if (log.isDebugEnabled()) {
                    logNumBytesRead(bytes);
                }
                byteBuffer.clear();
                payload = bytes;
            } else {
                payload = null;
            }
        } catch (IOException e) {
            log.error("{} exception {} thrown while reading from channel {}",
                    nodeName, e.getMessage(), channelInfo.apply(socketChannel));
            throw e;
        }
    }


    @Override
    public void send() throws IOException {
        if (payload != null) {
            byteBuffer.clear();
            byteBuffer.put(payload);
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            if (log.isDebugEnabled()) {
                logNumBytesWritten(payload);
            }
        } else {
            log.error("cannot send empty payload");
        }
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    @Override
    public final void returnByteBuffer() {
        returnBuffer.accept(byteBuffer);
    }


    protected void setPayload(byte[] payload) {
        this.payload = payload;
    }

    protected Optional<NetworkEnvelope> waitForEnvelope(MessageType messageType) {
        Optional<NetworkEnvelope[]> envelopes = waitForEnvelopes(messageType);
        return envelopes.isPresent() ? Optional.of(envelopes.get()[0]) : Optional.empty();
    }

    protected Optional<NetworkEnvelope[]> waitForEnvelopes(MessageType... messageTypes) {
        // Wait for messages of the given type in given order (modified from  programmingbitcoin/code-ch13/network.py)
        // TODO re-impl 'waitForEnvelopes' cuz garbage cometh
        try {
            List<MessageType> cmdQueue = stream(messageTypes).collect(Collectors.toList());
            NetworkEnvelope[] envelopes = new NetworkEnvelope[0];
            NetworkCommand command = null;
            while (waitingForCommands.apply(command, messageTypes)) {

                read();

                if (payload != null) {
                    envelopes = NetworkEnvelope.parseAll(stream.apply(payload), NETWORK);
                    if (envelopes != null) {
                        for (NetworkEnvelope envelope : envelopes) {
                            command = envelope.getNetworkCommand();
                            // TODO check for error
                            if (payload.length < 100) {
                                log.debug("{} rcvd {} '{}'", nodeName, command.getAscii(), HEX.encode(payload));
                                cmdQueue.remove(getMessageType.apply(command));
                            } else {
                                log.debug("{} rcvd {} msg", nodeName, command.getAscii());
                                cmdQueue.remove(getMessageType.apply(command));
                            }
                        }
                    }
                    if (!cmdQueue.isEmpty()) {
                        log.error("did not rcv all expected msg(s) in set {}; missing {}", messageTypes, cmdQueue);
                    }
                }
            }
            return Optional.of(Objects.requireNonNull(envelopes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Optional<NetworkCommand> waitForCommand(MessageType messageType) {
        Optional<NetworkCommand[]> networkCommand = waitForCommands(messageType);
        return networkCommand.isPresent() ? Optional.of(networkCommand.get()[0]) : Optional.empty();
    }

    protected Optional<NetworkCommand[]> waitForCommands(MessageType... messageTypes) {
        // Wait for messages of the given type in given order (modified from  programmingbitcoin/code-ch13/network.py)
        // TODO re-impl 'waitForCommands' cuz garbage cometh
        try {
            List<MessageType> cmdQueue = stream(messageTypes).collect(Collectors.toList());
            List<NetworkCommand> commandList = new ArrayList<>();
            NetworkCommand command = null;
            while (waitingForCommands.apply(command, messageTypes)) {

                read();

                if (payload != null) {
                    NetworkEnvelope[] envelopes = NetworkEnvelope.parseAll(stream.apply(payload), NETWORK);

                    if (envelopes != null) {
                        for (NetworkEnvelope envelope : envelopes) {
                            command = envelope.getNetworkCommand();
                            // TODO check for error
                            log.debug("{} rcvd {} '{}'", nodeName, command.getAscii(), HEX.encode(payload));
                            commandList.add(command);
                            cmdQueue.remove(getMessageType.apply(envelope.getNetworkCommand()));
                        }
                    }

                    if (!cmdQueue.isEmpty()) {
                        log.error("did not rcv all expected msg(s) in set {}; missing {}", messageTypes, cmdQueue);
                    }
                }
            }
            return Optional.of(Objects.requireNonNull(commandList.toArray(new NetworkCommand[0])));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SocketChannel configureClientChannel() throws IOException, InterruptedException {
        if (socketChannel != null && socketChannel.isOpen()) {
            throw new IllegalStateException("attempt to open socket channel that is already open");
        }
        try {
            socketChannel = SocketChannel.open(peer);
        } catch (IOException e) {
            log.error("could not open channel socket to peer {} on network {}",
                    addressInfo.apply(peer), NETWORK.name(), e);
            throw e;
        }
        socketChannel.configureBlocking(true);
        socketChannel.socket().setTcpNoDelay(true);
        if (CLIENT_SOCKET_TIMEOUT > 0) {
            /*
             *  Enable/disable {@link SocketOptions#SO_TIMEOUT SO_TIMEOUT}
             *  with the specified timeout, in milliseconds. With this option set
             *  to a non-zero timeout, a read() call on the InputStream associated with
             *  this Socket will block for only this amount of time.  If the timeout
             *  expires, a <B>java.net.SocketTimeoutException</B> is raised, though the
             *  Socket is still valid. The option <B>must</B> be enabled
             *  prior to entering the blocking operation to have effect. The
             *  timeout must be {@code > 0}.
             *  A timeout of zero is interpreted as an infinite timeout.
             */
            socketChannel.socket().setSoTimeout(CLIENT_SOCKET_TIMEOUT);
            log.debug("set client.socket.timeout to {} ms (will cause blocking on read)", CLIENT_SOCKET_TIMEOUT);
        }
        while (!socketChannel.finishConnect()) {
            log.info("{} connecting to peer {} ...", nodeName, peer);
            MILLISECONDS.sleep(5L);
        }
        return socketChannel;
    }

    protected void closeChannel() throws IOException, InterruptedException {
        if (socketChannel != null) {
            socketChannel.shutdownOutput(); // server will receive the FIN packet
            MILLISECONDS.sleep(300L);
            socketChannel.close();
        }
    }


    private void logNumBytesRead(byte[] readPayload) {
        if (readPayload.length < 100) {
            log.debug("{} read {} bytes ({}) in response '{}' from channel {}",
                    nodeName, readPayload.length, intToByteCountString.apply(readPayload.length),
                    HEX.encode(readPayload), channelInfo.apply(socketChannel));
        } else {
            log.debug("{} read {} bytes ({}) from channel {}",
                    nodeName, readPayload.length, intToByteCountString.apply(readPayload.length),
                    channelInfo.apply(socketChannel));
        }
    }


    private void logNumBytesWritten(byte[] writePayload) {
        if (writePayload.length < 100) {
            log.debug("{} sent {} bytes in response '{}' via channel {}", nodeName, writePayload.length,
                    HEX.encode(writePayload), channelInfo.apply(socketChannel));
        } else {
            log.debug("{} sent {} bytes via channel {}", nodeName, writePayload.length,
                    channelInfo.apply(socketChannel));
        }
    }

}
