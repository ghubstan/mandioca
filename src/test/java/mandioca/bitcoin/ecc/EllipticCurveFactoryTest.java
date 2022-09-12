package mandioca.bitcoin.ecc;

import org.junit.Test;

import java.math.BigInteger;

import static java.math.BigInteger.*;
import static mandioca.bitcoin.ecc.curveparams.EllipticCurveName.SECP256K1;
import static org.junit.Assert.assertEquals;

public class EllipticCurveFactoryTest extends AbstractEllipticCurveTest {

    @Test
    public void testCreateSecp256k1Curve() {
        EllipticCurve ec = new EllipticCurve(SECP256K1);
        assertEquals(SECP256K1.name().toLowerCase(), ec.getName());
        assertEquals("y^2 = x^3 + 7", ec.getEquation());
        assertEquals(ZERO, ec.getA().getNumber());
        assertEquals(valueOf(7), ec.getB().getNumber());
        Secp256k1Point generatorPoint = ((Secp256k1Point) ec.getG());
        assertEquals(new BigInteger("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16),
                generatorPoint.getX().getNumber());
        assertEquals(new BigInteger("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8", 16),
                generatorPoint.getY().getNumber());
        assertEquals(new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16),
                ec.getN());
        assertEquals(TWO.pow(256).subtract(TWO.pow(32)).subtract(BigInteger.valueOf(977)), ec.getP());
        assertEquals(ec.getP(), generatorPoint.getY().getPrime());
    }
}
