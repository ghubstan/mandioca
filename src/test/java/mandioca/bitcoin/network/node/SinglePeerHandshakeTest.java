package mandioca.bitcoin.network.node;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static java.lang.System.currentTimeMillis;
import static mandioca.bitcoin.function.TimeFunctions.durationString;
import static mandioca.bitcoin.network.NetworkProperties.LOCALHOST;
import static org.junit.Assert.*;

public class SinglePeerHandshakeTest extends NodeTest {

    private static final Logger log = LoggerFactory.getLogger(SinglePeerHandshakeTest.class);

    private static final boolean FORCE_SHUTDOWN = true;

    private Node nodeA;
    private Node nodeB;

    @Before
    public void setup() {
        this.nodeA = startServer(LOCALHOST, 0);
        this.nodeB = startServer(LOCALHOST, 0);
    }

    @Test
    public void testNodeInitiatedHandshakeWithOnePeer() {
        long t0 = currentTimeMillis();
        InetSocketAddress peer = new InetSocketAddress(nodeB.getHostname(), nodeB.getPort());
        try {
            boolean success = nodeA.handshake(peer);
            log.info("completed handshake in {}", durationString.apply(currentTimeMillis() - t0));
            assertTrue(success);
            assertEquals(1, nodeA.getHandshakeCacheSize());
        } catch (HandshakeFailedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (FORCE_SHUTDOWN) {
                nodeA.shutdown();
                nodeB.shutdown();
            }
        }
    }
}
