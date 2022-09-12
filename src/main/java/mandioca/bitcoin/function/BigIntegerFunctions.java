package mandioca.bitcoin.function;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.math.BigInteger.*;

public class BigIntegerFunctions {

    private static final DecimalFormat INTEGRAL_NUMBER_FORMAT = new DecimalFormat("###,###,###,###");
    public static final Function<BigInteger, String> formatBigInteger = (n) -> INTEGRAL_NUMBER_FORMAT.format(n.intValue());
    public static final Function<Integer, String> formatInt = INTEGRAL_NUMBER_FORMAT::format;

    public static final int HEX_RADIX = 16;
    public static final BigInteger THREE = BigInteger.valueOf(3);
    public static final BigInteger FOUR = BigInteger.valueOf(4);

    public static final Function<Integer, BigInteger> wrap = BigInteger::valueOf;

    public static final Predicate<Object> isBigInteger = (n) -> n instanceof BigInteger;
    public static final Predicate<Object> isInteger = (n) -> n instanceof Integer;
    public static final Predicate<BigInteger> isNegative = (n) -> n.signum() == -1;
    public static final Predicate<BigInteger> isPositive = (n) -> n.compareTo(ZERO) > 0;
    public static final Predicate<BigInteger> isZero = (n) -> n.compareTo(ZERO) == 0;
    public static final Predicate<BigInteger> isNotZero = (n) -> n.compareTo(ZERO) != 0;
    public static final Predicate<BigInteger> isOne = (n) -> n.compareTo(ONE) == 0;
    public static final Predicate<BigInteger> isTwo = (n) -> n.compareTo(TWO) == 0;
    public static final Predicate<BigInteger> isThree = (n) -> n.compareTo(THREE) == 0;

    public static final BiFunction<Integer, Integer, Integer[]> startEnd = (start, end) -> new Integer[]{start, end};
    public static final BiFunction<BigInteger, BigInteger, Boolean> isEqual = (a, b) -> a.compareTo(b) == 0;
    public static final BiFunction<BigInteger, BigInteger, Boolean> isLessThan = (n, max) -> n.compareTo(max) < 0;
    public static final BiFunction<BigInteger, BigInteger, Boolean> isLessThanOrEqual = (n, max) -> n.compareTo(max) <= 0;
    public static final BiFunction<BigInteger, BigInteger, Boolean> isGreaterThan = (n, min) -> n.compareTo(min) > 0;
    public static final BiFunction<BigInteger, BigInteger, Boolean> isGreaterThanOrEqual = (n, min) -> n.compareTo(min) >= 0;
    public static final BiFunction<BigInteger, BigInteger[], Boolean> isInRangeInclusive = (n, range) ->
            (n.compareTo(range[0]) >= 0) && (n.compareTo(range[1]) <= 0);
    public static final BiFunction<Byte, byte[], Boolean> isInRangeExclusive = (n, range) ->
            (n.compareTo(range[0]) > 0) && (n.compareTo(range[1]) < 0);
    public static Function<byte[], BigInteger> toBigInt = (b) -> new BigInteger(1, b);
    public static Function<BigInteger, BigInteger> clone = (i) -> new BigInteger(1, i.toByteArray());
}
