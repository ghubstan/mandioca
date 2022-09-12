package mandioca.bitcoin.transaction;

import mandioca.bitcoin.MandiocaTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// TODO implement a way to load the real cache with contents of the test cache, so the TxFetcher can use it, and
// not have to try to fetch txs that do not exist on the networks.
public class BookLoadSerializedTxCacheTest extends MandiocaTest {

    @Test
    public void testLoadCache() {
        assertEquals(17, localTxCache.size());
        assertTrue(localTxCache.containsKey("0d6fe5213c0b3291f208cba8bfb59b7476dffacc4e5cb66f6eb20a080843a299"));
        assertTrue(localTxCache.containsKey("d869f854e1f8788bcff294cc83b280942a8c728de71eb709a2c29d10bfe21b7c"));
    }

}
