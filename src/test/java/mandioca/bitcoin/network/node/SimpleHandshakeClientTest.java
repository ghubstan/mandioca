package mandioca.bitcoin.network.node;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static mandioca.bitcoin.function.TimeFunctions.durationString;
import static mandioca.bitcoin.network.NetworkProperties.LOCALHOST;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SimpleHandshakeClientTest extends NodeTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleHandshakeClientTest.class);

    private static final boolean FORCE_SHUTDOWN = true;

    private static final boolean USE_RANDOM_SERVER_PORT = true;

    private Node server;
    private InetSocketAddress peer;

    @Before
    public void setup() {
        if (USE_RANDOM_SERVER_PORT) {
            server = startServer(LOCALHOST, 0);
        } else {
            server = startServer(LOCALHOST, portFactory.nextPort.get());
        }
        peer = new InetSocketAddress(server.getHostname(), server.getPort());
    }

    @Test
    public void testClientInitiatedHandshake() {
        HandshakeClient client = new HandshakeClient(server.getNodeName(), peer);
        ExecutorService testExecutorService = Executors.newFixedThreadPool(1);
        Future<Boolean> result = testExecutorService.submit(client);
        long t0 = currentTimeMillis();
        while (!result.isDone()) {
            try {
                MILLISECONDS.sleep(5L);
            } catch (InterruptedException ignored) {
            }
        }
        long executionTime = currentTimeMillis() - t0;
        testExecutorService.shutdown();
        try {
            assertTrue(result.get().booleanValue());
            log.info("completed handshake in {}", durationString.apply(executionTime));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            client.returnByteBuffer();
            if (FORCE_SHUTDOWN) {
                server.shutdown();
            }
        }
    }
}
