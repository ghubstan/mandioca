package mandioca.bitcoin.ecc;

import java.math.BigInteger;

import static mandioca.bitcoin.ecc.curveparams.EllipticCurveName.SECP256K1;

// TODO create tests from data in https://crypto.stackexchange.com/questions/784/are-there-any-secp256k1-ecdsa-test-examples-available

public abstract class AbstractSecp256k1Test extends AbstractEllipticCurveTest {
    protected static final EllipticCurve SECP256K1_CURVE = new EllipticCurve(SECP256K1);
    protected static final Field A = SECP256K1_CURVE.getA();  // constant finite field (a, p), where a = 0
    protected static final Field B = SECP256K1_CURVE.getB();  // constant finite field (b, p), where b = 7
    protected static final BigInteger P = SECP256K1_CURVE.getP();  // finite field prime order p
    protected static final Secp256k1Point G = (Secp256k1Point) SECP256K1_CURVE.getG(); // curve's generator point G
    protected static final BigInteger N = SECP256K1_CURVE.getN();  // curve's group order N
    protected static final Secp256k1Point IDENTITY = (Secp256k1Point) SECP256K1_CURVE.getIdentity(); // curve's identity pt at infinity

    protected Secp256k1Point getPoint(BigInteger x, BigInteger y) {
        return (Secp256k1Point) SECP256K1_CURVE.getPoint(x, y);
    }
}
