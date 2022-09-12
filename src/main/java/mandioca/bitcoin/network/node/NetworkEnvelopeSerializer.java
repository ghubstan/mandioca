package mandioca.bitcoin.network.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.function.Supplier;

import static mandioca.bitcoin.function.ByteArrayFunctions.isEmptyArray;
import static mandioca.bitcoin.function.EndianFunctions.toLittleEndian;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.network.NetworkConstants.*;
import static mandioca.bitcoin.pbbl.ByteBufferLender.borrowBuffer;
import static mandioca.bitcoin.pbbl.ByteBufferLender.returnBuffer;

public class NetworkEnvelopeSerializer {

    private static final Logger log = LoggerFactory.getLogger(NetworkEnvelopeSerializer.class);

    private NetworkEnvelope networkEnvelope;

    private Supplier<ByteBuffer> commandBuffer = () -> {
        ByteBuffer commandBuffer = borrowBuffer.apply(COMMAND_LENGTH).clear();
        if (commandBuffer.capacity() > COMMAND_LENGTH) {
            log.warn("ByteBufferPool provided command buffer that's too large for a  command (capacity={} limit={})",
                    commandBuffer.capacity(), commandBuffer.limit());
        }
        return commandBuffer;
    };
    private final Function<byte[], byte[]> serializeCommand = (c) -> {
        ByteBuffer buffer = commandBuffer.get();
        try {
            return buffer.put(c).array();
        } finally {
            returnBuffer.accept(buffer.clear());
        }
    };
    private final Function<byte[], byte[]> serializePayload = (p) -> {
        if (isEmptyArray.test(p)) {
            return SERIALIZED_EMPTY_PAYLOAD;
        }
        byte[] payloadLength = toLittleEndian.apply(p.length, Integer.BYTES);
        byte[] payloadHash = hash256.apply(p);
        int bufferSize = payloadLength.length + PAYLOAD_CHECKSUM_LENGTH + p.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
        byteBuffer.put(payloadLength);
        byteBuffer.put(payloadHash, 0, PAYLOAD_CHECKSUM_LENGTH);
        byteBuffer.put(p);
        return byteBuffer.array();
    };

    public NetworkEnvelopeSerializer() {
    }

    public NetworkEnvelopeSerializer init(NetworkEnvelope networkEnvelope) {
        this.networkEnvelope = networkEnvelope;
        return this;
    }

    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(networkEnvelope.getMagic().getBytes());
            baos.write(serializeCommand.apply(networkEnvelope.getNetworkCommand().getRaw()));
            baos.write(serializePayload.apply(networkEnvelope.getPayload()));
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error serializing network envelope", e);
        }
    }

}
