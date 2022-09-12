package mandioca.bitcoin.function;

import org.junit.Test;

import java.math.BigInteger;

import static java.math.BigInteger.ONE;
import static mandioca.bitcoin.function.EndianFunctions.*;
import static mandioca.bitcoin.util.ByteArrayUtils.toOctetsString;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class EndianFunctionsTest {

    @Test
    public void testLittleEndianToInt() {
        byte[] bytes = HEX.decode("99c3980000000000");
        byte[] reversedBytes = reverse.apply(bytes);
        BigInteger expected = new BigInteger("10011545");
        BigInteger actual = new BigInteger(1, reversedBytes);
        assertEquals(expected, actual);

        bytes = HEX.decode("a135ef0100000000");
        reversedBytes = reverse.apply(bytes);
        expected = new BigInteger("32454049");
        actual = new BigInteger(1, reversedBytes);
        assertEquals(expected, actual);
    }

    @Test
    public void testIntToLittleEndian() {
        BigInteger n = ONE;
        byte[] expected = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        byte[] actual = toLittleEndian.apply(n, expected.length);
        assertArrayEquals(expected, actual);

        n = BigInteger.valueOf(10011545);
        expected = new byte[]{(byte) 0x99, (byte) 0xc3, (byte) 0x98, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        actual = toLittleEndian.apply(n, expected.length);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testLittleEndianToLong() {
        byte[] bytes = HEX.decode("99c3980000000000");
        byte[] reversedBytes = reverse.apply(bytes);
        long expected = 10011545L;
        long actual = new BigInteger(1, reversedBytes).longValue();
        assertEquals(expected, actual);

        bytes = HEX.decode("a135ef0100000000");
        reversedBytes = reverse.apply(bytes);
        expected = 32454049L;
        actual = new BigInteger(1, reversedBytes).longValue();
        assertEquals(expected, actual);
    }

    @Test
    public void testLongToLittleEndian() {
        long n = 1L;
        byte[] expected = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        byte[] actual = toLittleEndian.apply(n, expected.length);
        assertArrayEquals(expected, actual);

        n = 10011545L;
        expected = new byte[]{(byte) 0x99, (byte) 0xc3, (byte) 0x98, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        actual = toLittleEndian.apply(n, expected.length);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testToOctets() {
        BigInteger beBigInteger = new BigInteger("45679812", 16);
        // reverse big endian bytes for little endian integer
        BigInteger leBigInteger = reverseBigInt.apply(beBigInteger);
        String expectedBEOctets = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 45 67 98 12";
        assertEquals(expectedBEOctets, toOctetsString(beBigInteger));
        String expectedLEOctets = "12 98 67 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        assertEquals(expectedLEOctets, toOctetsString(leBigInteger));
        // reverse little endian bytes again for big endian integer
        beBigInteger = reverseBigInt.apply(leBigInteger);
        assertEquals(expectedBEOctets, toOctetsString(beBigInteger));
    }
}
