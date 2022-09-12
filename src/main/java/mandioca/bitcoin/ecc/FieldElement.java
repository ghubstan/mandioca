package mandioca.bitcoin.ecc;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;
import static mandioca.bitcoin.function.BigIntegerFunctions.*;
import static mandioca.bitcoin.function.LongFunctions.isLong;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class FieldElement implements Field {

    public static final BiFunction<BigInteger, BigInteger, FieldElement> newField = FieldElement::new;

    @SuppressWarnings("CanBeFinal")
    protected BigInteger number;
    @SuppressWarnings("CanBeFinal")
    protected BigInteger prime;

    private final Supplier<Field> squareRoot = () -> {
        if (isThree.test(this.getPrime().mod(FOUR))) {  // Prime p in secp256k1 is such that p % 4 == 3
            return this.power(prime.add(ONE).divide(FOUR));  // JSong's python sqrt():  self**( (P+1)//4 ).
        } else {
            throw new RuntimeException("Not implemented for fields on any curve except secp256k1");
        }
    };
    protected final Supplier<String> fieldNumberNotInRangeError = () ->
            String.format("Number %d not in field range 0 to %d", number, prime.longValue() - 1);
    protected final Supplier<Boolean> fieldNumberIsNotInRange = () ->
            isGreaterThanOrEqual.apply(number, prime) || isNegative.test(number);

    public FieldElement(BigInteger number, BigInteger prime) {
        this.number = number;
        this.prime = prime;
        checkRange();
    }

    public static FieldElement valueOf(Object number, int order) {
        if (isBigInteger.test(number)) {
            return newField.apply((BigInteger) number, BigInteger.valueOf(order));
        }
        if (isInteger.test(number)) {
            return newField.apply(BigInteger.valueOf((Integer) number), BigInteger.valueOf(order));
        }
        if (isLong.test(number)) {
            return newField.apply(BigInteger.valueOf((Long) number), BigInteger.valueOf(order));
        }
        throw new RuntimeException("Cannot create FieldElement with number of type " + number.getClass().getName());
    }

    @Override
    public Field add(Field other) {
        if (other == null) {
            throw new NullPointerException("Cannot operate on null FieldElement");
        }
        if (!prime.equals(other.getPrime())) {
            throw new IllegalArgumentException("Cannot add two numbers in different finite fields");
        }
        BigInteger sum = number.add(other.getNumber());
        return new FieldElement(sum.mod(prime), prime);
    }

    public Field add(Field... other) {
        if (other == null) {
            throw new NullPointerException("Cannot operate on null FieldElement");
        }
        if (other.length == 0) {
            throw new IllegalArgumentException("Cannot operate on empty FieldElement array");
        }
        final BigInteger[] sum = {number};
        Arrays.stream(other).forEach(o -> {
            if (!o.getPrime().equals(prime)) {
                throw new IllegalArgumentException("Cannot add two numbers in different finite fields");
            }
            sum[0] = sum[0].add(o.getNumber());
        });
        return new FieldElement(sum[0].mod(prime), prime);
    }

    @Override
    public Field subtract(Field other) {
        if (other == null) {
            throw new NullPointerException("Cannot operate on null FieldElement");
        }
        if (!this.prime.equals(other.getPrime())) {
            throw new IllegalArgumentException("Cannot subtract two numbers in different finite fields");
        }
        BigInteger difference = number.subtract(other.getNumber());
        return new FieldElement(difference.mod(prime), prime);
    }

    public Field subtract(Field... other) {
        if (other == null) {
            throw new NullPointerException("Cannot operate on null FieldElement");
        }
        if (other.length == 0) {
            throw new IllegalArgumentException("Cannot operate on empty FieldElement array");
        }
        BigInteger[] difference = {number};
        Arrays.stream(other).forEach(o -> {
            if (!o.getPrime().equals(prime)) {
                throw new IllegalArgumentException("Cannot subtract two numbers in different finite fields");
            }
            difference[0] = difference[0].subtract(o.getNumber());
        });
        return new FieldElement(difference[0].mod(prime), prime);
    }

    @Override
    public Field multiply(Field other) {
        if (other == null) {
            throw new NullPointerException("Cannot operate on null FieldElement");
        }
        if (!prime.equals(other.getPrime())) {
            throw new IllegalArgumentException("Cannot add two numbers in different finite fields");
        }
        BigInteger newNumber = number.multiply(other.getNumber()).mod(prime);
        return new FieldElement(newNumber, prime);
    }

    public Field multiply(Field... other) {
        if (other == null) {
            throw new NullPointerException("Cannot operate on null FieldElement");
        }
        if (other.length == 0) {
            throw new IllegalArgumentException("Cannot operate on empty FieldElement array");
        }
        final BigInteger[] product = {number};
        Arrays.stream(other).forEach(o -> {
            if (!o.getPrime().equals(prime)) {
                throw new IllegalArgumentException("Cannot multiply two numbers in different finite fields");
            }
            product[0] = product[0].multiply(o.getNumber());
        });
        return new FieldElement(product[0].mod(prime), prime);
    }

    @Override
    public Field power(BigInteger exponent) {
        // Convert the exponent into something within the 0 to p-2 range, inclusive, and use
        // Fermat's Little Theorem:  n^(p-1) mod p  =  1, where p is prime.
        BigInteger n = exponent.modPow(ONE, prime.subtract(ONE));
        BigInteger num = number.modPow(n, prime);
        return new FieldElement(num, prime);
    }

    public Field power(int exponent) {
        return power(BigInteger.valueOf(exponent));
    }

    @Override
    public Field divide(Field other) {
        if (other == null) {
            throw new NullPointerException("Cannot operate on null FieldElement");
        }
        if (!prime.equals(other.getPrime())) {
            throw new IllegalArgumentException("Cannot divide two numbers in different finite fields");
        }
        BigInteger product = number.multiply(other.getNumber().modPow(prime.subtract(TWO), prime));
        BigInteger result = product.modPow(ONE, prime);
        return new FieldElement(result, prime);
    }

    @Override
    public Field sqrt() {
        return squareRoot.get();
    }

    @Override
    public BigInteger getNumber() {
        return number;
    }

    @Override
    public BigInteger getPrime() {
        return prime;
    }

    @Override
    public Field clone() {
        try {
            return (FieldElement) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldElement that = (FieldElement) o;
        return Objects.equals(this.number, that.number) && Objects.equals(this.prime, that.prime);
    }

    public boolean notEquals(Field o) {
        if (o == null || getClass() != o.getClass()) return true;
        return !this.number.equals(o.getNumber()) || !this.prime.equals(o.getPrime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(number.longValue(), prime.longValue());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{"
                + "\n\t  number = " + HEX.toPrettyHex(number)
                + "\n\t, prime  = " + HEX.toPrettyHex(prime)
                + "\n  }";
    }

    private void checkRange() {
        if (fieldNumberIsNotInRange.get()) {
            throw new IllegalArgumentException(fieldNumberNotInRangeError.get());
        }
    }
}
