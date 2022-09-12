package mandioca.bitcoin.network.block;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.network.block.BlockHeaderSerializer.*;
import static mandioca.bitcoin.util.HexUtils.HEX;


/**
 * Block headers are sent in a headers packet in response to a getheaders message.
 * <p>
 * A block header is 80 bytes.
 *
 * @see <a href=" https://en.bitcoin.it/wiki/Block"> https://en.bitcoin.it/wiki/Block</a>
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#Block_Headers">https://en.bitcoin.it/wiki/Protocol_documentation#Block_Headers</a>
 */
@SuppressWarnings("unused")
public class BlockHeader {

    private final Consumer<List<byte[]>> balanceMerkleTree = (hashes) -> {
        if (hashes.size() % 2 == 1) {   // if odd # of leaves, duplicate the last hash for balance
            hashes.add(hashes.get(hashes.size() - 1));
        }
    };
    private final BiFunction<byte[], byte[], byte[]> merkleParent = (l, r) -> hash256.apply(concatenate.apply(l, r));
    private final Function<List<byte[]>, List<byte[]>> merkleParentLevel = (hashes) -> {
        balanceMerkleTree.accept(hashes);
        List<byte[]> parentLevel = new ArrayList<>();
        for (int i = 0; i < hashes.size(); i += 2) { // skip 2
            byte[] parent = merkleParent.apply(hashes.get(i), hashes.get(i + 1));
            parentLevel.add(parent);
        }
        return parentLevel;
    };
    private final Function<List<byte[]>, byte[]> calcMerkleRoot = (hashes) -> {
        List<byte[]> currentHashes = hashes;
        while (currentHashes.size() > 1) {
            currentHashes = merkleParentLevel.apply(currentHashes);
        }
        assert 1 == currentHashes.size() : "there can be only one merkle root";
        return currentHashes.get(0);
    };
    private final BiFunction<byte[][], byte[], Boolean> validateMerkleRoot = (txHashes, merkleRoot) -> {
        List<byte[]> reversedHashes = new ArrayList<>();
        for (byte[] txHash : txHashes) {
            reversedHashes.add(reverse.apply(txHash));
        }
        byte[] calculatedRoot = reverse.apply(calcMerkleRoot.apply(reversedHashes));
        return Arrays.equals(calculatedRoot, merkleRoot);
    };


    // assuming block headers won't be parsed or serialized in parallel
    private static final BlockHeaderParser parser = new BlockHeaderParser();
    private static final BlockHeaderSerializer serializer = new BlockHeaderSerializer();

    protected final byte[] version;           //  4 bytes, little endian, interpreted as int
    protected final byte[] previousBlock;     // 32 bytes, little endian, interpreted as byte array
    protected final byte[] merkleRoot;        // 32 bytes, little endian, interpreted as byte array
    protected final byte[] timestamp;         //  4 bytes, little endian, interpreted int
    protected final byte[] bits;              //  4 bytes, little endian
    protected final byte[] nonce;             //  4 bytes, little endian
    protected final byte[] txHashes;          //  ORDERED tx hashes for merkle root calculation


    //  deserialized fields populated in the pkg private constructor, or lazily
    private int versionInt = -1;
    private byte[] previousBlockBigEndian;
    private byte[] merkleRootBigEndian;
    private int timestampInt = -1;

    public BlockHeader(
            byte[] version,
            byte[] previousBlock,
            byte[] merkleRoot,
            byte[] timestamp,
            byte[] bits,
            byte[] nonce,
            byte[] txHashes) {
        this.version = version;
        this.previousBlock = previousBlock;
        this.merkleRoot = merkleRoot;
        this.timestamp = timestamp;
        this.bits = bits;
        this.nonce = nonce;
        this.txHashes = txHashes;
    }

    /**
     * Secondary constructor that takes deserialized (and big endian) value parameters
     *
     * @param versionInt
     * @param previousBlockBigEndian
     * @param merkleRootBigEndian
     * @param timestampInt
     * @param bits
     * @param nonce
     * @param txHashes
     */
    BlockHeader(int versionInt,
                byte[] previousBlockBigEndian,
                byte[] merkleRootBigEndian,
                int timestampInt,
                byte[] bits,
                byte[] nonce,
                byte[] txHashes) {
        this(serializeVersion.apply(versionInt),
                serializePreviousBlock.apply(previousBlockBigEndian),
                serializeMerkleRoot.apply(merkleRootBigEndian),
                serializeTimestamp.apply(timestampInt),
                serializeBits.apply(bits),
                serializeNonce.apply(nonce),
                txHashes);
        this.versionInt = versionInt;
        this.previousBlockBigEndian = previousBlockBigEndian;
        this.merkleRootBigEndian = merkleRootBigEndian;
        this.timestampInt = timestampInt;
    }

    public static BlockHeader parse(ByteArrayInputStream bais) {
        return parser.init(bais).parse();
    }

    public byte[] serialize() {
        return serializer.init(this).serialize();
    }

    public byte[] hash() {
        return reverse.apply(hash256.apply(serialize()));
    }

    public String getHashHex() {
        return HEX.encode(hash());
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

    @Override
    public String toString() {
        return "BlockHeader{" + "\n" +
                "  hash=" + getHashHex() + "\n" +
                "  getVersionHex=" + getVersionHex() + "\n" +
                ", previousBlock=" + getPreviousBlockHex() + "\n" +
                ", merkleRoot=" + getMerkleRootHex() + "\n" +
                ", timestamp=" + getTimestampLong() + "\n" +
                ", bits=" + getBitsHex() + "\n" +
                ", nonce=" + getNonceInt() +
                '}';
    }

}
