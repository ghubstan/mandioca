package mandioca.bitcoin.network.block;

import mandioca.bitcoin.MandiocaTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static mandioca.bitcoin.function.BigIntegerFunctions.HEX_RADIX;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.network.block.BlockHeader.parse;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
public class BlockHeaderTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(BlockHeaderTest.class);

    @Test
    public void testParse() {
        // From programmingbitcoin/code-ch09/block.py def test_parse(self)
        String blockHeaderRaw = "020000208ec39428b17323fa0ddec8e887b4a7c53b8c0a0a220cfd0000000000000000005b0750fce0a889502d40508d39576821155e9c9e3f5c3157f961db38fd8b25be1e77a759e93c0118a4ffd71d";
        BlockHeader blockHeader = parse(hexToByteArrayInputStream.apply(blockHeaderRaw));

        String expectedVersionHex = "20000002";
        assertEquals(expectedVersionHex, blockHeader.getVersionHex());
        assertEquals(expectedVersionHex, Integer.toHexString(blockHeader.getVersionInt()));

        String expectedPreviousBlockHex = "000000000000000000fd0c220a0a8c3bc5a7b487e8c8de0dfa2373b12894c38e";
        assertEquals(expectedPreviousBlockHex, blockHeader.getPreviousBlockHex());
        assertEquals(expectedPreviousBlockHex, HEX.encode(blockHeader.getPreviousBlockBigEndian()));

        String expectedMerkleRootHex = "be258bfd38db61f957315c3f9e9c5e15216857398d50402d5089a8e0fc50075b";
        assertEquals(expectedMerkleRootHex, blockHeader.getMerkleRootHex());
        assertEquals(expectedMerkleRootHex, HEX.encode(blockHeader.merkleRootBigEndian()));

        String expectedTimestampHex = "59a7771e";
        String actualTimestampHex = blockHeader.getTimestampHex();
        assertEquals(expectedTimestampHex, actualTimestampHex);
        assertEquals(Integer.valueOf(expectedTimestampHex, HEX_RADIX).longValue(),
                Integer.valueOf(blockHeader.getTimestampInt()).longValue());

        // String expectedBitsHex = "e93c0118";
        String expectedBitsHex = "18013ce9"; // after interpreting field as little endian
        assertEquals(expectedBitsHex, blockHeader.getBitsHex());

        //  String expectedNonceHex = "a4ffd71d";
        String expectedNonceHex = "1dd7ffa4"; // after interpreting field as little endian
        assertEquals(expectedNonceHex, blockHeader.getNonceHex());

    }

    @Test
    public void testTestnet3GenesisBits() {
        // SEE https://api.blockcypher.com/v1/btc/test3/blocks/000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943
        // {
        //  "hash": "000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943",
        //  "height": 1,
        //  "chain": "BTC.test3",
        //  "total": 5000000000,
        //  "fees": 0,
        //  "size": 285,
        //  "ver": 1,
        //  "time": "2011-02-02T23:16:42Z",
        //  "received_time": "2011-02-02T23:16:42Z",
        //  "coinbase_addr": "",
        //  "relayed_by": "",
        //  "bits": 486604799,
        //  "nonce": 414098458,
        //  "n_tx": 0,
        //  "prev_block": "0000000000000000000000000000000000000000000000000000000000000000",
        //  "mrkl_root": "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b",
        //  "txids": [],
        //  "depth": 1664422,
        //  "prev_block_url": "https://api.blockcypher.com/v1/btc/test3/blocks/0000000000000000000000000000000000000000000000000000000000000000",
        //  "tx_url": "https://api.blockcypher.com/v1/btc/test3/txs/"
        // }
        int testnetGenesisBlkBits = 486604799;
        byte[] LOWEST_BITS = intToBytes.apply(testnetGenesisBlkBits);
        // log.info("testnet LOWEST_BITS.hex {}", HEX.encode(LOWEST_BITS));
        long bitsLong = bytesToLong.apply(LOWEST_BITS);
        assertEquals(testnetGenesisBlkBits, bitsLong);

        String regtestBitsHex = "207fffff";
        LOWEST_BITS = HEX.decode(regtestBitsHex);
        // log.info("regest LOWEST_BITS.hex {}", HEX.encode(LOWEST_BITS));

        // [main] INFO BlockHeaderTest - testnet LOWEST_BITS.hex 1d00ffff
        // [main] INFO BlockHeaderTest - regest LOWEST_BITS.hex 207fffff
        // TODO read the chapter about block getDifficulty again, make sure I'm not wasting time trying to figure out
        //      how it works for regest.
        //  If it's supposed to work, it looks like I need to write a Block impl for regtest ?
    }

    @Test
    public void testSerialize() {
        // From programmingbitcoin/code-ch09/block.py def test_serialize(self)
        String blockHeaderRaw = "020000208ec39428b17323fa0ddec8e887b4a7c53b8c0a0a220cfd0000000000000000005b0750fce0a889502d40508d39576821155e9c9e3f5c3157f961db38fd8b25be1e77a759e93c0118a4ffd71d";
        BlockHeader blockHeader = parse(hexToByteArrayInputStream.apply(blockHeaderRaw));
        byte[] serializedBlockHeader = blockHeader.serialize();
        assertEquals(80L, serializedBlockHeader.length);
        String actualSerializedHex = HEX.encode(serializedBlockHeader);
        assertEquals(blockHeaderRaw, actualSerializedHex);
    }

    @Test
    public void testHash() {
        // From programmingbitcoin/code-ch09/block.py def test_hash(self)
        String blockHeaderRaw = "020000208ec39428b17323fa0ddec8e887b4a7c53b8c0a0a220cfd0000000000000000005b0750fce0a889502d40508d39576821155e9c9e3f5c3157f961db38fd8b25be1e77a759e93c0118a4ffd71d";
        BlockHeader blockHeader = parse(hexToByteArrayInputStream.apply(blockHeaderRaw));
        String expectedHashHex = "0000000000000000007e9e4c586439b0cdbe13b1370bdd9435d76a644d047523";
        String actualHashHex = HEX.encode(blockHeader.hash());
        assertEquals(expectedHashHex, actualHashHex);
    }
}
