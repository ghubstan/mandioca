package mandioca.bitcoin.network.message;

import mandioca.bitcoin.function.ThrowingBiConsumer;
import mandioca.bitcoin.network.block.BlockHeader;
import mandioca.bitcoin.parser.ByteBufferParser;
import mandioca.bitcoin.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static mandioca.bitcoin.function.ByteArrayFunctions.toByteArrayInputStream;
import static mandioca.bitcoin.network.NetworkConstants.BLOCK_HEADER_LENGTH;
import static mandioca.bitcoin.network.message.MessageType.HEADERS;
import static mandioca.bitcoin.pbbl.ByteBufferLender.borrowBuffer;
import static mandioca.bitcoin.pbbl.ByteBufferLender.returnBuffer;
import static mandioca.bitcoin.util.HexUtils.HEX;


/**
 * The headers packet returns block headers in response to a getheaders packet.
 * <p>
 * Note that the block headers in this packet include a transaction count (a var_int, so there can be more than
 * 81 bytes per header) as opposed to the block headers that are hashed by miners.
 * <p>
 * SEE https://bitco.in/en/developer-reference#merkleblock
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#headers">https://en.bitcoin.it/wiki/Protocol_documentation#headers</a>
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#Block_Headers">https://en.bitcoin.it/wiki/Protocol_documentation#Block_Headers</a>
 */
public class HeadersMessage extends AbstractNetworkMessage implements NetworkMessage {

    private static final Logger log = LoggerFactory.getLogger(HeadersMessage.class);

    private static final Function<byte[], ByteArrayInputStream> stream = toByteArrayInputStream;
    /**
     * Number of of block header groups as varint (can be more than 1 if there's a chain split).
     * This implementation can only download headers using a single group (TODO).
     */
    private final byte[] count;
    /**
     * Block headers
     */
    private final byte[] headers;

    public HeadersMessage(byte[] count, byte[] headers) {
        this.messageType = HEADERS;
        this.count = count;
        this.headers = headers;
    }

    public static BlockHeader[] parse(byte[] payload) {
        Parser parser = new ByteBufferParser(payload);
        try {
            List<BlockHeader> headers = parseBlockHeaders(parser);
            checkParserConsumedAll.accept(parser, payload.length);
            return headers.toArray(new BlockHeader[0]);
        } finally {
            parser.closeInputStream();
        }
    }

    @Override
    public byte[] serialize() {
        ByteBuffer byteBuffer = borrowBuffer.apply(count.length + headers.length).clear();
        try {
            byteBuffer.put(count);
            byteBuffer.put(headers);
            byteBuffer.flip();
            return byteBuffer.array();
        } finally {
            returnBuffer.accept(byteBuffer);
        }
    }

    @Override
    public String toString() {
        return "HeadersMessage{" +
                "count=" + HEX.encode(count) +
                ", headers=" + HEX.encode(headers) +
                '}';
    }

    private static List<BlockHeader> parseBlockHeaders(Parser parser) {
        List<BlockHeader> headers = new ArrayList<>();
        int blockCount = (int) parser.readVarint();
        log.debug("parsing {} block header(s)", blockCount);
        for (long i = 1; i <= blockCount; i++) {
            headers.add(parseOneBlockHeader(parser));
        }
        return headers;
    }

    private static BlockHeader parseOneBlockHeader(Parser parser) {
        byte[] headerPayload = parser.readBytes(BLOCK_HEADER_LENGTH);
        BlockHeader blockHeader = BlockHeader.parse(stream.apply(headerPayload));
        // After the 80 byte blockheader payload is a varint representing # of tx,
        // which is always 0, for compatibility with the BlockMessage format.
        if (parser.readVarint() != 0) {
            throw new RuntimeException("number of transactions != 0 in blk header " + blockHeader.getHashHex());
        }
        return blockHeader;
    }

    private static final ThrowingBiConsumer<Parser, Integer> checkParserConsumedAll = (parser, payloadSize) -> {
        if (parser.hasRemaining()) {
            throw new RuntimeException(getParserInfoString(parser, payloadSize));
        }
    };

    private static String getParserInfoString(Parser parser, int payloadSize) throws IOException {
        return String.format("failed to read full %d byte payload during block headers parsing%n", payloadSize)
                + String.format("\tparser.hasRemaining = %d%n", parser.hasRemaining())
                + String.format("\tparser.inputstream.available = %d%n", parser.getInputStream().available())
                + String.format("\tparser.position = %d%n", parser.position())
                + String.format("\tparser.limit = %d%n", parser.limit());
    }
}
