package mandioca.bitcoin.network.node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class StartMultipleServersTest extends NodeTest {

    private static final Logger log = LoggerFactory.getLogger(StartMultipleServersTest.class);

    private final List<Node> servers = new ArrayList<>();

    @Before
    public void setup() {
        servers.addAll(createServers(25));
        startServers(servers);
    }

    @Test
    public void waitASec() {
        try {
            MILLISECONDS.sleep(10000L);
        } catch (InterruptedException ignored) {
        }
    }

    @After
    public void teardown() {
        try {
            stopServers(servers);
            MILLISECONDS.sleep(3000L);
        } catch (InterruptedException ignored) {
        }
    }
}
