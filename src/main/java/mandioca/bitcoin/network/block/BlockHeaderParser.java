package mandioca.bitcoin.network.block;

import mandioca.bitcoin.parser.DataInputStreamParser;
import mandioca.bitcoin.parser.Parser;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.function.Supplier;

import static mandioca.bitcoin.function.ByteArrayFunctions.bytesToInt;
import static mandioca.bitcoin.function.EndianFunctions.reverse;

public class BlockHeaderParser {

    private Parser parser;

    private final Supplier<Integer> parseVersion = () -> bytesToInt.apply(reverse.apply(parser.readBytes(Integer.BYTES)));
    private final Supplier<byte[]> parsePreviousBlock = () -> reverse.apply(parser.readBytes(Integer.SIZE));
    private final Supplier<byte[]> parseMerkleRoot = () -> reverse.apply(parser.readBytes(Integer.SIZE));
    private final Supplier<Integer> parseTimestamp = () -> bytesToInt.apply(reverse.apply(parser.readBytes(Integer.BYTES)));
    private final Supplier<byte[]> parseBits = () -> reverse.apply(parser.readBytes(Integer.BYTES));
    private final Supplier<byte[]> parseNonce = () -> reverse.apply(parser.readBytes(Integer.BYTES));

    public BlockHeaderParser() {
    }

    public BlockHeaderParser init(ByteArrayInputStream bais) {
        this.parser = new DataInputStreamParser(new DataInputStream(bais));
        return this;
    }

    public BlockHeader parse() {
        int version = parseVersion.get();
        byte[] previousBlock = parsePreviousBlock.get();
        byte[] merkleRoot = parseMerkleRoot.get();
        int timestamp = parseTimestamp.get();
        byte[] bits = parseBits.get();
        byte[] nonce = parseNonce.get();
        return new BlockHeader(version, previousBlock, merkleRoot, timestamp, bits, nonce, new byte[]{});
    }

}
