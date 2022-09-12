package mandioca.bitcoin.network.node;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static mandioca.bitcoin.function.TimeFunctions.durationString;
import static org.junit.Assert.fail;

public class SinglePeerPingTest extends NodeTest {

    private static final Logger log = LoggerFactory.getLogger(SinglePeerPingTest.class);

    private static final boolean FORCE_SHUTDOWN = true;
    private static final int NUM_PINGS = 40;

    private final List<Node> servers = new ArrayList<>();
    private Node nodeA;


    @Before
    public void setup() {
        servers.addAll(createServers(2)); // nodes 'A' and 'B', each is the other's peer
        startServers(servers);
        this.nodeA = servers.get(0);
    }

    @Test
    public void testNodeInitiatedHandshakeWithOnePeer() {
        long t0 = currentTimeMillis();
        InetSocketAddress peer = nodeA.getPeers().get(0); // Node-A's single peer is Node-B
        try {
            int count = 0;
            while (count < NUM_PINGS) {
                if (!nodeA.ping(peer)) {
                    fail("test failure after " + count + " ping(s)");
                }
                MILLISECONDS.sleep(20L);
                count++;
                if (count % 20 == 0) {
                    log.info("ping count {};  time {}", count, durationString.apply(currentTimeMillis() - t0));
                }
            }
            log.info("completed {} pings in {}", count, durationString.apply(currentTimeMillis() - t0));
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
