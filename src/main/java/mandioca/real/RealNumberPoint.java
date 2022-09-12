package mandioca.real;

import java.math.BigInteger;
import java.util.Objects;

// Does not implement ecc.Point, but helped me begin to learn how field arithmetic works.
// TODO move to test folder?
public class RealNumberPoint implements Cloneable {
    /**
     * Point on x-axis in elliptic curve equation y^2 = x^3 + ax + b
     */
    private final BigInteger x;
    /**
     * Point on y-axis in elliptic curve equation y^2 = x^3 + ax + b
     */
    private final BigInteger y;
    /**
     * Constant a in elliptic curve equation y^2 = x^3 + ax + b
     */
    private final BigInteger a;
    /**
     * Constant b in elliptic curve equation y^2 = x^3 + ax + b
     */
    private final BigInteger b;

    // Constructors

    public RealNumberPoint(BigInteger x, BigInteger y, BigInteger a, BigInteger b) {
        this.x = x;
        this.y = y;
        this.a = a;
        this.b = b;
        checkIsPointOnCurve();
    }

    private RealNumberPoint(BigInteger a, BigInteger b) {
        this(null, null, a, b);
    }

    // Public methods


    public static RealNumberPoint valueOf(BigInteger x, BigInteger y, BigInteger a, BigInteger b) {
        return new RealNumberPoint(x, y, a, b);
    }

    public static RealNumberPoint valueOf(BigInteger a, BigInteger b) {
        return new RealNumberPoint(null, null, a, b);
    }

    public static RealNumberPoint valueOf(long x, long y, long a, long b) {
        return new RealNumberPoint(BigInteger.valueOf(x), BigInteger.valueOf(y), BigInteger.valueOf(a), BigInteger.valueOf(b));
    }

    public static RealNumberPoint valueOf(long a, long b) {
        return new RealNumberPoint(null, null, BigInteger.valueOf(a), BigInteger.valueOf(b));
    }

    public static RealNumberPoint valueOf(int x, int y, int a, int b) {
        return new RealNumberPoint(BigInteger.valueOf(x), BigInteger.valueOf(y), BigInteger.valueOf(a), BigInteger.valueOf(b));
    }

    public static RealNumberPoint valueOf(int a, int b) {
        return new RealNumberPoint(null, null, BigInteger.valueOf(a), BigInteger.valueOf(b));
    }

    public RealNumberPoint add(RealNumberPoint other) {
        if (!a.equals(other.getA()) || !b.equals(other.getB())) {
            String error = String.format("Points %s and %s are not on the curve", this, other);
            throw new IllegalArgumentException(error);
        }
        if (x == null) {
            return other; // this is pt at infinity
        }
        if (other.getX() == null) {
            return this; // other is pt at infinity
        }
        if (x.equals(other.getX()) && !y.equals(other.getY())) {
            return new RealNumberPoint(a, b);
        }
        if (!x.equals(other.getX())) {
            BigInteger m = getSlope((RealNumberPoint) other);
            // From Vieta's Formula, we know -m^2 = -(x1 _ x2 + x3), and can derive x3, then y3
            BigInteger x3 = m.pow(2).subtract(x).subtract(((RealNumberPoint) other).getX()); // x3 = m^2 - x1 - x2
            BigInteger y3 = m.multiply(x.subtract(x3)).subtract(y); // y3 = m(x1 - x3) - y1
            return new RealNumberPoint(x3, y3, a, b);
        }
        if (this.equals(other) && y.equals(BigInteger.ZERO)) { // P1 = P2, y = 0
            return new RealNumberPoint(a, b);  // Tangent line is vertical
        }
        if (this.equals(other)) { // P1 == P2
            BigInteger m = getSlopeOfTangentLine(); // dy/dx = (3x^2 + a) / 2y
            BigInteger x3 = m.pow(2).subtract(BigInteger.TWO.multiply(x)); // x3 = m^2 - 2*x1
            BigInteger y3 = m.multiply(x.subtract(x3)).subtract(y); // y3 = m(x1 - x3) - y1
            return new RealNumberPoint(x3, y3, a, b);
        }
        String error = String.format("Real number point addition not supported for points %s and %s", this, other);
        throw new RuntimeException(error);
    }

    public RealNumberPoint scalarMultiply(BigInteger coefficient) {
        throw new UnsupportedOperationException("scalarMultiply operation not implemented");
    }

    public BigInteger getX() {
        return x;
    }

    public BigInteger getY() {
        return y;
    }

    // Private methods

    public BigInteger getA() {
        return a;
    }

    public BigInteger getB() {
        return b;
    }

    @Override
    public RealNumberPoint clone() throws CloneNotSupportedException {
        return (RealNumberPoint) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RealNumberPoint point = (RealNumberPoint) o;
        return Objects.equals(x, point.x) && Objects.equals(y, point.y) &&
                Objects.equals(a, point.a) && Objects.equals(b, point.b);
    }

    public boolean notEquals(Object o) {
        if (o == null || getClass() != o.getClass()) return true;
        RealNumberPoint point = (RealNumberPoint) o;
        return !Objects.equals(x, point.x) || !Objects.equals(y, point.y)
                || !Objects.equals(a, point.a) || !Objects.equals(b, point.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, a, b);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + "x=" + x + ", y=" + y + ", a=" + a + ", b=" + b + '}';
    }

    /**
     * Throws an IllegalArgumentException if point is not on real number curve described by y^2 != x^3 + ax + b
     */
    private void checkIsPointOnCurve() {
        if (x == null && y == null) {
            return; // skip check if pt is at infinity
        }
        BigInteger leftHandSide = y.pow(2);
        BigInteger rightHandSide = x.pow(3).add(a.multiply(x)).add(b);
        if (!leftHandSide.equals(rightHandSide)) {
            String error = String.format("%d, %d is not on the real number curve", x, y);
            throw new IllegalArgumentException(error);
        }
    }

    /**
     * Returns slope for this point p1, and another point p2, where m = (p2.y - p1.y) / (p2.x - p1.x)
     *
     * @param other
     * @return slope
     */
    private BigInteger getSlope(RealNumberPoint other) {
        return (other.y.subtract(y)).divide(other.x.subtract(x));
    }


    /**
     * Returns slope of the tangent line at this point p, where m = dy/dx = (3x^2 + a) / 2y
     *
     * @return slope
     */
    private BigInteger getSlopeOfTangentLine() {
        // Find slope of line tangent to curve: dy/dx = (3x^2 + a) / 2y
        return ((BigInteger.valueOf(3).multiply(x.pow(2))).add(a))
                .divide(BigInteger.TWO.multiply(y));
    }
}
