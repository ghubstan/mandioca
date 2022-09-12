package mandioca.bitcoin.network.block;

import mandioca.bitcoin.stack.BlockingStack;
import mandioca.bitcoin.stack.Stack;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static mandioca.bitcoin.network.block.MerkleTreeTestUtils.assertNodeLabel;
import static mandioca.bitcoin.network.block.MerkleTreeTestUtils.getHashesStack;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

// Independent of bitcoin blocks

public class MerkleTreeTest {

    private static final Logger log = LoggerFactory.getLogger(MerkleTreeTest.class);

    private final List<byte[]> txHashes = new ArrayList<>() {{
        add(HEX.decode("9745f7173ef14ee4155722d1cbf13304339fd00d900b759c6f9d58579b5765fb"));
        add(HEX.decode("5573c8ede34936c29cdfdfe743f7f5fdfbd4f54ba0705259e62f39917065cb9b"));
        add(HEX.decode("82a02ecbb6623b4274dfcab82b336dc017a27136e08521091e443e62582e8f05"));
        add(HEX.decode("507ccae5ed9b340363a0e6d765af148be9cb1c8766ccc922f83e4ae681658308"));
        add(HEX.decode("a7a4aec28e7162e1e9ef33dfa30f0bc0526e6cf4b11a576f6c5de58593898330"));
        add(HEX.decode("bb6267664bd833fd9fc82582853ab144fece26b7a8a5bf328f8a059445b59add"));
        add(HEX.decode("ea6d7ac1ee77fbacee58fc717b990c4fcccf1b19af43103c090f601677fd8836"));
        add(HEX.decode("457743861de496c429912558a106b810b0507975a49773228aa788df40730d41"));
        add(HEX.decode("7688029288efc9e9a0011c960a6ed9e5466581abf3e3a6c26ee317461add619a"));
        add(HEX.decode("b1ae7f15836cb2286cdd4e2c37bf9bb7da0a2846d06867a429f654b2e7f383c9"));
        add(HEX.decode("9b74f89fa3f93e71ff2c241f32945d877281a6a50a6bf94adac002980aafe5ab"));
        add(HEX.decode("b3a92b5b255019bdaf754875633c2de9fec2ab03e6b8ce669d07cb5b18804638"));
        add(HEX.decode("b5c0b915312b9bdaedd2b86aa2d0f8feffc73a2d37668fd9010179261e25e263"));
        add(HEX.decode("c9d52c5cb1e557b92c84c52e7c4bfbce859408bedffc8a5560fd6e35e10b8800"));
        add(HEX.decode("c555bc5fc3bc096df0a0c9532f07640bfb76bfe4fc1ace214b8b228a1297a4c2"));
        add(HEX.decode("f9dbfafc3af3400954975da24eb325e326960a25b87fffe23eef3e7ed2fb610e"));
    }};

    @Test
    public void testCreateEmptyMerkleTree() {
        MerkleTree merkleTree = new MerkleTree(txHashes.size());
        // log.info(merkleTree.toString());
    }

    @Test
    public void testCreateMerkleTree() {
        MerkleTree tree = new MerkleTree(txHashes.size());
        tree.populateTree(txHashes);
        // log.info(tree.toString());
        assertEquals("597c4bafe3832b17cbbabe56f878f4fc2ad0f6a402cee7fa851a9cb205f87ed1", tree.rootHex.get());
        assertNodeLabel(tree, 0, 0, "597c4bafe3832b17cbbabe56f878f4fc2ad0f6a402cee7fa851a9cb205f87ed1");
        assertNodeLabel(tree, 1, 0, "6382df3f3a0b1323ff73f4da50dc5e318468734d6054111481921d845c020b93");
        assertNodeLabel(tree, 2, 2, "7ab01bb63d45a05721c8d1f653fcf76fecc377315b3e60051e4e9fd28eb5b962");
        assertNodeLabel(tree, 3, 7, "8636b7a3935a68e49dd19fc224a8318f4ee3c14791b3388f47f9dc3dee2247d1");
        assertNodeLabel(tree, 4, 15, "f9dbfafc3af3400954975da24eb325e326960a25b87fffe23eef3e7ed2fb610e");
    }

    @Test
    public void testInitMerkleTree() {
        // From /code-ch11/merkleblock.py       def test_init(self):
        MerkleTree tree = new MerkleTree(9);
        assertEquals(1, tree.getNodes(0).size());
        assertEquals(2, tree.getNodes(1).size());
        assertEquals(3, tree.getNodes(2).size());
        assertEquals(5, tree.getNodes(3).size());
        assertEquals(9, tree.getNodes(4).size());
    }

    @Test
    public void testPopulateMerkleTree1() {
        // From /code-ch11/merkleblock.py        def test_populate_tree_1(self)
        MerkleTree tree = new MerkleTree(txHashes.size());
        assertEquals(1, tree.getNodes(0).size());
        assertEquals(2, tree.getNodes(1).size());
        assertEquals(4, tree.getNodes(2).size());
        assertEquals(8, tree.getNodes(3).size());
        assertEquals(16, tree.getNodes(4).size());

        byte[] flagBytes = new byte[31]; // size of flags (in bytes)
        Arrays.fill(flagBytes, (byte) 0x01);
        Stack flagBitsStack = bytesToBitField(flagBytes);
        Stack hashesStack = getHashesStack(txHashes);

        tree.populateTree(flagBitsStack, hashesStack);
        assertEquals("597c4bafe3832b17cbbabe56f878f4fc2ad0f6a402cee7fa851a9cb205f87ed1", tree.rootHex.get());
    }


    @Test
    public void testPopulateMerkleTree2() {
        // From /code-ch11/merkleblock.py        def test_populate_tree_2(self)
        List<byte[]> oddTxHashes = new ArrayList<>() {{
            add(HEX.decode("42f6f52f17620653dcc909e58bb352e0bd4bd1381e2955d19c00959a22122b2e"));
            add(HEX.decode("94c3af34b9667bf787e1c6a0a009201589755d01d02fe2877cc69b929d2418d4"));
            add(HEX.decode("959428d7c48113cb9149d0566bde3d46e98cf028053c522b8fa8f735241aa953"));
            add(HEX.decode("a9f27b99d5d108dede755710d4a1ffa2c74af70b4ca71726fa57d68454e609a2"));
            add(HEX.decode("62af110031e29de1efcad103b3ad4bec7bdcf6cb9c9f4afdd586981795516577")); //
        }};
        MerkleTree tree = new MerkleTree(oddTxHashes.size());

        byte[] flagBytes = new byte[11]; // size of flags (in bytes)
        Arrays.fill(flagBytes, (byte) 0x01);
        Stack flagBitsStack = bytesToBitField(flagBytes);
        Stack hashesStack = getHashesStack(oddTxHashes);

        tree.populateTree(flagBitsStack, hashesStack);
        assertEquals("a8e8bd023169b81bc56854137a135b97ef47a6a7237f4c6e037baed16285a5ab", tree.rootHex.get());
    }

    private Stack bytesToBitField(byte[] bytes) {
        // Independent of bitcoin blocks
        Stack flagBits = new BlockingStack(bytes.length);
        for (byte aByte : bytes) {
            flagBits.push((byte) (aByte & 1));
        }
        return flagBits;
    }

}
