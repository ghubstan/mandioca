package mandioca.bitcoin.function;

import java.util.function.Function;

public class StorageUnitConversionFunctions {

    public static final Function<Long, String> byteCountString = (n) -> humanReadableByteCount(n, false);

    // From: https://programming.guide/worlds-most-copied-so-snippet.html
    // The strictfp keyword ensures same result on every platform using floating-point variables.
    private static strictfp String humanReadableByteCount(long bytes, boolean siMode) {
        int unit = siMode ? 1000 : 1024;
        long absBytes = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absBytes < unit) return bytes + " B";
        int exp = (int) (Math.log(absBytes) / Math.log(unit));
        long th = (long) (Math.pow(unit, exp) * (unit - 0.05));
        if (exp < 6 && absBytes >= th - ((th & 0xfff) == 0xd00 ? 52 : 0)) exp++;
        String pre = (siMode ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (siMode ? "" : "i");
        if (exp > 4) {
            bytes /= unit;
            exp -= 1;
        }
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}
