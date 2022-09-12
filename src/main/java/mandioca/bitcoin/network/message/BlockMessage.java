package mandioca.bitcoin.network.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static mandioca.bitcoin.function.ByteArrayFunctions.intToBytes;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.message.MessageType.BLOCK;

/**
 * The block message is sent in response to a getdata message which requests transaction information from a block hash.
 * <p>
 * The SHA256 hash that identifies each block (and which must have a run of 0 bits) is calculated from the first
 * six fields of this structure (version, prev_block, merkle_root, timestamp, bits, nonce, and standard SHA256 padding,
 * making two 64-byte chunks in all) and not from the complete block. To calculate the hash, only two chunks need to
 * be processed by the SHA256 algorithm. Since the nonce field is in the second chunk, the first chunk stays constant
 * during mining and therefore only the second chunk needs to be processed. However, a Bitcoin hash is the hash of the
 * hash, so two SHA256 rounds are needed for each mining iteration. See Block hashing algorithm for details and an example.
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#block">https://en.bitcoin.it/wiki/Protocol_documentation#block</a>
 * @see <a href="https://bitcoindev.network/bitcoin-wire-protocol">https://bitcoindev.network/bitcoin-wire-protocol</a>
 */
@SuppressWarnings("unused")
public class BlockMessage extends AbstractNetworkMessage implements NetworkMessage {
    /**
     * block version information (note, this is signed), 4 bytes little endian (???)
     */
    private final byte[] version;
    /**
     * The hash value of the previous block this particular block references, 32 bytes
     */
    private final byte[] previousBlock;
    /**
     * The reference to a Merkle tree collection which is a hash of all transactions related to this block, 32 bytes
     */
    private final byte[] merkleRoot;
    /**
     * A Unix timestamp recording when this block was created (currently limited to dates before the year 2106), 4 bytes
     */
    private final byte[] timestamp;
    /**
     * The calculated getDifficulty getTarget being used for this block, 4 bytes
     */
    private final byte[] bits;
    /**
     * The nonce used to generate this block;  to allow variations of the header and compute different hashes, 4 bytes
     */
    private final byte[] nonce;
    /**
     * Number of transaction entries, varint
     */
    private final byte[] transactionCount;
    /**
     * Block's transactions, in format of "tx" command, variable length (data type tx[]?)
     */
    private final byte[] transactions;

    private BlockMessage(byte[] version, byte[] previousBlock, byte[] merkleRoot, byte[] timestamp,
                         byte[] bits, byte[] nonce, byte[] transactionCount, byte[] transactions) {
        this.messageType = BLOCK;
        this.version = version;
        this.previousBlock = previousBlock;
        this.merkleRoot = merkleRoot;
        this.timestamp = timestamp;
        this.bits = bits;
        this.nonce = nonce;
        this.transactionCount = transactionCount;
        this.transactions = transactions;
    }

    public BlockMessage(BlockMessageBuilder blockMessageBuilder) {
        this.messageType = BLOCK;
        this.version = blockMessageBuilder.version;
        this.previousBlock = blockMessageBuilder.previousBlock;
        this.merkleRoot = blockMessageBuilder.merkleRoot;
        this.timestamp = blockMessageBuilder.timestamp;
        this.bits = blockMessageBuilder.bits;
        this.nonce = blockMessageBuilder.nonce;
        this.transactionCount = blockMessageBuilder.transactionCount;
        this.transactions = blockMessageBuilder.transactions;
    }

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(version);
            baos.write(previousBlock);
            baos.write(merkleRoot);
            baos.write(timestamp);
            baos.write(bits);
            baos.write(nonce);
            baos.write(transactionCount);
            baos.write(transactions);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("error serializing block message", e);
        }
    }

    /**
     * BlockMessageBuilder builder helps avoid bungling use of the BlockMessage constructor
     * argument list, which is large, of all one type, and not wise to overload.
     */
    @SuppressWarnings("unused")
    public static class BlockMessageBuilder {
        private byte[] version;
        private byte[] previousBlock;
        private byte[] merkleRoot;
        private byte[] timestamp;
        private byte[] bits;
        private byte[] nonce;
        private byte[] transactionCount;
        private byte[] transactions;

        public BlockMessageBuilder() {
        }

        public BlockMessageBuilder withVersion(byte[] version) {
            this.version = version;
            return this;
        }

        public BlockMessageBuilder withPreviousBlock(byte[] previousBlock) {
            this.previousBlock = previousBlock;
            return this;
        }

        public BlockMessageBuilder withMerkleRoot(byte[] merkleRoot) {
            this.merkleRoot = merkleRoot;
            return this;
        }

        public BlockMessageBuilder withTimestamp(byte[] timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public BlockMessageBuilder withTimestamp(int timestamp) {
            this.timestamp = reverse.apply(intToBytes.apply(timestamp));
            return this;
        }

        public BlockMessageBuilder withBits(byte[] bits) {
            this.bits = bits;
            return this;
        }

        public BlockMessageBuilder withNonce(byte[] nonce) {
            this.nonce = nonce;
            return this;
        }

        public BlockMessageBuilder withTransactionCount(byte[] transactionCount) {
            this.transactionCount = transactionCount;
            return this;
        }

        public BlockMessageBuilder withTransactions(byte[] transactions) {
            this.transactions = transactions;
            return this;
        }

        public BlockMessage build() {
            return new BlockMessage(this);
        }
    }
}
