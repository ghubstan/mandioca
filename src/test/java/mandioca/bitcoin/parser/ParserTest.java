package mandioca.bitcoin.parser;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.util.VarintUtils.VARINT;
import static org.junit.Assert.*;

public class ParserTest {

    @Test
    public void testRead2ByteReadVarintLogic() {
        byte[] byte0 = new byte[]{(byte) 0xfd}; // 0xfd = 253
        assertTrue(VARINT.firstByteIs253.test(byte0));

        byte[] nextTwoBytes = new byte[]{(byte) 0xfd, (byte) 0x00};
        int lenValue = bytesToInt.apply(reverse.apply(nextTwoBytes));
        assertEquals(253, lenValue);

        byte[] varint = concatenate.apply(byte0, nextTwoBytes);
        int totalPayloadLength = byte0.length + nextTwoBytes.length + 253;
        byte[] payload = concatenate.apply(varint, emptyArray.apply(totalPayloadLength - varint.length));

        // Test the parser
        Parser parser = new ByteBufferParser(payload);
        long n = parser.readVarint();
        assertEquals(253, n);
        parser.readBytes((int) n);  // read 253 bytes
        assertFalse(parser.hasRemaining());
    }

    @Test
    public void testRead4ByteReadVarintLogic() {
        byte[] byte0 = new byte[]{(byte) 0xfe}; // 0xfd = 254
        assertTrue(VARINT.firstByteIs254.test(byte0));

        byte[] nextFourBytes = new byte[]{(byte) 0xfe, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        int lenValue = bytesToInt.apply(reverse.apply(nextFourBytes));
        assertEquals(254, lenValue);

        byte[] varint = concatenate.apply(byte0, nextFourBytes);
        int totalPayloadLength = byte0.length + nextFourBytes.length + 254;
        byte[] payload = concatenate.apply(varint, emptyArray.apply(totalPayloadLength - varint.length));

        // Test the parser
        Parser parser = new ByteBufferParser(payload);
        long n = parser.readVarint();
        assertEquals(254, n);
        parser.readBytes((int) n);  // read 254 bytes
        assertFalse(parser.hasRemaining());
    }

    @Test
    public void testRead8ByteReadVarintLogic() {
        byte[] byte0 = new byte[]{(byte) 0xff}; // 0xff = 255
        assertTrue(VARINT.firstByteIs255.test(byte0));

        byte[] nextEightBytes = new byte[]{(byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        long lenValue = bytesToLong.apply(reverse.apply(nextEightBytes));
        assertEquals(255, lenValue);

        byte[] varint = concatenate.apply(byte0, nextEightBytes);
        int totalPayloadLength = byte0.length + nextEightBytes.length + 255;
        byte[] payload = concatenate.apply(varint, emptyArray.apply(totalPayloadLength - varint.length));

        // Test the parser
        Parser parser = new ByteBufferParser(payload);
        long n = parser.readVarint();
        assertEquals(255, n);
        parser.readBytes((int) n);  // read 255 bytes
        assertFalse(parser.hasRemaining());
    }

    @Test
    public void testReadVarintFromStreamParser() {
        Parser parser = null;
        ByteArrayInputStream bais = toByteArrayInputStream.apply(new byte[]{0x04});
        try {
            parser = new DataInputStreamParser(new DataInputStream((bais)));
            long i = parser.readVarint();
            assertEquals(4, i);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (parser != null) {
                parser.closeInputStream();
            }
        }
    }

    @Test
    public void testReadVarintFromByteBufferParser1() {
        Parser parser = new ByteBufferParser(new byte[]{0x04});
        long n = parser.readVarint();
        assertEquals(4, n);
    }
}
