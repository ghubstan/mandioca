package mandioca.bitcoin.script.processing;


import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static mandioca.bitcoin.script.processing.AbstractOpFunctions.decodeElement;
import static mandioca.bitcoin.script.processing.AbstractOpFunctions.encodeNumber;
import static mandioca.bitcoin.script.processing.ScriptConstants.NEGATIVE_ONE;
import static mandioca.bitcoin.script.processing.ScriptConstants.POSITIVE_ZERO;
import static org.junit.Assert.assertArrayEquals;

public class ElementEncoderDecoderTest extends OpCodeFunctionsTest {

    @Test
    public void testEncodePositiveNumbers() {
        assertArrayEquals(POSITIVE_ZERO, encodeNumber(0));
        assertArrayEquals(B0x01, encodeNumber(1));
        assertArrayEquals(B0x02, encodeNumber(2));
        assertArrayEquals(B0x0b, encodeNumber(11));
        assertArrayEquals(B0x0d, encodeNumber(13));
        assertArrayEquals(new byte[]{(byte) 0xb2, 0x01}, encodeNumber(434));
        assertArrayEquals(new byte[]{(byte) 0xf5, 0x01}, encodeNumber(501));
        assertArrayEquals(new byte[]{(byte) 0x6f, 0x03}, encodeNumber(879));
        assertArrayEquals(new byte[]{(byte) 0xf6, 0x03}, encodeNumber(1014));
        assertArrayEquals(new byte[]{(byte) 0x8c, 0x13}, encodeNumber(5004));
        assertArrayEquals(new byte[]{(byte) 0x10, 0x27}, encodeNumber(10000));
    }

    @Test
    public void testEncodeNegativeNumbers() {
        assertArrayEquals(NEGATIVE_ONE, encodeNumber(-1));
        assertArrayEquals(new byte[]{(byte) 0x82}, encodeNumber(-2));
        assertArrayEquals(new byte[]{(byte) 0x83}, encodeNumber(-3));
        assertArrayEquals(new byte[]{(byte) 0x84}, encodeNumber(-4));
        assertArrayEquals(new byte[]{(byte) 0x8b}, encodeNumber(-11));
        assertArrayEquals(new byte[]{(byte) 0xb2, (byte) 0x81}, encodeNumber(-434));
        assertArrayEquals(new byte[]{(byte) 0xf5, (byte) 0x81}, encodeNumber(-501));
        assertArrayEquals(new byte[]{(byte) 0x6f, (byte) 0x83}, encodeNumber(-879));
        assertArrayEquals(new byte[]{(byte) 0xf6, (byte) 0x83}, encodeNumber(-1014));
        assertArrayEquals(new byte[]{(byte) 0x8c, (byte) 0x93}, encodeNumber(-5004));
        assertArrayEquals(new byte[]{(byte) 0x10, (byte) 0xa7}, encodeNumber(-10000));
    }

    @Test
    public void testDecodePositiveEncodings() {
        assertEquals(0, decodeElement(POSITIVE_ZERO));
        assertEquals(1, decodeElement(B0x01));
        assertEquals(2, decodeElement(B0x02));
        assertEquals(11, decodeElement(B0x0b));
        assertEquals(13, decodeElement(B0x0d));
        assertEquals(434, decodeElement(new byte[]{(byte) 0xb2, 0x01}));
        assertEquals(501, decodeElement(new byte[]{(byte) 0xf5, 0x01}));
        assertEquals(879, decodeElement(new byte[]{(byte) 0x6f, 0x03}));
        assertEquals(1014, decodeElement(new byte[]{(byte) 0xf6, 0x03}));
        assertEquals(5004, decodeElement(new byte[]{(byte) 0x8c, 0x13}));
        assertEquals(10000, decodeElement(new byte[]{(byte) 0x10, 0x27}));
    }

    @Test
    public void testDecodeNegativeEncodings() {
        assertEquals(-1, decodeElement(NEGATIVE_ONE));
        assertEquals(-2, decodeElement(new byte[]{(byte) 0x82}));
        assertEquals(-3, decodeElement(new byte[]{(byte) 0x83}));
        assertEquals(-4, decodeElement(new byte[]{(byte) 0x84}));
        assertEquals(-11, decodeElement(new byte[]{(byte) 0x8b}));
        assertEquals(-434, decodeElement(new byte[]{(byte) 0xb2, (byte) 0x81}));
        assertEquals(-501, decodeElement(new byte[]{(byte) 0xf5, (byte) 0x81}));
        assertEquals(-879, decodeElement(new byte[]{(byte) 0x6f, (byte) 0x83}));
        assertEquals(-1014, decodeElement(new byte[]{(byte) 0xf6, (byte) 0x83}));
        assertEquals(-5004, decodeElement(new byte[]{(byte) 0x8c, (byte) 0x93}));
        assertEquals(-10000, decodeElement(new byte[]{(byte) 0x10, (byte) 0xa7}));
    }
}
