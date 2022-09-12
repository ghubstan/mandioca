package mandioca.bitcoin.ecc;

import mandioca.bitcoin.function.QuinqueFunction;
import mandioca.bitcoin.function.TriFunction;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import static java.math.BigInteger.TWO;
import static java.math.BigInteger.ZERO;
import static mandioca.bitcoin.ecc.FieldArithmeticFunctions.cube;
import static mandioca.bitcoin.ecc.FieldArithmeticFunctions.square;
import static mandioca.bitcoin.ecc.FieldElement.newField;
import static mandioca.bitcoin.function.BigIntegerFunctions.*;

public class EllipticCurvePoint implements Point {
    /**
     * Point on x-axis in elliptic curve equation y^2 = x^3 + ax + b
     */
    protected final Field x;
    /**
     * Point on y-axis in elliptic curve equation y^2 = x^3 + ax + b
     */
    protected final Field y;
    /**
     * Constant a in elliptic curve equation y^2 = x^3 + ax + b
     */
    protected final Field a;
    /**
     * Constant b in elliptic curve equation y^2 = x^3 + ax + b
     */
    protected final Field b;

    // EllipticCurvePointFunctions idea sucks because the caller has to prefix all calls with 'func.*'.
    // Keep it around in docs folder as a reminder, then get rid of it after considering alternatives.
    // private final EllipticCurvePointFunctions pointFunctions;
    //
    // This class is really not that ugly, just need to copy Fields x,y,a,b to top of file and not reformat members.

    static final QuinqueFunction<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, EllipticCurvePoint>
            newPoint = (x, y, a, b, p) ->
            new EllipticCurvePoint(newField.apply(x, p), newField.apply(y, p), newField.apply(a, p), newField.apply(b, p));

    static final TriFunction<BigInteger, BigInteger, BigInteger, EllipticCurvePoint>
            newIdentity = (a, b, p) -> getIdentity(newField.apply(a, p), newField.apply(b, p));

    protected final Function<Point, String> invalidPointError = (p) ->
            String.format("%s %s and %s is not on the elliptic curve", this.getClass().getSimpleName(), p.getX(), p.getY());

    protected final BiFunction<Field, Field, String> invalidCoordinatesError = (x, y) ->
            String.format("%s %s and %s is not on the elliptic curve", this.getClass().getSimpleName(), x, y);

    protected final Function<Point, String> pointAdditionNotSupported = (other) ->
            String.format("Elliptic curve point addition not supported for points %s and %s", this, other);

    // following 2 funcs have been copied
    protected final Function<Point, Field> slope = (p) -> (p.getY().subtract(this.getY()))
            .divide(p.getX().subtract(this.getX()));

    protected final BooleanSupplier isIdentity = () -> this.getX() == null && this.getY() == null;

    protected final Function<Point, Field> slopeOfTangent = (p) -> {
        // slope of line tangent to curve is dy/dx = (3x^2 + a) / 2y
        Field dy = new FieldElement(THREE, this.getX().getPrime())
                .multiply(square.apply(this.getX()))
                .add(this.getA());
        Field dx = new FieldElement(TWO, this.getX().getPrime())
                .multiply(this.getY());
        return dy.divide(dx);
    };

    protected final Function<Point, Boolean> isNotOnCurve = (other) ->
            !this.getA().equals(other.getA()) || !this.getB().equals(other.getB());

    private final Function<Point, Point> pointDerivedFromSlope = (other) -> {
        Field m = slope.apply(other), x = this.getX(), y = this.getY(), a = this.getA(), b = this.getB();
        // Vieta's formula says -m^2 = -(x1 - x2 + x3), so we can derive x3, then y3
        Field x3 = square.apply(m).subtract(x).subtract(other.getX()); // x3 = m^2 - x1 - x2
        Field y3 = m.multiply(x.subtract(x3)).subtract(y); // y3 = m(x1 - x3) - y1
        return new EllipticCurvePoint(x3, y3, a, b);
    };

    private final Function<Point, Point> pointDerivedFromSlopeOfTangent = (other) -> {
        Field m = slopeOfTangent.apply(other), x = this.getX(), y = this.getY(), a = this.getA(), b = this.getB();
        Field x3 = square.apply(m).subtract(new FieldElement(TWO, x.getPrime()).multiply(x)); // x3 = m^2 - 2x1
        Field y3 = m.multiply(x.subtract(x3)).subtract(y); // y3 = m(x1 - x3) - y1
        return new EllipticCurvePoint(x3, y3, a, b);
    };

    private final Function<BigInteger, Point> doubleAndAdd = (c) -> {
        BigInteger coefficient = clone.apply(c);  // don't change c arg's value
        Point current = this.clone();
        Point result = getIdentity(this.getA(), this.getB());  // start at identity
        while (isNotZero.test(coefficient)) {
            if (coefficient.testBit(0)) {
                result = result.add(current);
            }
            current = current.add(current);
            coefficient = coefficient.shiftRight(1);
        }
        return result;
    };

    public EllipticCurvePoint(Field x, Field y, Field a, Field b) {
        this.x = x;
        this.y = y;
        this.a = a;
        this.b = b;
        checkIsPointOnCurve();
    }

    private static EllipticCurvePoint getIdentity(Field a, Field b) {
        return new EllipticCurvePoint(null, null, a, b);
    }

    @Override
    public Point add(Point other) {
        if (isNotOnCurve.apply(other)) {
            throw new IllegalArgumentException(invalidPointError.apply(other));
        }
        if (x == null) {
            return other;   // case: this pt is identity, 0+q = q
        }
        if (other.getX() == null) {
            return this;    // case: other pt is identity, p+0=p
        }
        if (x.equals(other.getX()) && !y.equals(other.getY())) {
            return getIdentity(a, b);  // case: px=qx, py!=qy, p+q=identity
        }
        if (!x.equals(other.getX())) {
            // case: px!=qx, p,q has slope m=(py-qy)/(px-qx), intersection of line is r (rx, ry), and p+q=-r
            return pointDerivedFromSlope.apply(other);
        }
        if (this.equals(other) && y.getNumber().equals(ZERO)) {
            return getIdentity(a, b); // case: P1 == P2, and tangent line is vertical (y=0)
        }
        if (this.equals(other)) {
            return pointDerivedFromSlopeOfTangent.apply(other);  // case: p1 == p2
        }
        throw new RuntimeException(pointAdditionNotSupported.apply(other));
    }

    @Override
    public Point scalarMultiply(BigInteger coefficient) {
        if (isNegative.test(coefficient)) {
            throw new IllegalArgumentException("Cannot perform scalar multiplication for negative coefficient");
        }
        // if (equalsZero.apply(coefficient)) {throw new IllegalArgumentException("Multiply by zero error");}
        if (isOne.test(coefficient)) {
            return this;
        }
        return doubleAndAdd.apply(coefficient);
    }

    @Override
    public Field getX() {
        return x;
    }

    @Override
    public Field getY() {
        return y;
    }

    @Override
    public Field getA() {
        return a;
    }

    @Override
    public Field getB() {
        return b;
    }

    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    @Override
    protected EllipticCurvePoint clone() {
        try {
            return (EllipticCurvePoint) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EllipticCurvePoint point = (EllipticCurvePoint) o;
        return Objects.equals(x, point.x) && Objects.equals(y, point.y) &&
                Objects.equals(a, point.a) && Objects.equals(b, point.b);
    }

    public boolean notEquals(Object o) {
        if (o == null || getClass() != o.getClass()) return true;
        EllipticCurvePoint point = (EllipticCurvePoint) o;
        return !Objects.equals(x, point.x) || !Objects.equals(y, point.y)
                || !Objects.equals(a, point.a) || !Objects.equals(b, point.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, a, b);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + "x=" + x + ", y=" + y + ", a=" + a + ", b=" + b + "}";
    }

    /**
     * Throws an IllegalArgumentException if point is not on elliptic curve curve described y^2 != x^3 + ax + b
     */
    private void checkIsPointOnCurve() {
        if (isIdentity.getAsBoolean()) {
            return;
        }
        if (square.apply(y).notEquals(cube.apply(x).add(a.multiply(x)).add(b))) {
            throw new IllegalArgumentException(invalidCoordinatesError.apply(x, y)); // y^2 != x^3 + ax + b
        }
    }
}
