package mandioca.bitcoin.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import static java.lang.System.out;
import static mandioca.bitcoin.function.ByteArrayFunctions.bigIntToUnsignedByteArray;
import static mandioca.bitcoin.function.ByteArrayFunctions.mostSignificantBit;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class ByteArrayUtils {

    public static String toOctetsString(BigInteger number) {
        byte[] bytes = bigIntToUnsignedByteArray.apply(number);
        return toOctetsString(bytes);
    }

    public static String toOctetsString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder hexBuf = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            hexBuf.append(HEX.toPaddedHex.apply(bytes[i]));
            if (i + 1 < bytes.length) {
                hexBuf.append(' ');
            }
        }
        return hexBuf.toString();
    }

    // Copied from https://stackoverflow.com/questions/4703503/read-java-in-as-hex
    public static void dumpHexStream(final InputStream inputStream, final int numberOfColumns) throws IOException {
        long streamPtr = 0;
        while (inputStream.available() > 0) {
            final long col = streamPtr++ % numberOfColumns;
            out.printf("%02x ", inputStream.read());
            if (col == (numberOfColumns - 1)) {
                out.print("\n");
            }
        }
    }

    public static void printMostSignificantBits(byte[] bytes) {
        out.print("MSBs(" + HEX.toPrefixedHexString(bytes) + ") -> ");
        for (byte b : bytes) {
            int msb = mostSignificantBit.apply(b);
            out.print("msb(" + HEX.byteToPrefixedHex.apply(b) + ") = " + msb + " " + (msb == 0 ? "+" : "-") + "\t\t");
        }
        out.println();
    }
}
