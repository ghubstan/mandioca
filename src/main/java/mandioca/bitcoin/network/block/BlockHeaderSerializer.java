package mandioca.bitcoin.network.block;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Function;

import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.function.EndianFunctions.toLittleEndian;

public class BlockHeaderSerializer {

    final static Function<Integer, byte[]> serializeVersion = (v) -> toLittleEndian.apply(v, Integer.BYTES);
    final static Function<byte[], byte[]> serializePreviousBlock = reverse;
    final static Function<byte[], byte[]> serializeMerkleRoot = reverse;
    final static Function<Integer, byte[]> serializeTimestamp = (t) -> toLittleEndian.apply(t, Integer.BYTES);
    final static Function<byte[], byte[]> serializeBits = reverse;
    final static Function<byte[], byte[]> serializeNonce = reverse;

    private BlockHeader blockHeader;

    public BlockHeaderSerializer() {
    }

    public BlockHeaderSerializer init(BlockHeader blockHeader) {
        this.blockHeader = blockHeader;
        return this;
    }

    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(blockHeader.version);
            baos.write(blockHeader.previousBlock);
            baos.write(blockHeader.merkleRoot);
            baos.write(blockHeader.timestamp);
            baos.write(blockHeader.bits);
            baos.write(blockHeader.nonce);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error serializing block header", e);
        }
    }
}
