package mandioca.bitcoin.ecc;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Function;

import static java.math.BigInteger.TWO;
import static mandioca.bitcoin.util.HexUtils.HEX;

// See https://github.com/jimmysong/pybtcfork/blob/master/ecc.py
// See https://www.youtube.com/watch?v=e6voIwB-An4&feature=youtu.be

public class Signature {
    // assuming  signature's won't be parsed or serialized in parallel
    private static final SignatureDerParser parser = new SignatureDerParser();
    private static final SignatureDerSerializer serializer = new SignatureDerSerializer();
    /**
     * X coordinate of point R
     */
    private final BigInteger r;
    /**
     * s = (z+re)/k, where z is a signature hash, e is a private key, and k is a random 256-bit number
     */
    private final BigInteger s;

    public final Function<BigInteger, BigInteger> sInverse = (N) -> this.getS().modPow(N.subtract(TWO), N);

    public Signature(BigInteger r, BigInteger s) {
        this.r = r;
        this.s = s;
    }

    public static Signature parse(byte[] der) {
        return parser.init(der).parse();
    }

    public BigInteger getR() {
        return r;
    }

    public BigInteger getS() {
        return s;
    }

    public byte[] getDer() {
        return serializer.init(this).getDer();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signature other = (Signature) o;
        return Objects.equals(r, other.r) &&
                Objects.equals(s, other.s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, s);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{\n"
                + "  r =" + HEX.toPrettyHex(r) + "\n"
                + ", s =" + HEX.toPrettyHex(s) + "\n"
                + "}";
    }
}
