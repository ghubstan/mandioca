package mandioca.bitcoin.function;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Arrays.copyOf;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;

public class EndianFunctions {

    public static final ThrowingFunction<byte[], byte[]> reverse = (bytes) -> {
        if (bytes == null) {
            throw new IllegalStateException("Cannot reverse bytes in null array");
        }
        byte[] reversedArray = copyOf(bytes, bytes.length);
        int i = 0;
        int j = reversedArray.length - 1;
        byte tmp;
        while (j > i) {
            tmp = reversedArray[j];
            reversedArray[j] = reversedArray[i];
            reversedArray[i] = tmp;
            j--;
            i++;
        }
        return reversedArray;
    };

    public static final BiFunction<byte[], Integer, byte[]> resizeAndReverse = (bytes, l) -> {
        ByteBuffer bb = ByteBuffer.allocate(l);
        if (bytes.length < l) {
            bb.put(emptyArray.apply(bb.capacity() - bytes.length));
        }
        bb.put(reverse.apply(bytes));
        return bb.array();
    };

    public static final Function<BigInteger, BigInteger> reverseBigInt = (i) -> {
        byte[] beBytes = bigIntToUnsignedByteArray.apply(i);
        return new BigInteger(1, reverse.apply(beBytes));
    };

    public static final BiFunction<Number, Integer, byte[]> toLittleEndian = (n, l) -> {
        if (n instanceof Integer && (int) n == 0) {
            return emptyArray.apply(l);
        }
        final byte[] beBytes = toTruncatedByteArray.apply(n);
        if (beBytes.length < l) {
            ByteBuffer bb = ByteBuffer.allocate(l);
            bb.put(reverse.apply(beBytes));
            return bb.array();
        } else if (beBytes.length == l) {
            return reverse.apply(beBytes);
        } else {
            throw new RuntimeException("Big endian integer too large to convert to little endian array of length " + l + " of type " + n.getClass().getSimpleName());
        }
    };

    public static final BiFunction<Number, Integer, byte[]> toBigEndian = (n, l) -> {
        final byte[] beBytes = toTruncatedByteArray.apply(n);
        if (beBytes.length < l) {
            ByteBuffer bb = ByteBuffer.allocate(l);
            bb.put(beBytes);
            return bb.array();
        } else if (beBytes.length == l) {
            return beBytes;
        } else {
            throw new RuntimeException("Big endian number too large to convert to big endian array of length " + l);
        }
    };
}
