package mandioca.bitcoin.ecc;

import mandioca.bitcoin.ecc.curveparams.Secp256k1CurveParameters;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.math.BigInteger.TWO;
import static java.math.BigInteger.ZERO;
import static mandioca.bitcoin.ecc.curveparams.Secp256k1CurveParameters.*;
import static mandioca.bitcoin.function.BigIntegerFunctions.THREE;
import static mandioca.bitcoin.function.BigIntegerFunctions.startEnd;
import static mandioca.bitcoin.function.ByteArrayFunctions.bigIntToUnsignedByteArray;
import static mandioca.bitcoin.function.ByteArrayFunctions.concatenate;
import static mandioca.bitcoin.function.HashFunctions.hash160;

public class Secp256k1Point extends EllipticCurvePoint implements Point {

    public static final Field FIELD_A = new FieldElement(Secp256k1CurveParameters.a, p);
    public static final Field FIELD_B = new FieldElement(Secp256k1CurveParameters.b, p);
    public static final Field FIELD_Gx = new FieldElement(Gx, p);
    public static final Field FIELD_Gy = new FieldElement(Gy, p);
    public static final Secp256k1Point G = new Secp256k1Point(FIELD_Gx, FIELD_Gy);
    public static final Secp256k1Point IDENTITY = new Secp256k1Point(null, null);
    static final byte[] COMPRESSED_EVEN_SEC_PREFIX = new byte[]{(byte) 0x2};
    static final byte[] COMPRESSED_ODD_SEC_PREFIX = new byte[]{(byte) 0x3};
    static final byte[] UNCOMPRESSED_SEC_PREFIX = new byte[]{(byte) 0x4};
    static final Function<BigInteger, FieldElement> newField = (n) -> new FieldElement(n, p);
    public static final BiFunction<BigInteger, BigInteger, Secp256k1Point> newPoint = (x, y)
            -> new Secp256k1Point(newField.apply(x), newField.apply(y));
    private static final BiFunction<byte[], Integer[], BigInteger> decodeCoordinate = (sec, range)
            -> new BigInteger(1, Arrays.copyOfRange(sec, range[0], range[1]));
    private static final Function<Byte, Boolean> isUncompressedSec = (b) -> b == UNCOMPRESSED_SEC_PREFIX[0];
    private static final Function<Byte, Boolean> isCompressedEvenSec = (b) -> b == COMPRESSED_EVEN_SEC_PREFIX[0];
    private static final Function<Field, Boolean> isYCoordinateEven = (y) -> y.getNumber().mod(TWO).compareTo(ZERO) == 0;
    private static final BiFunction<BigInteger, BigInteger, BigInteger> calcU = (z, sigInverse) -> z.multiply(sigInverse).mod(N);
    private static final BiFunction<Signature, BigInteger, BigInteger> calcV = (sig, sigInverse) -> sig.getR().multiply(sigInverse).mod(N);
    public final Function<Boolean, byte[]> secHash160 = (compressed) -> hash160.apply(getSec(compressed)); // 20-byte hash of pubkey
    private final Function<Point, Secp256k1Point> wrapResult = (p) -> new Secp256k1Point(p.getX(), p.getY());

    public Secp256k1Point(Field x, Field y) {
        super(x, y, FIELD_A, FIELD_B);
    }

    /**
     * Returns a Point object from a SEC binary.
     *
     * @param sec byte[]
     * @return Secp256k1Point
     */
    public static Secp256k1Point parse(byte[] sec) {
        return isUncompressedSec.apply(sec[0]) ? parseUncompressedSec(sec) : parseCompressedSec(sec);
    }

    private static Secp256k1Point parseUncompressedSec(byte[] sec) {
        BigInteger x = decodeCoordinate.apply(sec, startEnd.apply(1, 33));
        BigInteger y = decodeCoordinate.apply(sec, startEnd.apply(33, 65));
        return newPoint.apply(x, y);
    }

    private static Secp256k1Point parseCompressedSec(byte[] sec) {
        BigInteger x = decodeCoordinate.apply(sec, startEnd.apply(1, sec.length));
        Field alpha = newField.apply(x).power(THREE).add(FIELD_B);
        Field beta = alpha.sqrt();
        Field evenBeta, oddBeta;
        if (isYCoordinateEven.apply(beta)) {
            evenBeta = beta;
            oddBeta = newField.apply(p.subtract(beta.getNumber()));
        } else {
            evenBeta = newField.apply(p.subtract(beta.getNumber()));
            oddBeta = beta;
        }
        if (isCompressedEvenSec.apply(sec[0])) {
            return newPoint.apply(x, evenBeta.getNumber());
        } else {
            return newPoint.apply(x, oddBeta.getNumber());
        }
    }

    @Override
    public Secp256k1Point add(Point other) {
        return wrapResult.apply(super.add(other));
    }

    @Override
    public Secp256k1Point scalarMultiply(BigInteger coefficient) {
        Point result = super.scalarMultiply(coefficient.mod(N));
        return wrapResult.apply(result);
    }

    public boolean verify(BigInteger z, Signature signature) {
        BigInteger sigInverse = signature.sInverse.apply(N);
        BigInteger u = calcU.apply(z, sigInverse);
        BigInteger v = calcV.apply(signature, sigInverse);
        Secp256k1Point total = G.scalarMultiply(u).add(scalarMultiply(v));
        return total.getX().getNumber().equals(signature.getR());
    }

    public byte[] getSec(boolean compressed) {
        byte[] xBytes = bigIntToUnsignedByteArray.apply(x.getNumber());
        byte[] yBytes = bigIntToUnsignedByteArray.apply(y.getNumber());
        if (compressed) {
            if (isYCoordinateEven.apply(y)) {
                return concatenate.apply(COMPRESSED_EVEN_SEC_PREFIX, xBytes);
            } else {
                return concatenate.apply(COMPRESSED_ODD_SEC_PREFIX, xBytes);
            }
        } else {
            return concatenate.apply(UNCOMPRESSED_SEC_PREFIX, concatenate.apply(xBytes, yBytes));
        }
    }

    @Override
    protected Secp256k1Point clone() {
        return (Secp256k1Point) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Secp256k1Point p = (Secp256k1Point) o;
        return Objects.equals(x, p.x) && Objects.equals(y, p.y) &&
                Objects.equals(a, p.a) && Objects.equals(b, p.b);
    }

    public boolean notEquals(Object o) {
        if (o == null || getClass() != o.getClass()) return true;
        Secp256k1Point p = (Secp256k1Point) o;
        return !Objects.equals(x, p.x) || !Objects.equals(y, p.y)
                || !Objects.equals(a, p.a) || !Objects.equals(b, p.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, a, b);
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "{" + "\n"
                + "  x = " + x + ",\n"
                + "  y = " + y + ",\n"
                + "  a = " + a + ",\n"
                + "  b = " + b + "\n"
                + " }";
    }
}
