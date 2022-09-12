package mandioca.bitcoin.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.function.Predicate;

import static mandioca.bitcoin.function.BigIntegerFunctions.HEX_RADIX;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.ByteCompareFunctions.isEqual;
import static mandioca.bitcoin.function.EndianFunctions.toLittleEndian;
import static mandioca.bitcoin.pbbl.ByteBufferLender.borrowBuffer;
import static mandioca.bitcoin.pbbl.ByteBufferLender.returnBuffer;

public final class VarintUtils {

    // SEE https://en.bitcoin.it/wiki/Protocol_documentation#Variable_length_integer

    private static final Predicate<Byte> isLessThan0xfd = (b) -> byteToInt.apply(b) < 0xfd;
    private static final Predicate<Long> isLessThan0x10000 = (l) -> l < Long.valueOf("10000", HEX_RADIX);
    private static final Predicate<Long> isLessThan0x100000000 = (l) -> l < Long.valueOf("100000000", HEX_RADIX);
    private static final BigInteger hex0x10000000000000000 = new BigInteger("10000000000000000", HEX_RADIX);
    private static final Predicate<Long> isLessThan0x10000000000000000 = (l) -> BigInteger.valueOf(l).compareTo(hex0x10000000000000000) < 0;

    public static final Predicate<byte[]> firstByteIs253 = (bytes) -> isEqual.apply(bytes[0], (byte) 0xfd);
    public static final Predicate<byte[]> firstByteIs254 = (bytes) -> isEqual.apply(bytes[0], (byte) 0xfe);
    public static final Predicate<byte[]> firstByteIs255 = (bytes) -> isEqual.apply(bytes[0], (byte) MASK_0xFF);

    public static final VarintUtils VARINT = new VarintUtils();

    public long decode(byte[] vi) {
        // Varint's value range is 0 to 2^64 - 1
        ByteBuffer byteBuffer = null;
        try {
            if (firstByteIs253.test(vi)) {          // if byte == 0xfd, next two bytes are the number
                byteBuffer = borrowBuffer.apply(2);
                byteBuffer.put(vi[2]).put(vi[1]).flip();
                return bytesToLong.apply(byteBuffer.array());
            } else if (firstByteIs254.test(vi)) {   // if byte == 0xfe, next four bytes are the number
                byteBuffer = borrowBuffer.apply(4);
                byteBuffer.put(vi[4]).put(vi[3]).put(vi[2]).put(vi[1]).flip();
                return bytesToLong.apply(byteBuffer.array());
            } else if (firstByteIs255.test(vi)) {   // if byte == 0xff, next eight bytes are the number
                byteBuffer = borrowBuffer.apply(8);
                byteBuffer.put(vi[8]).put(vi[7]).put(vi[6]).put(vi[5]).put(vi[4]).put(vi[3]).put(vi[2]).put(vi[1]).flip();
                return bytesToLong.apply(byteBuffer.array());
            } else { // anything else is the int
                return bytesToLong.apply(vi);
            }
        } finally {
            if (byteBuffer != null) {
                returnBuffer.accept(byteBuffer);
            }
        }
    }

    // TODO optimize this dog's breakfast
    public byte[] encode(int n) {
        if (n == 0) {
            return new byte[]{0x00};
        } else if (n < 0xfd) {
            return intToTruncatedByteArray.apply(n);
        } else if (n < 65535) {     // 0x10000 = 65535
            return concatenate.apply(new byte[]{(byte) 0xfd}, toLittleEndian.apply(n, 2));
        } else {
            return encodeVeryLargeVarint((long) n);
        }
    }


    // Encode a long between 0 and 2^64 - 1 into a byte[]
    public byte[] encode(Long n) {  // From Jimmy Song's helper.py
        if (n == 0) {
            return new byte[]{0x00};
        } else if (n < 0xfd) {
            return longToTruncatedByteArray.apply(n);
        } else if (n < 65535) {     // 0x10000 = 65535
            return concatenate.apply(new byte[]{(byte) 0xfd}, toLittleEndian.apply(n, 2));
        } else {
            return encodeVeryLargeVarint(n);
        }
    }

    // Encode a BigInteger between 0 and 2^64 - 1 into a byte[]
    public byte[] encode(BigInteger n) { // From Jimmy Song's helper.py
        byte[] bigEndianBytes = bigIntToTruncatedByteArray.apply(n);
        if (isLessThan0xfd.test(bigEndianBytes[0])) {
            return bigEndianBytes;
        } else {
            return encodeVeryLargeVarint(n.longValue());
        }
    }


    private byte[] encodeVeryLargeVarint(Long n) {
        // From Jimmy Song's helper.py
        return getBytes(n, isLessThan0x10000, isLessThan0x100000000, isLessThan0x10000000000000000);
    }

    static byte[] getBytes(Long n, Predicate<Long> isLessThan0x10000, Predicate<Long> isLessThan0x100000000, Predicate<Long> isLessThan0x10000000000000000) {
        if (isLessThan0x10000.test(n)) {
            return concatenate.apply(new byte[]{(byte) 0xfd}, toLittleEndian.apply(n.intValue(), 2));
        } else if (isLessThan0x100000000.test(n)) {
            return concatenate.apply(new byte[]{(byte) 0xfe}, toLittleEndian.apply(n.intValue(), 4));
        } else if (isLessThan0x10000000000000000.test(n)) {
            return concatenate.apply(new byte[]{(byte) MASK_0xFF}, toLittleEndian.apply(n, 8));
        } else {
            throw new RuntimeException("n too large " + n);
        }
    }

}
