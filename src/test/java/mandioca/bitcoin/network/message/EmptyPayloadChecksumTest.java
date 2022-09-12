package mandioca.bitcoin.network.message;

import org.junit.Test;

import java.nio.ByteBuffer;

import static mandioca.bitcoin.function.ByteArrayFunctions.emptyArray;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.network.NetworkConstants.EMPTY_ARRAY_CHECKSUM;
import static mandioca.bitcoin.network.NetworkConstants.PAYLOAD_CHECKSUM_LENGTH;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

public class EmptyPayloadChecksumTest {

    @Test
    public void testEmptyPayloadChecksum() {
        byte[] checksum = EMPTY_ARRAY_CHECKSUM;
        String checksumHex = HEX.encode(checksum);
        assertEquals("5df6e0e2", checksumHex);

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.clear();
        buffer.put(hash256.apply(emptyArray.apply(0)), 0, PAYLOAD_CHECKSUM_LENGTH);
        checksum = buffer.array();
        checksumHex = HEX.encode(checksum);
        assertEquals("5df6e0e2", checksumHex);
    }
}
