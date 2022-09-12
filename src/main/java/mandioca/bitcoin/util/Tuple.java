package mandioca.bitcoin.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static mandioca.bitcoin.function.BigIntegerFunctions.wrap;

public class Tuple<X, Y> {

    private final X x;
    private final Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public static Tuple<BigInteger, BigInteger> valueOf(BigInteger x, BigInteger y) {
        return new Tuple<>(x, y);
    }

    public static Tuple<BigInteger, BigInteger> valueOf(int x, int y) {
        return new Tuple<>(wrap.apply(x), wrap.apply(y));
    }

    public static Tuple<BigInteger, BigInteger> valueOf(long x, long y) {
        return new Tuple<>(BigInteger.valueOf(x), BigInteger.valueOf(y));
    }

    @SuppressWarnings("rawtypes")
    public static List<Tuple> getList(Tuple... tuples) {
        final List<Tuple> list = new ArrayList<>();
        Arrays.stream(tuples).forEach(t -> list.add(new Tuple<>(t.getX(), t.getY())));
        return list;
    }

    public X getX() {
        return x;
    }

    public Y getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> that = (Tuple<?, ?>) o;
        return Objects.equals(x, that.x) &&
                Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Tuple{" + "x=" + x + ", y=" + y + '}';
    }
}
