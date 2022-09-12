package mandioca.bitcoin.network.message;

import mandioca.bitcoin.function.ThrowingConsumer;
import mandioca.bitcoin.parser.ByteBufferParser;
import mandioca.bitcoin.parser.Parser;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.function.Function;

import static mandioca.bitcoin.function.BigIntegerFunctions.HEX_RADIX;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.NetworkConstants.*;
import static mandioca.bitcoin.network.message.MessageType.GETHEADERS;
import static mandioca.bitcoin.network.message.VersionMessage.DEFAULT_VERSION;
import static mandioca.bitcoin.pbbl.ByteBufferLender.borrowBuffer;
import static mandioca.bitcoin.pbbl.ByteBufferLender.returnBuffer;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static mandioca.bitcoin.util.VarintUtils.VARINT;

/**
 * Return a headers packet containing the headers of blocks starting right after the last known hash in the block
 * locator object, up to hash_stop or 2000 blocks, whichever comes first.
 * <p>
 * To receive the next block headers, one needs to issue getheaders again with a new block locator object.
 * Keep in mind that some clients may provide headers of blocks which are invalid if the block locator object contains
 * a hash on the invalid branch.
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#getheaders">https://en.bitcoin.it/wiki/Protocol_documentation#getheaders</a>
 * @see <a href="https://bitcoindev.network/bitcoin-wire-protocol">https://bitcoindev.network/bitcoin-wire-protocol</a>
 */
public final class GetHeadersMessage extends AbstractNetworkMessage implements NetworkMessage {

    // TODO this class is almost identical to GetBlocksMessage (refactor after much testing)

    private static final byte[] DEFAULT_HASH_COUNT = VARINT.encode(1);

    private final ThrowingConsumer<byte[]> validateBlockLocatorBytes = (b) -> {
        if (b == null || b.length != HASH_LENGTH) {
            throw new IllegalArgumentException("byte[] blockLocator must have length 32");
        }
    };
    private final ThrowingConsumer<String> validateBlockLocatorHex = (h) -> {
        if (h == null || h.length() != (2 * HASH_LENGTH)) {
            throw new IllegalArgumentException("hex blockLocator must have length 64");
        }
    };
    private final Function<String, byte[]> hashHexToLittleEndianBytes = (h) -> {
        validateBlockLocatorHex.accept(h);
        BigInteger n = new BigInteger(h, HEX_RADIX);
        if (n.equals(BigInteger.ZERO)) {
            return ZERO_HASH;
        } else {
            byte[] bytes = n.toByteArray();
            return bytes.length < 32 ? reverse.apply(to32ByteArray.apply(bytes)) : reverse.apply(bytes);
        }
    };
    private final Function<byte[], String> hashLittleEndianBytesToHex = (b) -> {
        validateBlockLocatorBytes.accept(b);
        return HEX.encode(reverse.apply(b));
    };

    /**
     * protocol version (70015) as 4 byte little-endian
     */
    private final byte[] version;
    /**
     * number of block locator hash entries as a varint
     */
    private final byte[] hashCount;
    /**
     * starting block locator object (32+ bytes); newest back to genesis block (dense to start, but then sparse)
     */
    private final byte[] blockLocator;
    /**
     * hash (32 bytes) of the last desired block header; set to zero to get as many blocks as possible (2000)
     */
    private final byte[] hashStop;
    private final boolean hasZeroHashStop;

    public GetHeadersMessage(byte[] blockLocator) {
        this(DEFAULT_VERSION, DEFAULT_HASH_COUNT, blockLocator, null);
    }

    public GetHeadersMessage(String blockLocator) {
        this(DEFAULT_VERSION, DEFAULT_HASH_COUNT, blockLocator, null);
    }

    public GetHeadersMessage(byte[] blockLocator, byte[] hashStop) {
        this(DEFAULT_VERSION, DEFAULT_HASH_COUNT, blockLocator, hashStop);
    }

    public GetHeadersMessage(String blockLocator, String hashStop) {
        this(DEFAULT_VERSION, DEFAULT_HASH_COUNT, blockLocator, hashStop);
    }

    public GetHeadersMessage(byte[] version, byte[] hashCount, byte[] blockLocator, byte[] hashStop) {
        validateBlockLocatorBytes.accept(blockLocator);
        this.messageType = GETHEADERS;
        this.version = version;
        this.hashCount = hashCount;
        this.blockLocator = blockLocator;
        this.hasZeroHashStop = hashStop == null;
        this.hashStop = this.hasZeroHashStop ? ZERO_HASH : hashStop;
    }

    public GetHeadersMessage(byte[] version, byte[] hashCount, String blockLocator, String hashStop) {
        validateBlockLocatorHex.accept(blockLocator);
        this.messageType = GETHEADERS;
        this.version = version;
        this.hashCount = hashCount;

        this.blockLocator = hashHexToLittleEndianBytes.apply(blockLocator);    // hex to little endian byte[]

        this.hasZeroHashStop = hashStop == null;
        this.hashStop = this.hasZeroHashStop ? ZERO_HASH : hashHexToLittleEndianBytes.apply(hashStop);
    }

    public static GetHeadersMessage parse(byte[] payload) {
        Parser parser = new ByteBufferParser(payload);
        try {
            byte[] versionBytes = parser.readBytes(VERSION_LENGTH);
            long hashCount = parser.readVarint();
            byte[] blockLocator = parser.readBytes(HASH_LENGTH);
            byte[] hashStop = parser.readBytes(HASH_LENGTH);
            return new GetHeadersMessage(versionBytes, VARINT.encode(hashCount), blockLocator, hashStop);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            parser.closeInputStream();
        }
    }

    public int getVersionAsInt() {
        return bytesToInt.apply(reverse.apply(version));
    }

    public long getHashCountAsLong() {
        byte[] varintBytes = subarray.apply(hashCount, 0, hashCount.length - 1);
        return bytesToInt.apply(varintBytes);
    }

    public String blockLocatorBigEndianHex() {
        return hashLittleEndianBytesToHex.apply(blockLocator);
    }

    public String hashStopBigEndianHex() {
        return hashLittleEndianBytesToHex.apply(hashStop);
    }

    public boolean hasZeroHashStop() {
        return this.hasZeroHashStop;
    }

    @Override
    public byte[] serialize() {
        int bufferSize = version.length + hashCount.length + blockLocator.length + hashStop.length;
        ByteBuffer byteBuffer = borrowBuffer.apply(bufferSize).clear();
        try {
            byteBuffer.put(version);
            byteBuffer.put(hashCount);
            byteBuffer.put(blockLocator);
            byteBuffer.put(hashStop);
            byteBuffer.flip();
            return byteBuffer.array();
        } finally {
            returnBuffer.accept(byteBuffer);
        }
    }

    @Override
    public String toString() {
        return "GetHeadersMessage{" +
                "version=" + getVersionAsInt() +
                ", hashCount=" + getHashCountAsLong() +
                ", reverse(blockLocator)=" + hashLittleEndianBytesToHex.apply(blockLocator) +
                ", reverse(hashStop)=" + hashLittleEndianBytesToHex.apply(hashStop) +
                '}';
    }
}
