package mandioca.bitcoin.util;

import java.util.Objects;

public class Triple<X, Y, Z> {

    private final X x;
    private final Y y;
    private final Z z;

    public Triple(X x, Y y, Z z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public X getX() {
        return x;
    }

    public Y getY() {
        return y;
    }

    public Z getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triple<?, ?, ?> that = (Triple<?, ?, ?>) o;
        return Objects.equals(x, that.x)
                && Objects.equals(y, that.y)
                && Objects.equals(z, that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Tuple{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}
