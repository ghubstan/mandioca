package mandioca.bitcoin.ecc;

import mandioca.bitcoin.ecc.curveparams.ECCurveParameters;
import mandioca.bitcoin.ecc.curveparams.EllipticCurveName;
import mandioca.bitcoin.function.QuadriFunction;

import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static mandioca.bitcoin.ecc.EllipticCurvePoint.newIdentity;
import static mandioca.bitcoin.ecc.FieldElement.newField;
import static mandioca.bitcoin.ecc.Secp256k1Point.G;
import static mandioca.bitcoin.ecc.Secp256k1Point.IDENTITY;
import static mandioca.bitcoin.ecc.curveparams.EllipticCurveName.SECP256K1;
import static mandioca.bitcoin.util.HexUtils.HEX;

/**
 * Encapsulates an elliptic curve's properties: name, equation, a, b, p, Gx, Gy, N, and generates points on the curve
 * for x & y coordinates.
 */
public final class EllipticCurve {
    protected final BiFunction<BigInteger, BigInteger, Boolean> areNotIdentityCoordinates = (x, y) -> x != null && y != null;
    /**
     * Map of curve parameters used to construct fields and points.
     */
    private final ECCurveParameters ecCurveParameters;
    /**
     * Name of curve, i.e., secp256k1.
     */
    private final String name;
    // don't know how to make IDE shutdown re-ordering members
    protected final Supplier<Boolean> isSecp256k1Curve = () -> this.getName().toLowerCase().equals(SECP256K1.name().toLowerCase());
    /**
     * Equation for curve, i.e., y^2 = x^3 + ax + b
     */
    private final String equation;
    /**
     * Prime field order p.
     */
    private final BigInteger p;
    // don't know how to make IDE shutdown re-ordering members
    public final QuadriFunction<BigInteger, BigInteger, Field, Field, Point> newPoint = (x, y, a, b) -> {
        if (isSecp256k1Curve.get()) {
            return Secp256k1Point.newPoint.apply(x, y);
        } else {
            return EllipticCurvePoint.newPoint.apply(x, y, a.getNumber(), b.getNumber(), this.getP());
        }
    };
    /**
     * Constant a of the curve equation.
     */
    private final Field a;
    /**
     * Constant b of the curve equation.
     */
    private final Field b;
    /**
     * Generator point G on the curve.
     */
    private final Point g;
    /**
     * Group order N
     */
    private final BigInteger n;
    private final Point identity;

    public EllipticCurve(EllipticCurveName curveName) {
        this.ecCurveParameters = new ECCurveParameters(curveName);
        this.name = ecCurveParameters.getCurveName();
        this.equation = ecCurveParameters.getEquation();
        this.p = ecCurveParameters.getP();
        this.a = new FieldElement(ecCurveParameters.getA(), p);
        this.b = new FieldElement(ecCurveParameters.getB(), p);
        this.n = ecCurveParameters.getN();
        //noinspection SwitchStatementWithTooFewBranches
        switch ((curveName)) {
            case SECP256K1:
                this.g = G;
                this.identity = IDENTITY;
                break;
            default:
                this.g = new EllipticCurvePoint(
                        newField.apply(ecCurveParameters.getGx(), p),
                        newField.apply(ecCurveParameters.getGy(), p),
                        a, b);
                this.identity = new EllipticCurvePoint(null, null, a, b);
                break;
        }
        assert a.getPrime().equals(b.getPrime());
    }

    public EllipticCurve(String name, String equation,
                         BigInteger a, BigInteger b, BigInteger p,
                         BigInteger gx, BigInteger gy, BigInteger n) {
        this.ecCurveParameters = null;
        this.name = name;
        this.equation = equation;
        this.p = p;
        this.a = newField.apply(a, p);
        this.b = newField.apply(b, p);
        this.g = areNotIdentityCoordinates.apply(gx, gy) ? EllipticCurvePoint.newPoint.apply(gx, gy, a, b, p) : null;
        this.n = n;
        this.identity = newIdentity.apply(a, b, p);
    }

    public Point getPoint(BigInteger x, BigInteger y) {
        return newPoint.apply(x, y, getA(), getB());
    }

    public String getName() {
        return name;
    }

    public String getEquation() {
        return equation;
    }

    public Field getA() {
        return a;
    }

    public Field getB() {
        return b;
    }

    public BigInteger getP() {
        return p;
    }

    public Point getG() {
        return g;
    }

    public BigInteger getN() {
        return n;
    }

    public Point getIdentity() {
        return identity;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{\n" +
                "  a=" + HEX.encode(a.getNumber()) + "\n"
                + ", b=" + HEX.encode(b.getNumber()) + "\n"
                + ", p=" + HEX.encode(a.getPrime()) + "\n"
                + ", G=" + g + "\n"
                + ", N=" + HEX.encode(n) + "\n"
                + ", Identity=" + identity + "\n"
                + '}';
    }
}
