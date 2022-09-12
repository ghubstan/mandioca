package mandioca.bitcoin.network.block;

import mandioca.bitcoin.stack.BlockingStack;
import mandioca.bitcoin.stack.Stack;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class MerkleTreeTestUtils {


    public static Stack getHashesStack(List<byte[]> txHashes) {
        Stack stack = new BlockingStack(txHashes.size(), false, "hashes-stack");
        for (int i = txHashes.size() - 1; i >= 0; i--) {
            stack.push(txHashes.get(i));
        }
        return stack;
    }

    public static void assertNodeLabel(MerkleTree tree, int depth, int index, String hex) {
        assertEquals(hex, tree.nodeHex.apply(depth, index));
    }


}
