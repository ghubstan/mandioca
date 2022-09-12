package mandioca.bitcoin.util;

import org.junit.Test;

import java.math.BigInteger;

import static mandioca.bitcoin.util.HexUtils.HEX;
import static mandioca.bitcoin.util.VarintUtils.VARINT;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class VarintUtilsTest {

    @Test
    public void testVarintEncodeDecodeForInt_4() {
        byte[] bytes = VARINT.encode(4);
        assertEquals("04", HEX.encode(bytes));
        assertEquals(4, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForInt_10() {
        byte[] bytes = VARINT.encode(10);
        assertEquals("0a", HEX.encode(bytes));
        assertEquals(10, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForInt_100() {
        byte[] bytes = VARINT.encode(100);
        assertEquals("64", HEX.encode(bytes));
        assertEquals(100, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForInt_1_000() {
        byte[] bytes = VARINT.encode(1000);
        assertEquals("fde803", HEX.encode(bytes));
        assertEquals(1000, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForInt_2_000() {
        byte[] bytes = VARINT.encode(2000);
        assertEquals("fdd007", HEX.encode(bytes));
        assertArrayEquals(new byte[]{(byte) 0xfd, (byte) 0xd0, 0x07}, bytes);
        assertEquals(2000, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForInt_10_000() {
        byte[] bytes = VARINT.encode(10_000);
        assertEquals("fd1027", HEX.encode(bytes));
        assertEquals(10_000, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForInt_100_000() {
        byte[] bytes = VARINT.encode(100_000);
        assertEquals("fea0860100", HEX.encode(bytes));
        assertEquals(100_000, VARINT.decode(bytes));
    }


    @Test
    public void testVarintEncodeDecodeForInt_1_000_000() {
        byte[] bytes = VARINT.encode(1_000_000);
        assertEquals("fe40420f00", HEX.encode(bytes));
        assertEquals(1_000_000, VARINT.decode(bytes));
    }


    @Test
    public void testVarintEncodeDecodeForInt_10_000_000() {
        byte[] bytes = VARINT.encode(10_000_000);
        assertEquals("fe80969800", HEX.encode(bytes));
        assertEquals(10_000_000, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForInt_100_000_000() {
        byte[] bytes = VARINT.encode(100_000_000);
        assertEquals("fe00e1f505", HEX.encode(bytes));
        assertEquals(100_000_000, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForInt_1_000_000_000() {
        byte[] bytes = VARINT.encode(1_000_000_000);
        assertEquals("fe00ca9a3b", HEX.encode(bytes));
        assertEquals(1_000_000_000, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForIntMaxValue() {
        byte[] bytes = VARINT.encode(Integer.MAX_VALUE);
        assertEquals("feffffff7f", HEX.encode(bytes));
        assertEquals(Integer.MAX_VALUE, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForLong_4() {
        byte[] bytes = VARINT.encode(4L);
        assertArrayEquals(new byte[]{0x04}, bytes);
        assertEquals(4L, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForLong_10_000_000_000() {
        byte[] bytes = VARINT.encode(10_000_000_000L);
        assertEquals("ff00e40b5402000000", HEX.encode(bytes));
        assertEquals(10_000_000_000L, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForLong_100_000_000_000() {
        byte[] bytes = VARINT.encode(100_000_000_000L);
        assertEquals("ff00e8764817000000", HEX.encode(bytes));
        assertEquals(100_000_000_000L, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForLong_1_000_000_000_000() {
        byte[] bytes = VARINT.encode(1_000_000_000_000L);
        assertEquals("ff0010a5d4e8000000", HEX.encode(bytes));
        assertEquals(1_000_000_000_000L, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForLong_MAX_VALUE() {
        byte[] bytes = VARINT.encode(Long.MAX_VALUE);
        assertEquals("ffffffffffffffff7f", HEX.encode(bytes));
        assertEquals(Long.MAX_VALUE, VARINT.decode(bytes));
    }

    @Test
    public void testVarintEncodeDecodeForBigInteger_4() {
        byte[] bytes = VARINT.encode(BigInteger.valueOf(4));
        assertArrayEquals(new byte[]{0x04}, bytes);
        assertEquals(4, VARINT.decode(bytes));
    }
}
