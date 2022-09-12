package mandioca.bitcoin.network.message;

import mandioca.bitcoin.network.block.MerkleTree;
import mandioca.bitcoin.stack.BlockingStack;
import mandioca.bitcoin.stack.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.arraycopy;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.NetworkConstants.HASH_LENGTH;
import static mandioca.bitcoin.network.block.MerkleTree.bytesToBitFieldStack;
import static mandioca.bitcoin.network.message.MessageType.MERKLEBLOCK;
import static mandioca.bitcoin.util.HexUtils.HEX;

/**
 * A filtered block containing information needed to verify a transaction is in the merkle tree.
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#filterload.2C_filteradd.2C_filterclear.2C_merkleblock">
 * https://en.bitcoin.it/wiki/Protocol_documentation#filterload.2C_filteradd.2C_filterclear.2C_merkleblock</a>
 */
@SuppressWarnings("unused")
public final class MerkleBlockMessage extends AbstractNetworkMessage implements NetworkMessage {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(MerkleBlockMessage.class);

    private static final MerkleBlockMessageParser parser = new MerkleBlockMessageParser();

    protected final byte[] version;           //  4 bytes, little endian, interpreted as int
    protected final byte[] previousBlock;     // 32 bytes, little endian, interpreted as byte array
    protected final byte[] merkleRoot;        // 32 bytes, little endian, interpreted as byte array
    protected final byte[] timestamp;         //  4 bytes, little endian, interpreted int (limited to 2016)
    protected final byte[] bits;              //  4 bytes, little endian
    protected final byte[] nonce;             //  4 bytes, little endian
    protected final byte[] transactionCount;  //  4 bytes, little endian, interpret as int (indicates # of leaves merkletree)
    protected final byte[] hashCount;         // 1-8 byte varint for the # of hashes to follow
    protected final byte[] hashes;            // 32 bytes * #hashes, little endian, in depth-first order
    protected final byte[] flagBytes;         // 1-8 byte varint for the size of flags (in bytes) to follow
    protected final byte[] flags;             // big endian flag bits, packed per 8 in a byte, least significant bit first, 0 padded to reach full byte size

    //  deserialized fields populated in the pkg private constructor, or lazily
    private int versionInt = -1;
    private byte[] previousBlockBigEndian;
    private byte[] merkleRootBigEndian;
    private int timestampInt = -1;
    private int transactionCountInt = -1;
    private int hashCountInt = -1;
    private int flagBytesInt = -1;

    // Rules for flags (bits)
    //
    // 1.  If the node's value is given in the hashes field, flag bit is not set (0).
    //
    // 2.  If the node is an internal node, and the value needs to be calculated by the light client,
    //     the flag bit is set (1).
    //
    // 3.  If the node is a leaf node, and the transaction is of interest, the flag is set (1) and and node's value
    //     is also given in the hashes field.  These are the items proven to be included in the merkle tree.

    MerkleBlockMessage(
            byte[] version,
            byte[] previousBlock,
            byte[] merkleRoot,
            byte[] timestamp,
            byte[] bits,
            byte[] nonce,
            byte[] transactionCount,
            byte[] hashCount,
            byte[] hashes,
            byte[] flagBytes,
            byte[] flags) {
        this.messageType = MERKLEBLOCK;
        this.version = version;
        this.previousBlock = previousBlock;
        this.merkleRoot = merkleRoot;
        this.timestamp = timestamp;
        this.bits = bits;
        this.nonce = nonce;
        this.transactionCount = transactionCount;
        this.hashCount = hashCount;
        this.hashes = hashes;
        this.flagBytes = flagBytes;
        this.flags = flags;
    }

    public MerkleBlockMessage(MerkleBlockMessageBuilder merkleBlockMessageBuilder) {
        this.messageType = MERKLEBLOCK;
        this.version = merkleBlockMessageBuilder.version;
        this.previousBlock = merkleBlockMessageBuilder.previousBlock;
        this.merkleRoot = merkleBlockMessageBuilder.merkleRoot;
        this.timestamp = merkleBlockMessageBuilder.timestamp;
        this.bits = merkleBlockMessageBuilder.bits;
        this.nonce = merkleBlockMessageBuilder.nonce;
        this.transactionCount = merkleBlockMessageBuilder.transactionCount;
        this.hashCount = merkleBlockMessageBuilder.hashCount;
        this.hashes = merkleBlockMessageBuilder.hashes;
        this.flagBytes = merkleBlockMessageBuilder.flagBytes;
        this.flags = merkleBlockMessageBuilder.flags;
    }

    public static MerkleBlockMessage parse(ByteArrayInputStream bais) {
        return parser.init(bais).parse();
    }

    public boolean isValid() {
        MerkleTree tree = new MerkleTree(getTransactionCountInt()); // tx-count = #-leaves in tree
        Stack flagBitsStack = bytesToBitFieldStack(flags);
        Stack hashesStack = getHashesStack(); // reverse self.hashes for the merkle root calculation
        tree.populateTree(flagBitsStack, hashesStack);
        return Arrays.equals(reverse.apply(tree.root.get()), this.merkleRoot);
    }

    public byte[] getVersion() {
        return version;
    }

    public int getVersionInt() {
        if (versionInt == -1) {
            versionInt = bytesToInt.apply(reverse.apply(version));
        }
        return versionInt;
    }

    public String getVersionHex() {
        return HEX.encode(reverse.apply(version));
    }

    public byte[] getPreviousBlock() {
        return previousBlock;
    }

    public byte[] getPreviousBlockBigEndian() {
        if (previousBlockBigEndian == null) {
            previousBlockBigEndian = reverse.apply(previousBlock);
        }
        return previousBlockBigEndian;
    }

    public String getPreviousBlockHex() {
        return HEX.encode(getPreviousBlockBigEndian());
    }

    public byte[] getMerkleRoot() {
        return merkleRoot;
    }

    public byte[] merkleRootBigEndian() {
        if (merkleRootBigEndian == null) {
            merkleRootBigEndian = reverse.apply(merkleRoot);
        }
        return merkleRootBigEndian;
    }

    public String getMerkleRootHex() {
        return HEX.encode(merkleRootBigEndian());
    }

    public byte[] getTimestamp() {
        return timestamp;
    }

    public int getTimestampInt() {
        if (timestampInt == -1) {
            timestampInt = bytesToInt.apply(reverse.apply(timestamp));
        }
        return timestampInt;
    }

    public long getTimestampLong() {
        return bytesToLong.apply(reverse.apply(timestamp));
    }

    public String getTimestampHex() {
        return HEX.encode(reverse.apply(timestamp));
    }

    public byte[] getBits() {
        return bits;
    }

    public String getBitsHex() {
        return HEX.encode(reverse.apply(bits));
    }

    public byte[] getNonce() {
        return nonce;
    }

    public int getNonceInt() {
        return bytesToInt.apply(reverse.apply(nonce));
    }

    public String getNonceHex() {
        return HEX.encode(reverse.apply(nonce));
    }

    public byte[] getTransactionCount() {
        return transactionCount;
    }

    public int getTransactionCountInt() {
        if (transactionCountInt == -1) {
            transactionCountInt = bytesToInt.apply(transactionCount);
        }
        return transactionCountInt;
    }

    public String getTransactionCountHex() {
        return HEX.encode(transactionCount);
    }

    public byte[] getHashCount() {
        return hashCount;
    }

    public int getHashCountInt() {
        if (hashCountInt == -1) {
            hashCountInt = bytesToInt.apply(reverse.apply(hashCount));
        }
        return hashCountInt;
    }

    public byte[] getHashes() {
        return hashes;
    }

    public List<byte[]> getHashesList() {
        List<byte[]> list = new ArrayList<>();
        int byteCopyIndex = 0;
        for (int i = 0; i < getHashCountInt(); i++) {
            byte[] hash = new byte[HASH_LENGTH];
            arraycopy(hashes, byteCopyIndex, hash, 0, HASH_LENGTH);
            list.add(hash);
            byteCopyIndex += HASH_LENGTH;
        }
        return list;
    }

    public Stack getHashesStack() {
        Stack stack = new BlockingStack(getHashCountInt(), false, "hashes-stack");
        int byteCopyIndex;
        for (int hashIndex = getHashCountInt() - 1; hashIndex >= 0; hashIndex--) {
            byteCopyIndex = HASH_LENGTH * hashIndex;
            byte[] hash = new byte[HASH_LENGTH];
            arraycopy(hashes, byteCopyIndex, hash, 0, HASH_LENGTH);
            stack.push(hash);
        }
        return stack;
    }

    public byte[] getFlagBytes() {
        return flagBytes;
    }

    public String getFlagBytesHex() {
        return HEX.encode(flagBytes);
    }

    public byte[] getFlags() {
        return flags;
    }

    public String getFlagsHex() {
        return HEX.encode(flags);
    }


    @Override
    public byte[] serialize() {
        //noinspection DuplicatedCode
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(version);
            baos.write(previousBlock);
            baos.write(merkleRoot);
            baos.write(timestamp);
            baos.write(bits);
            baos.write(nonce);
            baos.write(transactionCount);
            baos.write(hashCount);
            baos.write(hashes);
            baos.write(flagBytes);
            baos.write(flags);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("error serializing block message", e);
        }
    }

    @Override
    public String toString() {
        return "MerkleBlockMessage{\n" +
                "  version=" + getVersionHex() + "\n" +
                ", previousBlock=" + HEX.encode(previousBlock) + "\n" +
                ", merkleRoot=" + HEX.encode(merkleRoot) + "\n" +
                ", timestamp=" + HEX.encode(timestamp) + "\n" +
                ", bits=" + HEX.encode(bits) + "\n" +
                ", nonce=" + HEX.encode(nonce) + "\n" +
                ", transactionCount=" + HEX.encode(transactionCount) + "\n" +
                ", hashCount=" + HEX.encode(hashCount) + "\n" +
                ", hashes=" + HEX.encode(hashes) + "\n" +
                ", flagBytes=" + HEX.encode(flagBytes) + "\n" +
                ", flags=" + getFlagsHex() + "\n" +
                '}';
    }

    /**
     * MerkleBlockMessageBuilder builder helps avoid bungling use of the MerkleBlockMessage
     * constructor argument list, which is large, of all one type, and not wise to overload.
     */
    @SuppressWarnings("unused")
    public static class MerkleBlockMessageBuilder {
        protected byte[] version;
        protected byte[] previousBlock;
        protected byte[] merkleRoot;
        protected byte[] timestamp;
        protected byte[] bits;
        protected byte[] nonce;
        protected byte[] transactionCount;
        protected byte[] hashCount;
        protected byte[] hashes;
        protected byte[] flagBytes;
        protected byte[] flags;

        public MerkleBlockMessageBuilder() {
        }

        public MerkleBlockMessageBuilder withVersion(byte[] version) {
            this.version = version;
            return this;
        }

        public MerkleBlockMessageBuilder withPreviousBlock(byte[] previousBlock) {
            this.previousBlock = previousBlock;
            return this;
        }

        public MerkleBlockMessageBuilder withMerkleRoot(byte[] merkleRoot) {
            this.merkleRoot = merkleRoot;
            return this;
        }

        public MerkleBlockMessageBuilder withTimestamp(byte[] timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public MerkleBlockMessageBuilder withTimestamp(int timestamp) {
            this.timestamp = reverse.apply(intToBytes.apply(timestamp));
            return this;
        }

        public MerkleBlockMessageBuilder withBits(byte[] bits) {
            this.bits = bits;
            return this;
        }

        public MerkleBlockMessageBuilder withNonce(byte[] nonce) {
            this.nonce = nonce;
            return this;
        }

        public MerkleBlockMessageBuilder withTransactionCount(byte[] transactionCount) {
            this.transactionCount = transactionCount;
            return this;
        }

        public MerkleBlockMessageBuilder withHashCount(byte[] hashCount) {
            this.hashCount = hashCount;
            return this;
        }

        public MerkleBlockMessageBuilder withHashes(byte[] hashes) {
            this.hashes = hashes;
            return this;
        }

        public MerkleBlockMessageBuilder withFlagBytes(byte[] flagBytes) {
            this.flagBytes = flagBytes;
            return this;
        }

        public MerkleBlockMessageBuilder withFlags(byte[] flags) {
            this.flags = flags;
            return this;
        }

        public MerkleBlockMessage build() {
            return new MerkleBlockMessage(this);
        }
    }
}
