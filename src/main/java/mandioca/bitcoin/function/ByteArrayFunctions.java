package mandioca.bitcoin.function;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.copyOfRange;
import static mandioca.bitcoin.function.BigIntegerFunctions.isBigInteger;
import static mandioca.bitcoin.function.BigIntegerFunctions.isInteger;
import static mandioca.bitcoin.function.LongFunctions.isLong;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class ByteArrayFunctions {

    public static final int MASK_0xFF = 0xff;

    public static final byte[] ZERO_BYTE = new byte[]{(byte) 0x00};
    public static final byte[] ONE_BYTE = new byte[]{(byte) 0x01};

    public static final Function<byte[], ByteArrayInputStream> toByteArrayInputStream = ByteArrayInputStream::new;
    public static final Function<byte[], DataInputStream> toDataInputStream = (bytes) -> new DataInputStream(new ByteArrayInputStream(bytes));

    public static final Function<String, ByteArrayInputStream> hexToByteArrayInputStream = (h) -> new ByteArrayInputStream(HEX.decode(h));
    public static final Function<String, DataInputStream> hexToDataInputStream = (h) -> new DataInputStream(new ByteArrayInputStream(HEX.decode(h)));

    public static final Function<Byte, Integer> byteToInt = (b) -> b & MASK_0xFF;
    public static final Function<byte[], Integer> bytesToInt = (bytes) -> {
        if (bytes.length == 4) {
            return bytes[0] << 24
                    | (bytes[1] & MASK_0xFF) << 16
                    | (bytes[2] & MASK_0xFF) << 8
                    | (bytes[3] & MASK_0xFF);
        } else if (bytes.length == 2) {
            return (bytes[0] & MASK_0xFF) << 8
                    | (bytes[1] & MASK_0xFF);
        } else if (bytes.length == 1) {
            return byteToInt.apply(bytes[0]);
        } else {
            return 0;
        }
    };

    public static final ThrowingFunction<String, byte[]> asciiToBytes = (s) -> s.getBytes(US_ASCII);
    public static final Function<Byte, Integer> mostSignificantBit = (b) -> (b & MASK_0xFF) >> 7;
    public static final BiFunction<byte[], Integer, Boolean> isEqual = (b, n) -> Objects.equals(bytesToInt.apply(b), n);
    public static final BiFunction<byte[], Integer, Boolean> isNotEqual = (b, n) -> !Objects.equals(bytesToInt.apply(b), n);
    public static final Predicate<byte[]> isZero = (b) -> bytesToInt.apply(b) == 0;
    public static final Predicate<byte[]> isNotZero = (b) -> bytesToInt.apply(b) != 0;
    public static final Predicate<byte[]> isOne = (b) -> bytesToInt.apply(b) == 1;
    public static final Function<Integer, byte[]> emptyArray = byte[]::new;
    public static final Predicate<byte[]> isEmptyArray = (bytes) -> bytes.length == 0;

    public static final Function<String, byte[]> stringToBytes = (s) -> s.getBytes(UTF_8);


    public static final ThrowingBiFunction<String, Integer, byte[]> stringToPaddedBytes = (s, l) -> {
        byte[] stringBytes = stringToBytes.apply(s);
        if (l < stringBytes.length) {
            throw new IllegalArgumentException("String argument too long to pad");
        }
        ByteBuffer bb = ByteBuffer.allocate(l);
        bb.put(stringBytes);
        return bb.array();
    };

    public static final BiFunction<byte[], byte[], byte[]> concatenate = (a, b) -> {
        ByteBuffer bb = ByteBuffer.allocate(a.length + b.length);
        bb.put(a);
        bb.put(b);
        return bb.array();
    };

    public static final TriFunction<byte[], Integer, Integer, byte[]> subarray = (array, offset, len) -> {
        byte[] subarray = new byte[len];
        arraycopy(array, offset, subarray, 0, subarray.length);
        return subarray;
    };

    public static final Function<Integer, byte[]> intToBytes = (i) -> new byte[]{
            (byte) ((i >> 24) & MASK_0xFF),
            (byte) ((i >> 16) & MASK_0xFF),
            (byte) ((i >> 8) & MASK_0xFF),
            (byte) ((i) & MASK_0xFF)};

    public static final Function<byte[], byte[]> dropMostSignificantByte = (bytes) -> {
        if (ByteCompareFunctions.isZero.test(bytes[0])) {
            byte[] tmp = new byte[bytes.length - 1];
            arraycopy(bytes, 1, tmp, 0, tmp.length);
            bytes = tmp;
        }
        return bytes;
    };


    public static final Function<byte[], Long> bytesToLong = (bytes) -> {
        // Modified from https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java/29132118#29132118
        long result = 0;
        for (byte value : bytes) { // byte[] b may contain 1, 2, 4, or 8 bytes.
            result <<= Long.BYTES;
            result |= (value & MASK_0xFF);
        }
        return result;
    };

    public static final Function<Long, byte[]> longToBytes = (l) -> {
        byte[] result = new byte[Long.BYTES];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & MASK_0xFF);
            l >>= Long.BYTES;
        }
        return result;
    };

    public static final Function<byte[], byte[]> dropLeadingZeros = (bytes) -> {
        // not called 'truncate' because that word means dropping 'trailing' zeros
        if (ByteCompareFunctions.isNotZero.test(bytes[0])) {
            return bytes; // Should throw exception instead?
        }
        for (int i = 0; i < bytes.length; i++) {
            if (ByteCompareFunctions.isNotZero.test(bytes[i])) {
                return copyOfRange(bytes, i, bytes.length);
            }
        }
        return bytes;
    };

    public static final Function<byte[], byte[]> dropTrailingZeros = (bytes) -> {
        // dropTrailingZeros could be called 'truncate', but name distinguishes it from dropLeadingZeros
        for (int i = 0; i < bytes.length; i++) {
            if (ByteCompareFunctions.isNotZero.test(bytes[i])) {
                continue;
            }
            return copyOfRange(bytes, 0, i);
        }
        return bytes;
    };

    public static final Function<byte[], byte[]> to32ByteArray = (bytes) -> concatenate.apply(new byte[32 - bytes.length], bytes);
    public static final Function<Integer, byte[]> intToUnsignedByteArray = (i) -> dropMostSignificantByte.apply(intToBytes.apply(i));
    public static final Function<BigInteger, byte[]> bigIntToUnsignedByteArray = (i) -> {
        byte[] bytes = dropMostSignificantByte.apply(i.toByteArray());
        return (bytes.length < 32) ? to32ByteArray.apply(bytes) : bytes;
    };

    public static final Function<BigInteger, byte[]> bigIntToTruncatedByteArray = (i) -> dropLeadingZeros.apply(bigIntToUnsignedByteArray.apply(i));
    public static final Function<Integer, byte[]> intToTruncatedByteArray = (i) -> dropLeadingZeros.apply(intToUnsignedByteArray.apply(i));
    public static final Function<Long, byte[]> longToTruncatedByteArray = (l) -> dropLeadingZeros.apply(longToBytes.apply(l));

    static final ThrowingFunction<Number, byte[]> toTruncatedByteArray = (n) -> {
        final byte[] bytes;
        if (isInteger.test(n)) {
            bytes = intToTruncatedByteArray.apply((int) n);
        } else if (isBigInteger.test(n)) {
            bytes = bigIntToTruncatedByteArray.apply((BigInteger) n);
        } else if (isLong.test(n)) {
            bytes = longToTruncatedByteArray.apply((Long) n);
        } else {
            throw new IllegalArgumentException("Converting " + n.getClass().getSimpleName() + " to little endian not supported");
        }
        return bytes;
    };
}
