package mandioca.bitcoin.network.node;

import mandioca.bitcoin.function.ThrowingBiConsumer;
import mandioca.bitcoin.function.ThrowingConsumer;
import mandioca.bitcoin.network.NetworkType;
import mandioca.bitcoin.network.message.NetworkCommand;
import mandioca.bitcoin.network.message.NetworkMagic;
import mandioca.bitcoin.parser.DataInputStreamParser;
import mandioca.bitcoin.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.network.NetworkConstants.*;
import static mandioca.bitcoin.network.message.NetworkMagic.networkTypeToMagic;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class NetworkEnvelopeParser {

    private static final Logger log = LoggerFactory.getLogger(NetworkEnvelopeParser.class);

    private Parser parser;

    private final Supplier<byte[]> parseMagic = () -> parser.readBytes(MAGIC_LENGTH);
    private final Supplier<byte[]> parseCommand = () -> parser.readBytes(COMMAND_LENGTH);
    private final Supplier<Integer> parsePayloadLength = () -> bytesToInt.apply(reverse.apply(parser.readBytes(Integer.BYTES)));
    private final Supplier<byte[]> parseChecksum = () -> parser.readBytes(PAYLOAD_CHECKSUM_LENGTH);
    private final Function<Integer, byte[]> parsePayload = (l) -> parser.readBytes(l);

    private final ThrowingBiConsumer<NetworkType, byte[]> validateMagic = (networkType, magic) -> {
        NetworkMagic expectedMagic = networkTypeToMagic.apply(networkType);
        if (!Arrays.equals(expectedMagic.getBytes(), magic)) {
            String magicErr = String.format("Magic syntax is incorrect expected: '%s' vs actual: '%s'",
                    expectedMagic.getHex(), HEX.encode(magic));
            throw new RuntimeException(magicErr);
        }
    };
    private final ThrowingConsumer<String> throwChecksumValidationException = (errMsg) -> {
        ChecksumValidationException e = new ChecksumValidationException(errMsg);
        e.fillInStackTrace();
        throw e;
    };
    private final ThrowingConsumer<byte[]> validateMissingPayloadChecksum = (checksum) -> {
        byte[] expected = EMPTY_ARRAY_CHECKSUM;
        if (!Arrays.equals(checksum, expected)) {
            String errMsg = String.format("parsed payload checksum %s does not match absent payload checksum %s",
                    Arrays.toString(checksum), Arrays.toString(expected));
            log.error(errMsg);
            throwChecksumValidationException.accept(errMsg);
        }
    };
    private final ByteBuffer checksumBuffer = ByteBuffer.allocate(PAYLOAD_CHECKSUM_LENGTH);
    // Checksum validation errors during long running, heavy load situations solved
    // by use of a single checksum buffer instead of borrowing/returning buffers.
    private final Function<byte[], byte[]> calculateChecksum = (payload) -> {
        synchronized (checksumBuffer) {
            checksumBuffer.clear();
            checksumBuffer.put(hash256.apply(payload), 0, PAYLOAD_CHECKSUM_LENGTH);
            return checksumBuffer.array();
        }
    };
    private final ThrowingBiConsumer<byte[], byte[]> validateChecksum = (payload, checksum) -> {
        if (isEmptyArray.test(payload)) {
            validateMissingPayloadChecksum.accept(checksum);
        } else {
            byte[] calculated = calculateChecksum.apply(payload);
            if (!Arrays.equals(calculated, checksum)) {
                String errMsg = String.format("calculated payload checksum %s does not match expected checksum %s",
                        Arrays.toString(calculated), Arrays.toString(checksum));
                log.error(errMsg);
                throwChecksumValidationException.accept(errMsg);
            }
        }
    };

    public NetworkEnvelopeParser() {
    }

    public NetworkEnvelopeParser init(ByteArrayInputStream bais) {
        this.parser = new DataInputStreamParser(new DataInputStream(bais));
        return this;
    }

    // TODO Calling this from static NetworkEnvelope.parse()  is not thread safe;
    //  each processor (server and client) thread & client needs it's own parser.
    public NetworkEnvelope[] parseAll(NetworkType networkType) {
        try {
            List<NetworkEnvelope> envelopes = new ArrayList<>();
            while (parser.hasRemaining()) {
                envelopes.add(parse(networkType));
            }
            return envelopes.toArray(new NetworkEnvelope[0]);
        } catch (Exception e) {
            return null;
        } finally {
            parser.closeInputStream();
        }
    }

    // TODO Calling this from static NetworkEnvelope.parse()  is not thread safe;
    //  each processor (server and client) thread & client needs it's own parser.
    public NetworkEnvelope parse(NetworkType networkType) {
        try {
            byte[] magic = parseMagic.get();
            if (isEmptyArray.test(magic)) {
                throw new RuntimeException("Connection reset!");
            }
            validateMagic.accept(networkType, magic);
            byte[] rawCommand = dropTrailingZeros.apply(parseCommand.get());
            int payloadLength = parsePayloadLength.get();
            byte[] checksum = parseChecksum.get();  // There should be a checksum even if there is no payload
            byte[] payload = parser.hasRemaining() ? parsePayload.apply(payloadLength) : emptyArray.apply(0);
            validateChecksum.accept(payload, checksum);
            return new NetworkEnvelope(new NetworkCommand(rawCommand), payload, networkType);
        } catch (Exception e) {
            e.printStackTrace();
            return errorEnvelope(e, networkType);
        } finally {
            parser.closeInputStream();
        }
    }

    private NetworkEnvelope errorEnvelope(Exception e, NetworkType networkType) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return new NetworkEnvelope(
                new NetworkCommand("error".getBytes()),
                sw.toString().getBytes(UTF_8), networkType);
    }

}
