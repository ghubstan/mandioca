package mandioca.bitcoin.network.node;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.lang.System.currentTimeMillis;
import static mandioca.bitcoin.function.TimeFunctions.durationString;
import static org.junit.Assert.*;

public class MultiPeerHandshakeTest extends NodeTest {

    private static final Logger log = LoggerFactory.getLogger(MultiPeerHandshakeTest.class);

    private static final boolean FORCE_SHUTDOWN = true;
    private static final int NUM_NODES = 100;
    private List<Node> servers;
    private Node nodeA;

    @Before
    public void setup() {
        this.servers = createServers(NUM_NODES);
        startServers(servers);
        this.nodeA = servers.get(0);
    }

    @Test
    public void testNodeInitiatedHandshakeWithAllPeers() {
        long t0 = currentTimeMillis();
        try {
            boolean success = nodeA.handshake();  // do handshake will all peers
            log.info("completed {} handshakes in {}", servers.size() - 1, durationString.apply(currentTimeMillis() - t0));
            assertTrue(success);
            assertEquals(NUM_NODES - 1, nodeA.getHandshakeCacheSize());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (FORCE_SHUTDOWN) {
                stopServers(servers);
            }
        }
    }
}
