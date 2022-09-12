package mandioca.bitcoin.network.node;

import org.junit.Test;

import static mandioca.bitcoin.network.NetworkProperties.NETWORK;
import static mandioca.bitcoin.network.message.MessageFactory.getNonce;
import static mandioca.bitcoin.network.node.HandshakeResponses.isHandshakeResponse;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class HandshakeResponsesSerializationTest {

    private final NetworkEnvelopeHelper envelopeHelper = new NetworkEnvelopeHelper();

    @Test
    public void testHandshakeSerialization() {
        HandshakeResponses hr = new HandshakeResponses(
                envelopeHelper.versionPayload.apply(getNonce(), NETWORK),
                envelopeHelper.verackPayload.apply(NETWORK));
        byte[] hrBytes = hr.serializeInternal();
        assertTrue(isHandshakeResponse.test(hrBytes));

        HandshakeResponses deserializedHr = HandshakeResponses.parse(hrBytes);
        assertArrayEquals(hr.getVersionPayload(), deserializedHr.getVersionPayload());
        assertArrayEquals(hr.getVerackPayload(), deserializedHr.getVerackPayload());
    }
}
