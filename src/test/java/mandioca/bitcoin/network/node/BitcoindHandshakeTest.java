package mandioca.bitcoin.network.node;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitcoindHandshakeTest extends BitcoindClientTest {

    private static final Logger log = LoggerFactory.getLogger(BitcoindHandshakeTest.class);

    @Before
    public void setup() {
        super.setup();
    }

    @Test
    public void testHandshakeWithLocalBitcoindNode() {
        doHandshakeWithLocalBitcoindNode();
        printCachedEnvelopes();
    }

}
