package mandioca.bitcoin.network.node;

import mandioca.bitcoin.network.message.VersionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static mandioca.bitcoin.function.ByteArrayFunctions.ONE_BYTE;
import static mandioca.bitcoin.function.ByteArrayFunctions.ZERO_BYTE;
import static mandioca.bitcoin.network.NetworkProperties.HANDSHAKE_COMPLETION_WAIT;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;
import static mandioca.bitcoin.network.message.MessageFactory.getNonce;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.addressInfo;
import static mandioca.bitcoin.util.HexUtils.HEX;

/**
 * This class handles the initiation and completion of a a handshake as described below, and it's socket will not be
 * closed so it can be used by other types of clients.
 * <p>
 * <p>
 * Version Handshake
 * <p>
 * When the local peer (L) connects to a remote peer (R), the remote peer will not send any data until it receives
 * a version message.
 * <p>
 * L -> R: Send version message with the local peer's version
 * R -> L: Send version message back
 * R -> L: Send verack message
 * R:      Sets version to the minimum of the 2 versions
 * L -> R: Send verack message after receiving version message from R
 * L:      Sets version to the minimum of the 2 versions
 * <p>
 * Note: Versions below 31800 are no longer supported.
 *
 * @see <a href="https://en.bitcoin.it/wiki/Version_Handshake">https://en.bitcoin.it/wiki/Version_Handshake</a>
 * @see <a href="https://bitcoindev.network/bitcoin-wire-protocol">https://bitcoindev.network/bitcoin-wire-protocol</a>
 */
public class HandshakeClient extends CallableClient<Boolean> implements Client {

    private static final Logger log = LoggerFactory.getLogger(HandshakeClient.class);

    static final int DEFAULT_BUFFER_SIZE = 512;

    private final byte[] relay;

    public HandshakeClient(String nodeName, InetSocketAddress peer) {
        this(nodeName, peer, DEFAULT_BUFFER_SIZE, false);
    }

    public HandshakeClient(String nodeName, InetSocketAddress peer, boolean relay) {
        this(nodeName, peer, DEFAULT_BUFFER_SIZE, relay);
    }

    public HandshakeClient(String nodeName, InetSocketAddress peer, int bufferSize) {
        this(nodeName, peer, bufferSize, false);
    }

    public HandshakeClient(String nodeName, InetSocketAddress peer, int bufferSize, boolean relay) {
        super(nodeName, peer, bufferSize);
        this.relay = relay ? ONE_BYTE : ZERO_BYTE;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            socketChannel = configureClientChannel();
            return doHandshake();
        } finally {
            returnByteBuffer();  // never close channel after handshake, but return buffer to pool
        }
    }

    public byte[] getRelay() {
        return relay;
    }

    private boolean doHandshake() throws IOException, InterruptedException {
        if (!socketChannel.isConnected()) {
            log.error("not connected to bitcoin node");
            return false;
        }
        initiateHandshake();
        if (!isPeerLocalRegtestNode) {
            MILLISECONDS.sleep(15L);
        }
        NetworkEnvelope[] handshakeAck = readHandshakeAck(); // TODO re-impl 'waitForCommands' cuz garbage cometh
        if (handshakeIsSuccessful.apply(handshakeAck)) {
            sendHandshakeCompletionAck();
            return true;
        } else {
            return false;
        }
    }

    private void initiateHandshake() throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("{} initiating handshake with {} peer {}", nodeName, NETWORK.name().toLowerCase(), addressInfo.apply(peer));
        }
        VersionMessage vMessage = versionMessageSupplier.get();
        NetworkEnvelope vEnvelope = versionEnvelope.apply(vMessage);
        setPayload(vEnvelope.serialize());
        if (log.isDebugEnabled()) {
            log.debug("sending version: {}", HEX.encode(payload));
        }
        send(); // send version payload
    }

    private NetworkEnvelope[] readHandshakeAck() throws IOException {
        read(); // read version & verack in one payload (as per bitcoind)
        return (payload != null) ? parseVersionAndVerackResponse.get() : null;
    }

    private void sendHandshakeCompletionAck() throws IOException, InterruptedException {
        setPayload(envelopeHelper.verackPayload.apply(NETWORK));
        send();       // send final verack to complete handshake
        MILLISECONDS.sleep(HANDSHAKE_COMPLETION_WAIT); // give remote node time to rcv final verack
    }

    private final Supplier<VersionMessage> versionMessageSupplier = () ->
            new VersionMessage.VersionMessageBuilder()
                    .withTimestamp()
                    .withNonce(getNonce())
                    .withRelay(this.getRelay())
                    .build();

    private final Function<VersionMessage, NetworkEnvelope> versionEnvelope = (vm) ->
            new NetworkEnvelope(vm.getMessageType().command(), vm.serialize(), NETWORK);

    private final Function<NetworkEnvelope[], Boolean> handshakeIsSuccessful = (e) ->
            e != null && e.length == 2
                    && envelopeHelper.isVersion.test(e[0])
                    && envelopeHelper.isVerAck.test(e[1]);

    private final Supplier<NetworkEnvelope[]> parseVersionAndVerackResponse = () ->
            NetworkEnvelope.parseAll(stream.apply(payload), NETWORK);

}
