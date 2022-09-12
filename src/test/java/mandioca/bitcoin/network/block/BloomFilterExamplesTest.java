package mandioca.bitcoin.network.block;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.network.message.MerkleBlockMessage;
import mandioca.bitcoin.transaction.Tx;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static mandioca.bitcoin.function.BigIntegerFunctions.toBigInt;
import static mandioca.bitcoin.function.ByteArrayFunctions.emptyArray;
import static mandioca.bitcoin.function.ByteArrayFunctions.stringToBytes;
import static mandioca.bitcoin.function.HashFunctions.hash160;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.network.block.BloomFilter.bitFieldToBytes;
import static mandioca.bitcoin.network.block.BloomFilter.bytesToBitField;
import static mandioca.bitcoin.network.block.Murmur3.murmurHash3;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.*;


// I'd suggest we look into Neutrino (BIP157/BIP158)-based setups instead of bloom filters (BIP37).
// BIP37 is generally seen among core devs as fundamentally incompatible with privacy, and also imposes disproportional cost on full nodes (DoS vector).
// https://github.com/bisq-network/bisq/issues/1062

// TODO dedup code with CreateNewTxUsingBloomFilter (and move to src/main)

public class BloomFilterExamplesTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(BloomFilterExamplesTest.class);

    public final BiFunction<byte[], Integer, Integer> hashToBit = (hash, bitFieldSize) ->
            toBigInt.apply(hash).mod(BigInteger.valueOf(bitFieldSize)).intValue();

    @Test
    public void testChapter12Example1() {
        int bitFieldSize = 10;
        byte[] bitField = emptyArray.apply(bitFieldSize);
        byte[] hash = hash256.apply(stringToBytes.apply("hello world"));
        int bit = hashToBit.apply(hash, bitFieldSize);
        bitField[bit] = 0x01;
        assertEquals("[0, 0, 0, 0, 0, 0, 0, 0, 0, 1]", Arrays.toString(bitField));
    }

    @Test
    public void testChapter12Example2() {
        int bitFieldSize = 10;
        byte[] bitField = emptyArray.apply(bitFieldSize);
        byte[][] hashes = new byte[][]{
                hash256.apply(stringToBytes.apply("hello world")),
                hash256.apply(stringToBytes.apply("goodbye"))
        };
        for (byte[] hash : hashes) {
            int bit = hashToBit.apply(hash, bitFieldSize);
            bitField[bit] = 0x01;
        }
        assertEquals("[0, 0, 1, 0, 0, 0, 0, 0, 0, 1]", Arrays.toString(bitField));
    }

    @Test
    public void testChapter12Exercise1() {
        int bitFieldSize = 10;
        byte[] bitField = emptyArray.apply(bitFieldSize);
        byte[][] hashes = new byte[][]{
                hash160.apply(stringToBytes.apply("hello world")),
                hash160.apply(stringToBytes.apply("goodbye"))
        };
        for (byte[] hash : hashes) {
            int bit = hashToBit.apply(hash, bitFieldSize);
            bitField[bit] = 0x01;
        }
        assertEquals("[1, 1, 0, 0, 0, 0, 0, 0, 0, 0]", Arrays.toString(bitField));
    }

    @Test
    public void testChapter12Example3() {
        int bitFieldSize = 10;
        byte[] bitField = emptyArray.apply(bitFieldSize);
        byte[][] items = new byte[][]{stringToBytes.apply("hello world"), stringToBytes.apply("goodbye")};
        List<Function<byte[], byte[]>> hashFunctions = new ArrayList<>() {{
            add(hash256);
            add(hash160);
        }};
        for (byte[] item : items) {
            for (Function<byte[], byte[]> hashFunction : hashFunctions) {
                byte[] hash = hashFunction.apply(item);
                int bit = hashToBit.apply(hash, bitFieldSize);
                bitField[bit] = 0x01;
            }

        }
        assertEquals("[1, 1, 1, 0, 0, 0, 0, 0, 0, 1]", Arrays.toString(bitField));
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
        // print(bit_field_to_bytes(bit_field).hex())
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
        byte[] result = bitFieldToBytes(bitField);
        String expected = "4000600a080000010940";
        assertEquals(expected, HEX.encode(result));
    }

    @Test
    public void testBitFieldToBytes() {
        byte[] bitField = new byte[]{
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x00,
                0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00
        };
        byte[] result = bitFieldToBytes(bitField);
        String expected = "4000600a080000010940";
        assertEquals(expected, HEX.encode(result));
        assertArrayEquals(bitField, bytesToBitField(result));
    }

    @Test
    public void testBloomFilterQuery() {
        try {
            String[] addresses = new String[]{"mwJn1YPMq7y5F8J3LkC5Hxg9PHyZ5K4cFv"};
            BloomFilter bloomFilter = new BloomFilter(30, 5, 90210);
            BloomFilterQuery bloomFilterQuery = new BloomFilterQuery(addresses, bloomFilter);

            String blockLocator = "00000000000538d5c2246336644f9a4956551afb44ba47278759ec55ea912e19";
            boolean gotResults = bloomFilterQuery.runQuery(blockLocator);
            assertTrue(gotResults);

            List<MerkleBlockMessage> merkleBlockMessages = bloomFilterQuery.getMerkleBlocks();
            log.info("downloaded {} merkleblock msgs", merkleBlockMessages.size());
            assertEquals(2000, merkleBlockMessages.size());

            List<Tx> transactions = bloomFilterQuery.getAllTransactions();
            int totalTxOutCount = getTotalTxOutCount(transactions);

            log.info("downloaded {} transactions containing a total of {} tx-outputs", transactions.size(), totalTxOutCount);
            assertEquals(29, transactions.size());
            assertEquals(68, totalTxOutCount);

            List<Tx> matchingTransactions = bloomFilterQuery.getMatchingTransactions();
            int matchingTxCount = matchingTransactions.size();
            log.info("found {} matching transactions", matchingTxCount);
            assertEquals(6, matchingTxCount);

            // TODO cache the txOut.index too, and test (use a map to hold all results, or just the matching txs?)
            // found: e3930e1e566ca9b75d53b0eb9acb7607f547e1182d1d22bd4b661cfe18dcddf1:0    // 0 = txOut.index
            // found: 328e6563ac9879f926c1d3a290d5b28451f77cc8d07b3fce57c34ec823e61bc8:8    // 8 = txOut.index
            // found: 4e1bfcd066a149a2890c0f2e79d78acdfb88015a43bcd44a131cb6f46bcde4d9:0    // 0 = txOut.index
            // found: 6aa77ed9307e819fa74fbda9bcb27c5df950f2fc5bec0e0f7ac082193ea978d2:1    // 1 = txOut.index
            // found: e506990f85cd3b6a5256e8cd46500b56e548cf52fe81c7b2c5c01341a46223eb:1    // 1 = txOut.index
            // found: 8041073d69189db94e810e9091ddb75f864a0486ab7818ec8e1ca8ea7f867f0a:0    // 0 = txOut.index
            assertEquals("e3930e1e566ca9b75d53b0eb9acb7607f547e1182d1d22bd4b661cfe18dcddf1", matchingTransactions.get(0).id());
            assertEquals("328e6563ac9879f926c1d3a290d5b28451f77cc8d07b3fce57c34ec823e61bc8", matchingTransactions.get(1).id());
            assertEquals("4e1bfcd066a149a2890c0f2e79d78acdfb88015a43bcd44a131cb6f46bcde4d9", matchingTransactions.get(2).id());
            assertEquals("6aa77ed9307e819fa74fbda9bcb27c5df950f2fc5bec0e0f7ac082193ea978d2", matchingTransactions.get(3).id());
            assertEquals("e506990f85cd3b6a5256e8cd46500b56e548cf52fe81c7b2c5c01341a46223eb", matchingTransactions.get(4).id());
            assertEquals("8041073d69189db94e810e9091ddb75f864a0486ab7818ec8e1ca8ea7f867f0a", matchingTransactions.get(5).id());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private int getTotalTxOutCount(List<Tx> transactions) {
        int count = 0;
        for (Tx tx : transactions) {
            count += tx.getDeserializedOutputs().length;
        }
        return count;
    }
}