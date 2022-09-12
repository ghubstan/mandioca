package mandioca.bitcoin.network.node;

import mandioca.bitcoin.network.message.PingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;

import static mandioca.bitcoin.network.NetworkConstants.PING_LENGTH;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;
import static mandioca.bitcoin.network.message.MessageFactory.pingMessage;
import static mandioca.bitcoin.network.message.MessageType.PONG;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.addressInfo;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.channelInfo;

/**
 * The ping message is sent primarily to confirm that the TCP/IP connection is still valid.
 * An error in transmission is presumed to be a closed connection and the address is removed as a current peer.
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#ping">https://en.bitcoin.it/wiki/Protocol_documentation#ping</a>
 */
public class PingClient extends CallableClient<Boolean> implements Client {

    private static final Logger log = LoggerFactory.getLogger(PingClient.class);

    private final BiFunction<byte[], byte[], Boolean> isSameNonce = Arrays::equals;

    public PingClient(String nodeName, InetSocketAddress peer) {
        super(nodeName, peer, PING_LENGTH);
    }

    @Override
    public Boolean call() throws Exception {
        try {
            socketChannel = configureClientChannel();
            return doPing();
        } catch (IOException e) {
            log.error("error pinging peer {}", channelInfo.apply(socketChannel), e);
            return false;
        } finally {
            returnByteBuffer();  // don't close channel, but always buffer to pool
        }
    }

    private boolean doPing() throws IOException {
        PingMessage ping = pingMessage.get();
        send(ping);
        return isValidPong(ping);
    }

    private void send(PingMessage ping) throws IOException {
        setPayload(envelopeHelper.pingPayload.apply(ping, NETWORK));
        send();  // send ping
        log.debug("{} initiated ping with peer {}", nodeName, addressInfo.apply(peer));
    }

    private boolean isValidPong(PingMessage ping) {
        Optional<NetworkEnvelope> pong = waitForEnvelope(PONG);
        if (pong.isPresent()) {
            NetworkEnvelope envelope = pong.get();
            if (envelopeHelper.isPong.test(envelope)) {
                if (isSameNonce.apply(ping.getNonce(), envelope.getPayload())) {
                    return true;
                } else {
                    log.error("pong.nonce != ping.nonce;  {} != {}",
                            Arrays.toString(envelope.getPayload()), Arrays.toString(ping.getNonce()));
                    return false; // pong.nonce != ping.nonce
                }
            } else {
                log.error("was expecting a pong, but got a {}", envelope);
                return false; // not a pong
            }
        } else {
            return false; // no envelope
        }
    }

}
