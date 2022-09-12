package mandioca.bitcoin.network.node;

// See https://en.bitcoin.it/wiki/Protocol_documentation

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.function.Function;

import static java.lang.System.arraycopy;
import static mandioca.bitcoin.function.ByteArrayFunctions.emptyArray;
import static mandioca.bitcoin.function.ByteArrayFunctions.toByteArrayInputStream;
import static mandioca.bitcoin.network.NetworkType.MAINNET;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NetworkEnvelopeTest {

    private final Function<byte[], ByteArrayInputStream> stream = toByteArrayInputStream;

    // Is this more evidence NetworkEnvelope.parse() should never be called, and everyone needs their own parser?
    // Anser, NO, it is not;  testSerialize() is still breaking when run in a suite.  TODO fix serious but
    private final NetworkEnvelopeParser parser = new NetworkEnvelopeParser();

    @Test
    public void testParse() {
        // From programmingbitcoin/code-ch10/network.py     def test_parse
        String msgHex = "f9beb4d976657261636b000000000000000000005df6e0e2";
        byte[] rawMsg = HEX.decode(msgHex);
        NetworkEnvelope networkEnvelope = NetworkEnvelope.parse(stream.apply(rawMsg), MAINNET);
        assertEquals("verack", networkEnvelope.getNetworkCommand().getAscii());
        assertArrayEquals(emptyArray.apply(0), networkEnvelope.getPayload());

        msgHex = "f9beb4d976657273696f6e0000000000650000005f1a69d2721101000100000000000000bc8f5e5400000000010000000000000000000000000000000000ffffc61b6409208d010000000000000000000000000000000000ffffcb0071c0208d128035cbc97953f80f2f5361746f7368693a302e392e332fcf05050001";
        rawMsg = HEX.decode(msgHex);

        // networkEnvelope = NetworkEnvelope.parse(stream.apply(rawMsg), MAINNET);
        networkEnvelope = parser.init(stream.apply(rawMsg)).parse(MAINNET);

        assertEquals("version", networkEnvelope.getNetworkCommand().getAscii());

        byte[] expectedRawPayload = new byte[rawMsg.length - 24];
        arraycopy(rawMsg, 24, expectedRawPayload, 0, expectedRawPayload.length);
        String expectedPayloadHex = HEX.encode(expectedRawPayload);
        assertEquals(expectedPayloadHex, networkEnvelope.getPayloadAsHex());
    }

    @Test
    public void testSerialize() {
        // TODO fix:  it breaks on byte 10 when run after SimpleNodeDeprecatedManyParallelClientsTest, but works when run alone.
        //  Probably can be fixed by test having it's own parser instance.
        // From programmingbitcoin/code-ch10/network.py     test_serialize
        String msgHex = "f9beb4d976657261636b000000000000000000005df6e0e2";
        byte[] rawMsg = HEX.decode(msgHex);

        // NetworkEnvelope networkEnvelope = NetworkEnvelope.parse(stream.apply(rawMsg), MAINNET);
        NetworkEnvelope networkEnvelope = parser.init(stream.apply(rawMsg)).parse(MAINNET);

        byte[] serialized = networkEnvelope.serialize();
        assertArrayEquals(rawMsg, serialized);

        msgHex = "f9beb4d976657273696f6e0000000000650000005f1a69d2721101000100000000000000bc8f5e5400000000010000000000000000000000000000000000ffffc61b6409208d010000000000000000000000000000000000ffffcb0071c0208d128035cbc97953f80f2f5361746f7368693a302e392e332fcf05050001";
        rawMsg = HEX.decode(msgHex);

        // networkEnvelope = NetworkEnvelope.parse(stream.apply(rawMsg), MAINNET);
        networkEnvelope = parser.init(stream.apply(rawMsg)).parse(MAINNET);

        assertArrayEquals(rawMsg, networkEnvelope.serialize());
    }

}
