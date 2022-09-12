package mandioca.bitcoin.network.block;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Arrays;

import static mandioca.bitcoin.function.ByteArrayFunctions.emptyArray;
import static mandioca.bitcoin.function.ByteArrayFunctions.stringToBytes;
import static mandioca.bitcoin.network.block.BloomFilter.bitFieldToBytes;
import static mandioca.bitcoin.network.block.Murmur3.murmurHash3;
import static mandioca.bitcoin.network.block.Murmur3.setBitLE;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Murmur3Test {

    private static final Logger log = LoggerFactory.getLogger(Murmur3Test.class);

    @Test
    public void testBitcoinJMurmur3Test1() {
        // From BitcoinJ BloomFilterTest #1
        int tweak = 0;
        int numHashFuncs = 5;
        // looks like a bit field (the data passed to bitcoinj murmur)
        byte[] data = new byte[]{0x00, 0x00, 0x00};
        // the object arg passed to bitcoinj murmur
        byte[] object = HEX.decode("99108ad8ed9bb6274d3980bab5a85c048f0950c8");
        for (int i = 0; i < numHashFuncs; i++) {
            int hash = murmurHash3(data, tweak, i, object);
            switch (i) {
                case 0:
                case 2:
                    assertEquals(0, hash);
                    break;
                case 1:
                    assertEquals(23, hash);
                    break;
                case 3:
                    assertEquals(19, hash);
                    break;
                case 4:
                    assertEquals(20, hash);
                    break;
                default:
                    fail("bad hash result");
            }
            setBitLE(data, hash);
        }
        //  log.info("bit field: {}", Arrays.toString(data));
    }

    @Test
    public void testChapter12Example4() {
        int fieldSize = 2;
        int numFunctions = 2;
        BigInteger tweak = BigInteger.valueOf(42);
        int bitFieldSize = fieldSize * Byte.SIZE;
        byte[] bitField = emptyArray.apply(bitFieldSize);
        byte[][] msgs = new byte[][]{stringToBytes.apply("hello world"), stringToBytes.apply("goodbye")};
        for (byte[] msg : msgs) {
            for (int i = 0; i < numFunctions; i++) {
                int hash = murmurHash3(bitField, tweak.intValue(), i, msg);
                int bit = BigInteger.valueOf(hash).mod(BigInteger.valueOf(bitFieldSize)).intValue();
                bitField[bit] = 0x01;
            }
        }
        assertEquals("[0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0]", Arrays.toString(bitField));
    }

    @Test
    public void testChapter12Exercise2() {
        // Given a Bloom Filter with `size=10`, `function_count=5`, `tweak=99`, what are the bytes
        // that are set after adding these items? (Use `bit_field_to_bytes` to convert to bytes.)
        //>>> print(bit_field_to_bytes(bit_field).hex())
        //      4000600a080000010940
        int fieldSize = 10;
        int numFunctions = 5;
        BigInteger tweak = BigInteger.valueOf(99);
        int bitFieldSize = fieldSize * Byte.SIZE;
        byte[] bitField = emptyArray.apply(bitFieldSize);
        byte[][] msgs = new byte[][]{stringToBytes.apply("Hello World"), stringToBytes.apply("Goodbye!")};
        for (byte[] msg : msgs) {
            for (int i = 0; i < numFunctions; i++) {
                int hash = murmurHash3(bitField, tweak.intValue(), i, msg);
                int bit = BigInteger.valueOf(hash).mod(BigInteger.valueOf(bitFieldSize)).intValue();
                bitField[bit] = 0x01;
            }
        }
        // log.info("bit field: {}", Arrays.toString(bitField));
        byte[] result = bitFieldToBytes(bitField);
        String expected = "4000600a080000010940";
        assertEquals(expected, HEX.encode(result));
    }
}
