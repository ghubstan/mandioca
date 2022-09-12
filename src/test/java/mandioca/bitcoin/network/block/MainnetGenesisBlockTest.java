package mandioca.bitcoin.network.block;

import mandioca.bitcoin.MandiocaTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.network.NetworkConstants.HASH_LENGTH;
import static mandioca.bitcoin.network.NetworkConstants.ZERO_HASH;
import static mandioca.bitcoin.network.block.BlockHeader.parse;
import static mandioca.bitcoin.network.block.BlockHeaderSerializer.*;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

public class MainnetGenesisBlockTest extends MandiocaTest {

    // https://en.bitcoin.it/wiki/Genesis_block
    // https://blockexplorer.com/block/000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f

    private static final Logger log = LoggerFactory.getLogger(MainnetGenesisBlockTest.class);

    private String hashHex = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";
    private int versionInt = 1;
    private String merkleRootHex = "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b";
    private int timestampInt = 1231006505;
    private String bitsHex = "1d00ffff";
    private int nonceInt = 2083236893;
    private int difficulty = 1;

    @Test
    public void testCreateGenesisBlockHeaderWithConstructor() {
        BlockHeader genesisBlockHeader = new BlockHeader(
                versionInt,
                ZERO_HASH,
                HEX.decode(merkleRootHex),
                timestampInt,
                HEX.decode(bitsHex),
                intToBytes.apply(nonceInt),
                new byte[]{});
        log.info("genesisBlockHeader {}", genesisBlockHeader);

        assertEquals(merkleRootHex, genesisBlockHeader.getMerkleRootHex());
        assertEquals(timestampInt, genesisBlockHeader.getTimestampInt());
        assertEquals("00000001", genesisBlockHeader.getVersionHex());
        assertEquals("1d00ffff", genesisBlockHeader.getBitsHex());
        assertEquals(nonceInt, genesisBlockHeader.getNonceInt());
        assertEquals(hashHex, genesisBlockHeader.getHashHex());

        Block block = new Block(genesisBlockHeader);
        assertEquals(difficulty, block.getDifficulty().longValue());
        assertEquals(hashHex, block.id());
        // log.info("block.target {}", block.getTargetHex());
    }

    @Test
    public void testParseGenesisBlockHeader() {
        final byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(serializeVersion.apply(versionInt));
            baos.write(emptyArray.apply(HASH_LENGTH)); // previous blk
            baos.write(serializeMerkleRoot.apply(HEX.decode(merkleRootHex)));
            baos.write(serializeTimestamp.apply(timestampInt));
            baos.write(serializeBits.apply(HEX.decode(bitsHex)));
            baos.write(serializeNonce.apply(intToBytes.apply(nonceInt)));
            bytes = baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("error constructing regtest genesis block", e);
        }

        String rawHeader = HEX.encode(bytes);
        // log.info("rawHeader {}", rawHeader);

        BlockHeader genesisBlockHeader = parse(hexToByteArrayInputStream.apply(rawHeader));
        assertEquals(hashHex, genesisBlockHeader.getHashHex());
    }
}
