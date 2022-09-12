package mandioca.bitcoin.function;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class LongFunctions {

    // TODO comment out them unused, but don't delete them.
    private static final DecimalFormat INTEGRAL_NUMBER_FORMAT = new DecimalFormat("###,###,###,###");
    public static final Function<Long, String> formatLong = INTEGRAL_NUMBER_FORMAT::format;

    public static final Predicate<Object> isLong = (n) -> n instanceof Long;

    public static final Predicate<Long> isNegative = (n) -> n < 0;
    public static final Predicate<Long> isPositive = (n) -> n > 0;
    public static final Predicate<Long> isZero = (n) -> n == 0;
    public static final Predicate<Long> isNotZero = (n) -> n != 0;
    public static final Predicate<Long> isOne = (n) -> n == 1;
    public static final Predicate<Long> isTwo = (n) -> n == 2;
    public static final Predicate<Long> isThree = (n) -> n == 3;
    public static final BiFunction<Long, Long, Long[]> startEnd = (start, end) -> new Long[]{start, end};
    public static final BiFunction<Long, Long, Boolean> isEqual = Objects::equals;
    public static final BiFunction<Long, Long, Boolean> isLessThan = (a, max) -> a < max;
    public static final BiFunction<Long, Long, Boolean> isLessThanOrEqual = (a, max) -> a <= max;
    public static final BiFunction<Long, Long, Boolean> isGreaterThan = (n, min) -> n > min;
    public static final BiFunction<Long, Long, Boolean> isGreaterThanOrEqual = (n, min) -> n >= min;
    public static final BiFunction<Long, Long[], Boolean> isInRangeInclusive = (n, range) -> (n >= range[0]) && (n <= range[1]);
    public static final BiFunction<Byte, byte[], Boolean> isInRangeExclusive = (n, range) -> (n > range[0]) && (n < range[1]);
    public static Function<byte[], Long> toLong = (b) -> new BigInteger(1, b).longValue();
    public static Function<Long, Long> clone = Long::valueOf;
}
