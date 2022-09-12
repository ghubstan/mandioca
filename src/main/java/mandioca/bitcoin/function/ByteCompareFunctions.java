package mandioca.bitcoin.function;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import static java.lang.Byte.compareUnsigned;

public class ByteCompareFunctions {
    public static final Predicate<Byte> isZero = (b) -> b == (byte) 0x00;
    public static final Predicate<Byte> isNotZero = (b) -> !isZero.test(b);
    public static final Predicate<Byte> isOne = (b) -> b == (byte) 0x01;
    public static final BiFunction<Byte, Byte, Boolean> isEqual = (a, b) -> compareUnsigned(a, b) == 0;
    public static final BiFunction<Byte, Byte, Boolean> isNotEqual = (a, b) -> compareUnsigned(a, b) != 0;
    public static final BiFunction<Byte, Byte, Boolean> isLessThan = (b, max) -> compareUnsigned(b, max) < 0;
    public static final BiFunction<Byte, Byte, Boolean> isLessThanOrEqual = (b, max) -> compareUnsigned(b, max) <= 0;
    public static final BiFunction<Byte, Byte, Boolean> isGreaterThan = (b, min) -> compareUnsigned(b, min) > 0;
    public static final BiFunction<Byte, Byte, Boolean> isGreaterThanOrEqual = (b, min) -> compareUnsigned(b, min) >= 0;
    public static final BiFunction<Byte, byte[], Boolean> isInRangeInclusive = (b, range) ->
            (compareUnsigned(b, range[0]) >= 0) && (compareUnsigned(b, range[1]) <= 0);
    public static final BiFunction<Byte, byte[], Boolean> isInRangeExclusive = (b, range) ->
            (compareUnsigned(b, range[0]) > 0) && (compareUnsigned(b, range[1]) < 0);
}
