package mandioca.bitcoin.network.block;

import mandioca.bitcoin.MandiocaTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.network.NetworkConstants.HASH_LENGTH;
import static mandioca.bitcoin.network.NetworkConstants.ZERO_HASH;
import static mandioca.bitcoin.network.block.BlockHeader.parse;
import static mandioca.bitcoin.network.block.BlockHeaderSerializer.*;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

public class RegtestGenesisBlockTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(RegtestGenesisBlockTest.class);

    private String hashHex = "0f9188f13cb7b2c71f2a335e3a4fc328bf5beb436012afca590b1a11466e2206";
    private int versionInt = 1;
    private String merkleRootHex = "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b";
    private int timestampInt = 1296688602;
    private String bitsHex = "207fffff";        // bits for unit testing
    private int nonceInt = 2;
    private BigDecimal difficulty = new BigDecimal("4.656542373906925e-10");

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
        // log.info("genesisBlockHeader {}", genesisBlockHeader);

        assertEquals(merkleRootHex, genesisBlockHeader.getMerkleRootHex());
        assertEquals(timestampInt, genesisBlockHeader.getTimestampInt());
        assertEquals("00000001", genesisBlockHeader.getVersionHex());
        assertEquals("207fffff", genesisBlockHeader.getBitsHex());
        assertEquals(nonceInt, genesisBlockHeader.getNonceInt());
        assertEquals(hashHex, genesisBlockHeader.getHashHex());

        Block block = new Block(genesisBlockHeader);
        assertEquals(0L, block.getDifficulty().longValue()); // TODO why is difficulty ZERO?
        assertEquals(hashHex, block.id());
        log.info("block.target {}", block.getTargetHex());
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
