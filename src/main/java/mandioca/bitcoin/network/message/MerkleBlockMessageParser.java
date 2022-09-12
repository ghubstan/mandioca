package mandioca.bitcoin.network.message;

import mandioca.bitcoin.parser.DataInputStreamParser;
import mandioca.bitcoin.parser.Parser;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.function.Function;
import java.util.function.Supplier;

import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.NetworkConstants.HASH_LENGTH;

final class MerkleBlockMessageParser {

    private Parser parser;

    private final Supplier<Integer> parseVersion = () -> bytesToInt.apply(reverse.apply(parser.readBytes(Integer.BYTES)));
    private final Supplier<byte[]> parsePreviousBlock = () -> parser.readBytes(Integer.SIZE);
    private final Supplier<byte[]> parseMerkleRoot = () -> parser.readBytes(Integer.SIZE);
    private final Supplier<byte[]> parseTimestamp = () -> parser.readBytes(Integer.BYTES);
    private final Supplier<byte[]> parseBits = () -> parser.readBytes(Integer.BYTES);
    private final Supplier<byte[]> parseNonce = () -> parser.readBytes(Integer.BYTES);
    private final Supplier<byte[]> parseTransactionCount = () -> parser.readBytes(Integer.BYTES);
    private final Supplier<Long> parseHashCount = () -> parser.readVarint();
    private final Function<Integer, byte[]> parseHashes = (n) -> parser.readBytes(n * HASH_LENGTH);
    private final Supplier<Long> parseFlagBytes = () -> parser.readVarint();
    private final Function<Integer, byte[]> parseFlags = (n) -> parser.readBytes(n);

    public MerkleBlockMessageParser() {
    }

    public MerkleBlockMessageParser init(ByteArrayInputStream bais) {
        this.parser = new DataInputStreamParser(new DataInputStream(bais));
        return this;
    }

    public MerkleBlockMessage parse() {
        int versionInt = parseVersion.get();
        byte[] version = intToBytes.apply(versionInt);
        byte[] previousBlock = parsePreviousBlock.get();
        byte[] merkleRoot = parseMerkleRoot.get();
        byte[] timestamp = parseTimestamp.get();
        byte[] bits = parseBits.get();
        byte[] nonce = parseNonce.get();
        byte[] transactionCount = parseTransactionCount.get(); // each transaction is 32 bytes, little endian
        Long numHashes = parseHashCount.get();
        byte[] hashCount = longToTruncatedByteArray.apply(numHashes);   // 1-8 byte varint for the # of hashes to follow
        byte[] hashes = parseHashes.apply(numHashes.intValue());        // 32 bytes * #hashes, little endian, in depth-first order
        Long numFlagBytes = parseFlagBytes.get();
        byte[] flagBytes = longToTruncatedByteArray.apply(numFlagBytes); // 1-8 byte varint for the size of flags (in bytes) to follow
        byte[] flags = parseFlags.apply(numFlagBytes.intValue());  // flag bits, packed per 8 in a byte, least significant bit first, 0 padded to reach full byte size
        if (parser.hasRemaining()) {
            throw new RuntimeException("did not consume entire merkleblock payload");
        }
        return new MerkleBlockMessage(
                reverse.apply(version),
                reverse.apply(previousBlock),
                reverse.apply(merkleRoot),
                reverse.apply(timestamp),
                reverse.apply(bits),
                reverse.apply(nonce),
                reverse.apply(transactionCount),
                hashCount,
                hashes,
                flagBytes,
                flags);
    }
}
