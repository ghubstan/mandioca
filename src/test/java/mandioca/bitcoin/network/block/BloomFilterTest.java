package mandioca.bitcoin.network.block;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static mandioca.bitcoin.function.ByteArrayFunctions.stringToBytes;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

public class BloomFilterTest {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(BloomFilterTest.class);

    @Test
    public void testBloomFilterAdd() {
        BloomFilter bf = new BloomFilter(10, 5, 99);
        byte[] item = stringToBytes.apply("Hello World");
        bf.add(item);
        String expected = "0000000a080000000140";
        assertEquals(expected, HEX.encode(bf.filterBytes()));

        item = stringToBytes.apply("Goodbye!");
        bf.add(item);
        expected = "4000600a080000010940";
        assertEquals(expected, HEX.encode(bf.filterBytes()));
    }

    @Test
    public void testBloomFilterLoad() {
        BloomFilter bf = new BloomFilter(10, 5, 99);
        byte[] item = stringToBytes.apply("Hello World");
        bf.add(item);
        item = stringToBytes.apply("Goodbye!");
        bf.add(item);
        String expected = "0a4000600a080000010940050000006300000001";
        byte[] bytes = bf.filterLoad();
        assertEquals(expected, HEX.encode(bytes));
    }
}
