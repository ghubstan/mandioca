package mandioca.bitcoin.network.message;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.network.block.MerkleTree;
import mandioca.bitcoin.stack.Stack;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.block.MerkleTreeTestUtils.getHashesStack;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.*;

public class MerkleBlockMessageTest extends MandiocaTest {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(MerkleBlockMessageTest.class);

    private static final String BLOCK_HEX = "00000020df3b053dc46f162a9b00c7f0d5124e2676d47bbe7c5d0793a500000000000000ef445fef2ed495c275892206ca533e7411907971013ab83e3b47bd0d692d14d4dc7c835b67d8001ac157e670bf0d00000aba412a0d1480e370173072c9562becffe87aa661c1e4a6dbc305d38ec5dc088a7cf92e6458aca7b32edae818f9c2c98c37e06bf72ae0ce80649a38655ee1e27d34d9421d940b16732f24b94023e9d572a7f9ab8023434a4feb532d2adfc8c2c2158785d1bd04eb99df2e86c54bc13e139862897217400def5d72c280222c4cbaee7261831e1550dbb8fa82853e9fe506fc5fda3f7b919d8fe74b6282f92763cef8e625f977af7c8619c32a369b832bc2d051ecd9c73c51e76370ceabd4f25097c256597fa898d404ed53425de608ac6bfe426f6e2bb457f1c554866eb69dcb8d6bf6f880e9a59b3cd053e6c7060eeacaacf4dac6697dac20e4bd3f38a2ea2543d1ab7953e3430790a9f81e1c67f5b58c825acf46bd02848384eebe9af917274cdfbb1a28a5d58a23a17977def0de10d644258d9c54f886d47d293a411cb6226103b55635";

    @Test
    public void testParse() {
        // From code-ch11/merkleblock.py  def test_parse(self)
        MerkleBlockMessage mbm = MerkleBlockMessage.parse(hexStream.apply(BLOCK_HEX));

        assertEquals("20000000", mbm.getVersionHex());
        assertEquals("ef445fef2ed495c275892206ca533e7411907971013ab83e3b47bd0d692d14d4", mbm.getMerkleRootHex());
        byte[] leMerkleRoot = reverse.apply(HEX.decode(mbm.getMerkleRootHex()));
        assertArrayEquals(leMerkleRoot, mbm.merkleRoot);
        assertEquals("df3b053dc46f162a9b00c7f0d5124e2676d47bbe7c5d0793a500000000000000", mbm.getPreviousBlockHex());
        assertEquals("dc7c835b", mbm.getTimestampHex());
        assertEquals("67d8001a", mbm.getBitsHex());
        assertEquals("c157e670", mbm.getNonceHex());
        assertEquals(3519, mbm.getTransactionCountInt());   // tx count is what jsong calls 'total'
        assertEquals("00000dbf", mbm.getTransactionCountHex());
        assertEquals(10, mbm.getHashCountInt());
        List<byte[]> hashesList = mbm.getHashesList();
        assertEquals("ba412a0d1480e370173072c9562becffe87aa661c1e4a6dbc305d38ec5dc088a", HEX.encode(hashesList.get(0)));
        assertEquals("7cf92e6458aca7b32edae818f9c2c98c37e06bf72ae0ce80649a38655ee1e27d", HEX.encode(hashesList.get(1)));
        assertEquals("f8e625f977af7c8619c32a369b832bc2d051ecd9c73c51e76370ceabd4f25097", HEX.encode(hashesList.get(5)));
        assertEquals("dfbb1a28a5d58a23a17977def0de10d644258d9c54f886d47d293a411cb62261", HEX.encode(hashesList.get(9)));
        assertEquals("03", mbm.getFlagBytesHex());
        assertEquals("b55635", mbm.getFlagsHex());
    }

    @Test
    public void testPopulateMerkleTree() {
        List<byte[]> txHashes = new ArrayList<>() {{
            add(HEX.decode("ba412a0d1480e370173072c9562becffe87aa661c1e4a6dbc305d38ec5dc088a"));
            add(HEX.decode("7cf92e6458aca7b32edae818f9c2c98c37e06bf72ae0ce80649a38655ee1e27d"));
            add(HEX.decode("34d9421d940b16732f24b94023e9d572a7f9ab8023434a4feb532d2adfc8c2c2"));
            add(HEX.decode("158785d1bd04eb99df2e86c54bc13e139862897217400def5d72c280222c4cba"));
            add(HEX.decode("ee7261831e1550dbb8fa82853e9fe506fc5fda3f7b919d8fe74b6282f92763ce"));
            add(HEX.decode("f8e625f977af7c8619c32a369b832bc2d051ecd9c73c51e76370ceabd4f25097"));
            add(HEX.decode("c256597fa898d404ed53425de608ac6bfe426f6e2bb457f1c554866eb69dcb8d"));
            add(HEX.decode("6bf6f880e9a59b3cd053e6c7060eeacaacf4dac6697dac20e4bd3f38a2ea2543"));
            add(HEX.decode("d1ab7953e3430790a9f81e1c67f5b58c825acf46bd02848384eebe9af917274c"));
            add(HEX.decode("dfbb1a28a5d58a23a17977def0de10d644258d9c54f886d47d293a411cb62261"));
        }};
        MerkleTree tree = new MerkleTree(3519);     // takes total number of leaves (txnCount)
        assertEquals(1, tree.getNodes(0).size());
        assertEquals(2, tree.getNodes(1).size());
        assertEquals(4, tree.getNodes(2).size());
        assertEquals(7, tree.getNodes(3).size());
        assertEquals(14, tree.getNodes(4).size());
        assertEquals(3519, tree.getNodes(tree.getNodes().size() - 1).size());
        String flagsHex = "b55635";
        byte[] flags = HEX.decode(flagsHex);
        Stack flagBitsStack = MerkleTree.bytesToBitFieldStack(flags);
        Stack hashesStack = getHashesStack(txHashes);
        tree.populateTree(flagBitsStack, hashesStack);
        assertEquals("ef445fef2ed495c275892206ca533e7411907971013ab83e3b47bd0d692d14d4", tree.rootHex.get());
    }

    @Test
    public void testIsValid() {
        // From code-ch11/merkleblock.py  def test_is_valid(self)
        MerkleBlockMessage mbm = MerkleBlockMessage.parse(hexStream.apply(BLOCK_HEX));
        assertTrue(mbm.isValid());
    }
}
