package mandioca.bitcoin.network.message;

import mandioca.bitcoin.MandiocaTest;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static mandioca.bitcoin.function.ByteArrayFunctions.emptyArray;
import static mandioca.bitcoin.function.ByteArrayFunctions.stringToBytes;
import static mandioca.bitcoin.network.NetworkConstants.NONCE_LENGTH;
import static mandioca.bitcoin.network.NetworkConstants.TIMESTAMP_LENGTH;
import static mandioca.bitcoin.network.NetworkProperties.DEFAULT_USERAGENT;
import static mandioca.bitcoin.network.message.VersionMessage.DEFAULT_SERVICES;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

public class VersionMessageTest extends MandiocaTest {

    // @Before
    public void setup() {
        // this is why the test hex in this test is different from book's def test_serialize in ch10/network.py
        System.out.println("DEFAULT_SERVICES  = " + HEX.encode(DEFAULT_SERVICES));
        System.out.println("DEFAULT_USERAGENT = " + DEFAULT_USERAGENT + " HEX " + HEX.encode(stringToBytes.apply(DEFAULT_USERAGENT)));
    }

    @Test
    public void testVersionMessageBuilder() throws UnsupportedEncodingException {
        // From programmingbitcoin/code-ch10/network.py     def test_serialize
        // Original test hex
        // String expectedSerializedHex = "7f11010000000000000000000000000000000000000000000000000000000000000000000000ffff00000000208d000000000000000000000000000000000000ffff00000000208d0000000000000000182f70726f6772616d6d696e67626974636f696e3a302e312f0000000000";
        // Note: this test hex != that in book due to VersionMessage defaults: service (1), default.useragent (/mandioca.0.1/)
        String expectedSerializedHex = "7f11010000000000000000000000000000000000010000000000000000000000000000000000ffff00000000208d010000000000000000000000000000000000ffff00000000208d000000000000000010222f6d616e64696f63613a302e312f220000000000";
        VersionMessage versionMessage = new VersionMessage.VersionMessageBuilder()
                .withTimestamp(emptyArray.apply(TIMESTAMP_LENGTH))
                .withNonce(emptyArray.apply(NONCE_LENGTH))
                .build();
        String serializedHex = HEX.encode(versionMessage.serialize());
        assertEquals(expectedSerializedHex, serializedHex);
    }
}
