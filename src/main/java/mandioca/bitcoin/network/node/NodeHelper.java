package mandioca.bitcoin.network.node;

import mandioca.ioc.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static mandioca.bitcoin.network.NetworkProperties.HANDSHAKE_TIME_TO_LIVE;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.addressToCacheKey;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.channelToCacheKey;

@SuppressWarnings("unused")
public class NodeHelper {

    private static final Logger log = LoggerFactory.getLogger(NodeHelper.class);

    @Inject
    private String nodeName;
    @Inject
    private HandshakeCache handshakeCache;

    public NodeHelper() {
    }

    boolean handshakePeers(List<InetSocketAddress> peers) {
        // Serially performs handshakes with all peers and returns true if # of successful handshakes = # of peers
        try {
            int[] numHandshakes = {0};
            int[] numFailedHandshakes = {0};
            if (log.isDebugEnabled()) {
                log.debug("{} handshaking with {} peers", nodeName, peers.size());
            }
            //noinspection SimplifyStreamApiCallChains
            peers.stream().forEachOrdered(p -> {
                try {
                    numHandshakes[0] += handshake(p) ? 1 : 0;
                    if (log.isDebugEnabled()) {
                        log.debug("{} completed handshake with peer {}", nodeName, p);
                    }
                } catch (HandshakeFailedException e) {
                    numFailedHandshakes[0]++;
                    log.error("failed handshake count = {}", numFailedHandshakes[0], e);
                }
            });
            return numHandshakes[0] == peers.size();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    boolean handshake(InetSocketAddress peer) throws HandshakeFailedException {
        // Performs handshake with one peer and returns true if # of successful handshakes = 1
        HandshakeClient client = new HandshakeClient(nodeName, peer);
        try {
            boolean success = client.call(); // call in current thread (not blocking) and do not close channel
            if (success) {
                String cacheKey = channelToCacheKey.apply(client.getSocketChannel());
                long ttl = currentTimeMillis() + HANDSHAKE_TIME_TO_LIVE;
                handshakeCache.add(cacheKey, ttl);
            }
            return success;
        } catch (Exception e) {
            throw new HandshakeFailedException(String.format("failed with peer %s", peer), e);
        }
    }

    boolean isHandshakeCached(InetSocketAddress peer) {
        String peerKey = addressToCacheKey.apply(peer);
        if (handshakeCache.isCached(peerKey)) {
            return true;
        } else {
            log.info("{} handshaking peer {} ", nodeName, peerKey);
            try {
                if (handshake(peer)) {
                    return true;
                } else {
                    log.error("failed handshake with peer {}", peerKey);
                    return false;
                }
            } catch (HandshakeFailedException e) {
                log.error("failed handshake with peer {}", peerKey, e);
                return false;
            }
        }
    }

    boolean ping(InetSocketAddress peer) {
        if (isHandshakeCached(peer)) {
            PingClient client = new PingClient(nodeName, peer);
            try {
                return client.call(); // call in current thread (not blocking) and do not close channel
            } catch (Exception e) {
                log.error("{} failure while pinging peer {}", nodeName, addressToCacheKey.apply(peer), e);
                return false;
            }
        } else {
            return false;
        }
    }
}
