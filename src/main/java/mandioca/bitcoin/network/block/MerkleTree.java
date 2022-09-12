package mandioca.bitcoin.network.block;

import mandioca.bitcoin.function.TriFunction;
import mandioca.bitcoin.stack.BlockingStack;
import mandioca.bitcoin.stack.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;

import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.util.HexUtils.HEX;


public final class MerkleTree {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(MerkleTree.class);

    private int currentDepth = 0;       // the node ptr is described by depth.index
    private int currentIndex = 0;

    private final int maxDepth;
    private final List<byte[][]> nodes;
    private final boolean debug;

    public MerkleTree(int totalLeaves) {
        this(totalLeaves, false);
    }

    public MerkleTree(int totalLeaves, boolean debug) {
        this.maxDepth = (int) Math.ceil(log2.apply(totalLeaves));
        this.nodes = createEmptyMerkleTree.apply(totalLeaves, maxDepth);
        this.debug = debug;
    }

    public final Function<Integer, Double> log2 = (n) -> Math.log(n) / Math.log(2);

    public final Consumer<List<byte[]>> balanceTree = (hashes) -> {
        if (hashes.size() % 2 == 1) {
            hashes.add(hashes.get(hashes.size() - 1));  // if odd # of leaves, duplicate the last hash for balance
        }
    };

    public final BiFunction<Integer, Integer, String> nodeHex = (level, index) -> HEX.encode(this.getNodes().get(level)[index]);

    public final Supplier<String> rootHex = () -> nodeHex.apply(0, 0);

    public final BiFunction<byte[], byte[], byte[]> merkleParent = (l, r) -> hash256.apply(concatenate.apply(l, r));

    public final Function<List<byte[]>, List<byte[]>> merkleParentLevel = (hashes) -> {
        balanceTree.accept(hashes);
        List<byte[]> parentLevel = new ArrayList<>();
        for (int i = 0; i < hashes.size(); i += 2) { // skip 2
            byte[] parent = merkleParent.apply(hashes.get(i), hashes.get(i + 1));
            parentLevel.add(parent);
        }
        return parentLevel;
    };

    @SuppressWarnings("unused")
    public final Function<List<byte[]>, byte[]> merkleRoot = (hashes) -> {
        List<byte[]> currentHashes = hashes;
        while (currentHashes.size() > 1) {
            currentHashes = merkleParentLevel.apply(currentHashes);
        }
        assert 1 == currentHashes.size() : "there can be only one merkle root";
        return currentHashes.get(0);
    };

    public final TriFunction<Integer, Integer, Integer, Integer> calcNumNodesAtDepth = (depth, maxDepth, totalLeaves) -> {
        @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
        BigDecimal tmp = BigDecimal.valueOf(totalLeaves).divide(BigDecimal.valueOf(2).pow(maxDepth - depth));
        return (int) Math.ceil(tmp.doubleValue());
    };

    public final Function<Integer, byte[][]> createNodes = (numItems) -> {
        byte[][] levelHashes = new byte[numItems][];
        Arrays.fill(levelHashes, new byte[]{});
        return levelHashes;
    };

    public final BiFunction<Integer, Integer, List<byte[][]>> createEmptyMerkleTree = (totalLeaves, maxDepth) -> {
        List<byte[][]> emptyMerkleTree = new ArrayList<>();
        for (int depth = 0; depth < maxDepth + 1; depth++) {
            int numItems = calcNumNodesAtDepth.apply(depth, maxDepth, totalLeaves);
            byte[][] levelHashes = createNodes.apply(numItems);
            emptyMerkleTree.add(levelHashes);
        }
        return emptyMerkleTree;
    };


    // Rules for flags (bits)
    //
    // 1.  If the node's value is given in the hashes field, flag bit is not set (0).
    //
    // 2.  If the node is an internal node, and the value needs to be calculated by the light client,
    //     the flag bit is set (1).
    //
    // 3.  If the node is a leaf node, and the transaction is of interest, the flag is set (1) and and node's value
    //     is also given in the hashes field.  These are the items proven to be included in the merkle tree.
    public void populateTree(Stack flagBits, Stack hashes) {
        int loopCounter = 0;
        // populate until we have the root
        while (isRootEmpty()) {
            if (debug) {
                log.info("LOOP #{}, depth={}}, index={}}", loopCounter, this.currentDepth, this.currentIndex);
            }
            if (isLeaf()) {
                if (debug) {
                    log.info("\t\tis leaf");
                }
                // if leaf, we know this position's hash
                flagBits.pop();  // get the next bit from flag_bits
                setCurrentNode(hashes.pop());  // set the current node in the merkle tree to the next hash
                up(); // go up a level
            } else {
                if (debug) {
                    log.info("\tis not leaf; check left\n");
                }
                byte[] left = getLeftNode();    // get the left hash
                if (isEmpty.test(left)) {
                    // if the next flag bit is 0, the next hash is our current node
                    if (isZero.test(flagBits.pop())) {
                        if (debug) {
                            log.info("\t\tbit=0");
                        }
                        setCurrentNode(hashes.pop()); // set the current node to be the next hash
                        up();  // sub-tree doesn't need calculation, go up
                    } else {
                        if (debug) {
                            log.info("\t\tbit=1");
                        }
                        left();
                    }
                } else if (rightExists()) {
                    if (debug) {
                        log.info("\tright exists");
                    }
                    byte[] right = getRightNode();
                    if (isEmpty.test(right)) {
                        right();
                    } else {
                        setCurrentNode(merkleParent.apply(left, right));
                        up();  // we've completed this sub-tree, go up
                    }
                } else {
                    setCurrentNode(merkleParent.apply(left, left)); // combine the left hash twice for balance
                    up();  // we've completed this sub-tree, go up
                }
            }
            loopCounter++;
        }
        if (!hashes.empty()) {
            throw new RuntimeException("hashes not all consumed, " + hashes.size() + " not inserted into tree");
        }
        while (!flagBits.empty()) {
            if (isOne.test(flagBits.pop())) {
                throw new RuntimeException("flag bits not all consumed");
            }
        }
    }

    public void populateTree(List<byte[]> hashes) {
        populateNodes(maxDepth, hashes);
        for (int depth = maxDepth; depth > 0; depth--) {
            populateParentOfDepth(depth);
        }
    }

    public List<byte[][]> getNodes() {
        return nodes;
    }

    public List<byte[]> getNodes(int depth) {
        if (depth > nodes.size() - 1) {
            throw new IllegalArgumentException("requested nodes at depth " + depth + " tree depth is " + nodes.size());
        }
        return Arrays.asList(nodes.get(depth));
    }

    public void populateParentOfDepth(int depth) {
        List<byte[]> parent = merkleParentLevel.apply(getNodes(depth));
        populateNodes(depth - 1, parent);
    }

    public void populateNodes(int depth, List<byte[]> hashes) {
        byte[][] level = nodes.get(depth);
        for (int i = 0; i < level.length; i++) {
            level[i] = hashes.get(i);
        }
    }

    @SuppressWarnings("unused")
    public void populateNodes(int depth, byte[][] hashes) {
        byte[][] level = nodes.get(depth);
        System.arraycopy(hashes, 0, level, 0, level.length);
    }

    public void up() {
        this.currentDepth -= 1;
        int before = this.currentIndex;
        this.currentIndex /= 2;
        if (debug) {
            log.info("\t\tup depth={}}, idx(before '/2'={}}) after={}}", currentDepth, before, currentIndex);
        }
    }

    public void left() {
        this.currentDepth += 1;
        this.currentIndex *= 2;
    }

    public void right() {
        this.currentDepth += 1;
        this.currentIndex = currentIndex * 2 + 1;
    }

    public final Supplier<byte[]> root = () -> this.getNodes().get(0)[0];

    public void setCurrentNode(byte[] value) {
        if (debug) {
            log.info("\t\tset node[{}}][{}}] = {}", currentDepth, currentIndex, HEX.encode(value));
        }
        this.nodes.get(currentDepth)[currentIndex] = value;
    }

    @SuppressWarnings("unused")
    public byte[] getCurrentNode() {
        return nodes.get(currentDepth)[currentIndex];
    }

    public byte[] getLeftNode() {
        return nodes.get(currentDepth + 1)[currentIndex * 2];
    }

    public byte[] getRightNode() {
        return nodes.get(currentDepth + 1)[currentIndex * 2 + 1];
    }

    public boolean isLeaf() {
        return this.currentDepth == this.maxDepth;
    }

    public boolean rightExists() {
        return nodes.get(currentDepth + 1).length > (currentIndex * 2 + 1);
    }

    public final Predicate<byte[]> isEmpty = (node) -> node.length == 0;

    public final boolean isRootEmpty() {
        return root.get().length == 0;
    }

    @Override
    public String toString() {
        boolean isEmptyNode;
        String emptyNode = "[      ]";
        StringBuilder sb = new StringBuilder("MerkleTree{\n");
        for (int depth = 0; depth < nodes.size(); depth++) {
            byte[][] level = nodes.get(depth);
            for (int index = 0; index < level.length; index++) {
                isEmptyNode = level[index].length == 0;
                String hash = isEmptyNode ? emptyNode : HEX.encode(level[index]);
                if (depth == this.currentDepth && index == this.currentIndex) {
                    sb.append(' ').append('*').append(hash, 0, 8).append(".*");
                } else {
                    sb.append("  ").append(hash, 0, 8).append("... ");
                }
            }
            sb.append("\n");
        }
        sb.append('}');
        return sb.toString();
    }

    public static Stack bytesToBitFieldStack(byte[] bytes) {
        int bitFieldSize = bytes.length * Byte.SIZE;
        Stack stack = new BlockingStack(bitFieldSize);
        for (byte aByte : bytes) {
            for (int i = 0; i < Byte.SIZE; i++) {
                stack.putLast((byte) (aByte & 1));
                aByte >>= 1;
            }
        }
        return stack;
    }
}
