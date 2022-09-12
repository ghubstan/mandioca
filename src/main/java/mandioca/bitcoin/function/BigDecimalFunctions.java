package mandioca.bitcoin.function;

import java.math.BigDecimal;
import java.util.function.Predicate;

public class BigDecimalFunctions {

    public static final Predicate<Object> isBigDecimal = (n) -> n instanceof BigDecimal;

    public static final ThrowingBiFunction<Long, Long, BigDecimal> toBigDecimal = (i, f) -> {
        if (i == null) {
            throw new IllegalArgumentException("BigDecimal constructor requires integer part argument");
        }
        if (f == null) {
            throw new IllegalArgumentException("BigDecimal constructor requires fractional argument");
        }
        return new BigDecimal(i + "." + f);
    };

}
