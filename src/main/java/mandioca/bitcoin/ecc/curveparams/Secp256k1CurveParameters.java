package mandioca.bitcoin.ecc.curveparams;

import java.math.BigInteger;
import java.util.Map;

import static java.math.BigInteger.TWO;
import static java.math.BigInteger.ZERO;
import static mandioca.bitcoin.ecc.curveparams.EllipticCurveName.SECP256K1;
import static mandioca.bitcoin.function.BigIntegerFunctions.HEX_RADIX;

public class Secp256k1CurveParameters {

    // secp256k1 primitive constants
    public static final BigInteger Gx = new BigInteger("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", HEX_RADIX);
    public static final BigInteger Gy = new BigInteger("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8", HEX_RADIX);
    public static final BigInteger a = ZERO;
    public static final BigInteger b = BigInteger.valueOf(7);
    public static final BigInteger p = TWO.pow(256).subtract(TWO.pow(32)).subtract(BigInteger.valueOf(977));
    public static final BigInteger N = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", HEX_RADIX);

    /**
     * Immutable map of primitive secp256k1 curve parameters, indirectly accessed by callers.
     */
    static final Map<String, Object> SECP256K1_PARAMETERS = Map.of(
            "name", SECP256K1.name().toLowerCase(),
            "equation", "y^2 = x^3 + 7",
            "a", a,
            "b", b,
            "p", p,  // secp256k1 field prime p = 2^256 - 2^32 - 977
            "Gx", Gx,
            "Gy", Gy,
            "n", N
    );
}
