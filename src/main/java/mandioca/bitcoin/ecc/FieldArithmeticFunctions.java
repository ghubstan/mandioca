package mandioca.bitcoin.ecc;

import java.util.function.UnaryOperator;

import static java.math.BigInteger.TWO;
import static mandioca.bitcoin.function.BigIntegerFunctions.THREE;

public class FieldArithmeticFunctions {
    public static final UnaryOperator<Field> square = (f) -> f.power(TWO);
    public static final UnaryOperator<Field> cube = (f) -> f.power(THREE);
}
