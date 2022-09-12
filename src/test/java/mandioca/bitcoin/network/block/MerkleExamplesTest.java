package mandioca.bitcoin.network.block;

import mandioca.bitcoin.function.TriFunction;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static mandioca.bitcoin.function.ByteArrayFunctions.concatenate;
import static mandioca.bitcoin.function.ByteArrayFunctions.emptyArray;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.network.NetworkConstants.HASH_LENGTH;
import static mandioca.bitcoin.network.block.MerkleTreeTestUtils.assertNodeLabel;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

// From Jimmy Song's "Programming Bitcoin", Chapter 11

public class MerkleExamplesTest {

    private static final Logger log = LoggerFactory.getLogger(MerkleExamplesTest.class);

    private final byte[] hash0 = HEX.decode("c117ea8ec828342f4dfb0ad6bd140e03a50720ece40169ee38bdc15d9eb64cf5");
    private final byte[] hash1 = HEX.decode("c131474164b412e3406696da1ee20ab0fc9bf41c8f05fa8ceea7a08d672d7cc5");
    private final byte[] hash2 = HEX.decode("f391da6ecfeed1814efae39e7fcb3838ae0b02c02ae7d0a5848a66947c0727b0");
    private final byte[] hash3 = HEX.decode("3d238a92a94532b946c90e19c49351c763696cff3db400485b813aecb8a13181");
    private final byte[] hash4 = HEX.decode("10092f2633be5f3ce349bf9ddbde36caa3dd10dfa0ec8106bce23acbff637dae");
    private final byte[] hash5 = HEX.decode("7d37b3d54fa6a64869084bfd2e831309118b9e833610e6228adacdbd1b4ba161");
    private final byte[] hash6 = HEX.decode("8118a77e542892fe15ae3fc771a4abfd2f5d5d5997544c3487ac36b5c85170fc");
    private final byte[] hash7 = HEX.decode("dff6879848c2c9b62fe652720b8df5272093acfaa45a43cdb3696fe2466a3877");
    private final byte[] hash8 = HEX.decode("b825c0745f46ac58f7d3759e6dc535a1fec7820377f24d4c2c6ad2cc55c0cb59");
    private final byte[] hash9 = HEX.decode("95513952a04bd8992721e9b7e2937f1c04ba31e0469fbe615a78197f68f52b7c");
    private final byte[] hash10 = HEX.decode("2e6d722e5e4dbdf2447ddecc9f7dabb8e299bae921c99ad5b0184cd9eb8e5908");
    private final byte[] hash11 = HEX.decode("b13a750047bc0bdceb2473e5fe488c2596d7a7124b4e716fdd29b046ef99bbf0");

    private final Consumer<List<byte[]>> balanceTree = (hashes) -> {
        if (hashes.size() % 2 == 1) {
            // if odd # of leaves, duplicate the last hash for balance
            hashes.add(hashes.get(hashes.size() - 1));
        }
    };

    private final BiFunction<byte[], byte[], byte[]> merkleParent = (l, r) -> hash256.apply(concatenate.apply(l, r));

    private final Function<List<byte[]>, List<byte[]>> merkleParentLevel = (hashes) -> {
        balanceTree.accept(hashes);
        List<byte[]> parentLevel = new ArrayList<>();
        for (int i = 0; i < hashes.size(); i += 2) { // skip 2
            byte[] parent = merkleParent.apply(hashes.get(i), hashes.get(i + 1));
            parentLevel.add(parent);
        }
        return parentLevel;
    };

    private final Function<List<byte[]>, byte[]> merkleRoot = (hashes) -> {
        List<byte[]> currentHashes = hashes;
        while (currentHashes.size() > 1) {
            currentHashes = merkleParentLevel.apply(currentHashes);
        }
        assert 1 == currentHashes.size() : "there can be only one merkle root";
        return currentHashes.get(0);
    };


    private final Function<Integer, Double> log2 = (n) -> Math.log(n) / Math.log(2);

    private final TriFunction<Integer, Integer, Integer, Integer> calcNumNodesAtDepth = (depth, maxDepth, totalLeaves) -> {
        BigDecimal tmp = BigDecimal.valueOf(totalLeaves).divide(BigDecimal.valueOf(2).pow(maxDepth - depth));
        return (int) Math.ceil(tmp.doubleValue());
    };

    private final Function<Integer, byte[][]> createLevelHashes = (numItems) -> {
        byte[][] levelHashes = new byte[numItems][];
        for (int i = 0; i < levelHashes.length; i++) {
            levelHashes[i] = emptyArray.apply(HASH_LENGTH);
        }
        return levelHashes;
    };

    private final Function<Integer, List<byte[][]>> createEmptyMerkleTree = (totalLeaves) -> {
        int maxDepth = (int) Math.ceil(log2.apply(totalLeaves));
        List<byte[][]> emptyMerkleTree = new ArrayList<>();
        for (int depth = 0; depth < maxDepth + 1; depth++) {
            int numItems = calcNumNodesAtDepth.apply(depth, maxDepth, totalLeaves);
            byte[][] levelHashes = createLevelHashes.apply(numItems);
            emptyMerkleTree.add(levelHashes);
        }
        return emptyMerkleTree;
    };


    @Test
    public void testChapter11Example1() {
        byte[] parent = hash256.apply(concatenate.apply(hash0, hash1));
        assertEquals("8b30c5ba100f6f2e5ad1e2a742e5020491240f8eb514fe97c713c31718ad7ecd", HEX.encode(parent));
    }

    @Test
    public void testChapter11Exercise1() {
        byte[] parent = merkleParent.apply(hash0, hash1);
        assertEquals("8b30c5ba100f6f2e5ad1e2a742e5020491240f8eb514fe97c713c31718ad7ecd", HEX.encode(parent));
    }

    @Test
    public void testChapter11Example2() {
        List<byte[]> hashes = new ArrayList<>() {{ // the tree
            add(hash0); // a leaf
            add(hash1); // a leaf...
            add(hash2);
            add(hash3);
            add(hash4);
        }};
        balanceTree.accept(hashes);
        List<byte[]> parentLevel = new ArrayList<>();
        for (int i = 0; i < hashes.size(); i += 2) {
            byte[] parent = merkleParent.apply(hashes.get(i), hashes.get(i + 1));
            parentLevel.add(parent);

        }
        assertEquals(3, parentLevel.size());
        assertEquals("8b30c5ba100f6f2e5ad1e2a742e5020491240f8eb514fe97c713c31718ad7ecd", HEX.encode(parentLevel.get(0)));
        assertEquals("7f4e6f9e224e20fda0ae4c44114237f97cd35aca38d83081c9bfd41feb907800", HEX.encode(parentLevel.get(1)));
        assertEquals("3ecf6115380c77e8aae56660f5634982ee897351ba906a6837d15ebc3a225df0", HEX.encode(parentLevel.get(2)));
    }

    @Test
    public void testChapter11Exercise2() {
        List<byte[]> hashes = new ArrayList<>() {{ // the tree
            add(hash0); // a leaf
            add(hash1); // a leaf...
            add(hash2);
            add(hash3);
            add(hash4);
        }};
        List<byte[]> parentLevel = merkleParentLevel.apply(hashes);
        assertEquals(3, parentLevel.size());
        assertEquals("8b30c5ba100f6f2e5ad1e2a742e5020491240f8eb514fe97c713c31718ad7ecd", HEX.encode(parentLevel.get(0)));
        assertEquals("7f4e6f9e224e20fda0ae4c44114237f97cd35aca38d83081c9bfd41feb907800", HEX.encode(parentLevel.get(1)));
        assertEquals("3ecf6115380c77e8aae56660f5634982ee897351ba906a6837d15ebc3a225df0", HEX.encode(parentLevel.get(2)));
    }

    @Test
    public void testChapter11Example3() {
        List<byte[]> hashes = new ArrayList<>() {{ // the tree
            add(hash0); // a leaf
            add(hash1); // a leaf...
            add(hash2);
            add(hash3);
            add(hash4);
            add(hash5);
            add(hash6);
            add(hash7);
            add(hash8);
            add(hash9);
            add(hash10);
            add(hash11);

        }};
        balanceTree.accept(hashes);
        List<byte[]> currentHashes = hashes;
        while (currentHashes.size() > 1) {
            currentHashes = merkleParentLevel.apply(currentHashes);
        }
        assertEquals(1, currentHashes.size());
        assertEquals("acbcab8bcc1af95d8d563b77d24c3d19b18f1486383d75a5085c4e86c86beed6", HEX.encode(currentHashes.get(0)));
    }

    @Test
    public void testChapter11Exercise3() {
        List<byte[]> hashes = new ArrayList<>() {{ // the tree
            add(hash0); // a leaf
            add(hash1); // a leaf...
            add(hash2);
            add(hash3);
            add(hash4);
            add(hash5);
            add(hash6);
            add(hash7);
            add(hash8);
            add(hash9);
            add(hash10);
            add(hash11);

        }};
        byte[] root = merkleRoot.apply(hashes);
        assertEquals("acbcab8bcc1af95d8d563b77d24c3d19b18f1486383d75a5085c4e86c86beed6", HEX.encode(root));
    }

    private final List<byte[]> txHashes = new ArrayList<>() {{
        add(HEX.decode("42f6f52f17620653dcc909e58bb352e0bd4bd1381e2955d19c00959a22122b2e"));
        add(HEX.decode("94c3af34b9667bf787e1c6a0a009201589755d01d02fe2877cc69b929d2418d4"));
        add(HEX.decode("959428d7c48113cb9149d0566bde3d46e98cf028053c522b8fa8f735241aa953"));
        add(HEX.decode("a9f27b99d5d108dede755710d4a1ffa2c74af70b4ca71726fa57d68454e609a2"));
        add(HEX.decode("62af110031e29de1efcad103b3ad4bec7bdcf6cb9c9f4afdd586981795516577"));
        add(HEX.decode("766900590ece194667e9da2984018057512887110bf54fe0aa800157aec796ba"));
        add(HEX.decode("e8270fb475763bc8d855cfe45ed98060988c1bdcad2ffc8364f783c98999a208"));
    }};

    @Test
    public void testChapter11Example4() {
        // In blocks, hashes must be reversed
        List<byte[]> hashes = new ArrayList<>();
        for (byte[] txHash : txHashes) {
            hashes.add(reverse.apply(txHash));
        }
        // In blocks, merkle root must be reversed
        byte[] blockMerkleRoot = reverse.apply(merkleRoot.apply(hashes));
        assertEquals("654d6181e18e4ac4368383fdc5eead11bf138f9b7ac1e15334e4411b3c4797d9", HEX.encode(blockMerkleRoot));
    }


    @Test
    public void testChapter11Example5() {
        List<byte[][]> emptyMerkleTree = createEmptyMerkleTree.apply(16);

        assertEquals(5, emptyMerkleTree.size());
        assertEquals(1, emptyMerkleTree.get(0).length);
        byte[] rootHash = emptyMerkleTree.get(0)[0];
        assertEquals(HASH_LENGTH, rootHash.length);
        assertEquals(16, emptyMerkleTree.get(4).length);
        byte[] lastLeaf = emptyMerkleTree.get(4)[15];
        assertEquals(HASH_LENGTH, lastLeaf.length);
    }

    @Test
    public void testChapter11Exercise5() {
        List<byte[][]> emptyMerkleTree = createEmptyMerkleTree.apply(27);

        assertEquals(6, emptyMerkleTree.size());
        assertEquals(1, emptyMerkleTree.get(0).length);
        byte[] rootHash = emptyMerkleTree.get(0)[0];
        assertEquals(HASH_LENGTH, rootHash.length);

        assertEquals(4, emptyMerkleTree.get(2).length);
        assertEquals(7, emptyMerkleTree.get(3).length);
        assertEquals(14, emptyMerkleTree.get(4).length);

        assertEquals(27, emptyMerkleTree.get(5).length);
        byte[] lastLeaf = emptyMerkleTree.get(5)[26];
        assertEquals(HASH_LENGTH, lastLeaf.length);
    }

    @Test
    public void testChapter11Example6() {
        List<byte[]> txHashes = new ArrayList<>() {{
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

        MerkleTree tree = new MerkleTree(txHashes.size());
        tree.populateTree(txHashes);
        // log.info(tree.toString());

        assertEquals(1, tree.getNodes(0).size());
        assertEquals(2, tree.getNodes(1).size());
        assertEquals(4, tree.getNodes(2).size());
        assertEquals(8, tree.getNodes(3).size());
        assertEquals(16, tree.getNodes(4).size());

        assertEquals("597c4bafe3832b17cbbabe56f878f4fc2ad0f6a402cee7fa851a9cb205f87ed1", tree.rootHex.get());

        assertNodeLabel(tree, 0, 0, "597c4bafe3832b17cbbabe56f878f4fc2ad0f6a402cee7fa851a9cb205f87ed1");
        assertNodeLabel(tree, 1, 0, "6382df3f3a0b1323ff73f4da50dc5e318468734d6054111481921d845c020b93");
        assertNodeLabel(tree, 2, 2, "7ab01bb63d45a05721c8d1f653fcf76fecc377315b3e60051e4e9fd28eb5b962");
        assertNodeLabel(tree, 3, 7, "8636b7a3935a68e49dd19fc224a8318f4ee3c14791b3388f47f9dc3dee2247d1");
        assertNodeLabel(tree, 4, 15, "f9dbfafc3af3400954975da24eb325e326960a25b87fffe23eef3e7ed2fb610e");
    }

    @Test
    public void testChapter11Example7() {
        List<byte[]> txHashes = new ArrayList<>() {{
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

        MerkleTree tree = new MerkleTree(txHashes.size());
        tree.populateNodes(4, txHashes);

        // This work only if # of leaves is a power of two; it does not handle cases were # of hashes at a level is odd.

        while (tree.isRootEmpty()) {
            if (tree.isLeaf()) {
                tree.up();
            } else {
                byte[] left = tree.getLeftNode();
                byte[] right = tree.getRightNode();
                if (tree.isEmpty.test(left)) {
                    tree.left();
                } else if (tree.isEmpty.test(right)) {
                    tree.right();
                } else {
                    tree.setCurrentNode(tree.merkleParent.apply(left, right));
                    tree.up();
                }
            }
        }
        assertEquals(5, tree.getNodes().size()); // depth

        // log.info(tree.toString());
        assertEquals(1, tree.getNodes(0).size());
        assertEquals(2, tree.getNodes(1).size());
        assertEquals(4, tree.getNodes(2).size());
        assertEquals(8, tree.getNodes(3).size());
        assertEquals(16, tree.getNodes(4).size());

        assertEquals("597c4bafe3832b17cbbabe56f878f4fc2ad0f6a402cee7fa851a9cb205f87ed1", tree.rootHex.get());

        assertNodeLabel(tree, 0, 0, "597c4bafe3832b17cbbabe56f878f4fc2ad0f6a402cee7fa851a9cb205f87ed1");
        assertNodeLabel(tree, 1, 0, "6382df3f3a0b1323ff73f4da50dc5e318468734d6054111481921d845c020b93");
        assertNodeLabel(tree, 2, 2, "7ab01bb63d45a05721c8d1f653fcf76fecc377315b3e60051e4e9fd28eb5b962");
        assertNodeLabel(tree, 3, 7, "8636b7a3935a68e49dd19fc224a8318f4ee3c14791b3388f47f9dc3dee2247d1");
        assertNodeLabel(tree, 4, 15, "f9dbfafc3af3400954975da24eb325e326960a25b87fffe23eef3e7ed2fb610e");
    }

    @Test
    public void testChapter11Example8() {
        List<byte[]> txHashes = new ArrayList<>() {{
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
            // this hash make it an odd # of leaves
            add(HEX.decode("38faf8c811988dff0a7e6080b1771c97bcc0801c64d9068cffb85e6e7aacaf51"));
        }};

        MerkleTree tree = new MerkleTree(txHashes.size());
        tree.populateNodes(5, txHashes);

        while (tree.isRootEmpty()) {
            if (tree.isLeaf()) {
                tree.up();
            } else {
                byte[] left = tree.getLeftNode();
                if (tree.isEmpty.test(left)) {
                    tree.left();
                } else if (tree.rightExists()) {
                    byte[] right = tree.getRightNode();
                    if (tree.isEmpty.test(right)) {
                        tree.right();
                    } else {
                        tree.setCurrentNode(tree.merkleParent.apply(left, right));
                        tree.up();
                    }
                } else {
                    tree.setCurrentNode(tree.merkleParent.apply(left, left)); // duplicate left leaf for balance
                    tree.up();
                }
            }
        }
        assertEquals(6, tree.getNodes().size()); // depth
    }
}
