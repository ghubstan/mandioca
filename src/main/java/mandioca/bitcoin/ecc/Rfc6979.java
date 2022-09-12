package mandioca.bitcoin.ecc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;
import static java.util.Arrays.fill;
import static java.util.Arrays.stream;
import static mandioca.bitcoin.ecc.curveparams.Secp256k1CurveParameters.N;
import static mandioca.bitcoin.function.BigIntegerFunctions.*;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.util.HexUtils.HEX;

/**
 * Deterministic K calculator based on the algorithm defined in section 3.2 of RFC 6979.
 * <p>
 * See https://tools.ietf.org/html/rfc6979
 */
public class Rfc6979 {

    private static final String MAC_ALGORITHM = "HmacSHA256";

    private final Mac mac;
    private byte[] k;
    private byte[] v;
    @Deprecated
    private boolean debug;

    public Rfc6979() {
        try {
            this.mac = Mac.getInstance(MAC_ALGORITHM);
            this.k = new byte[mac.getMacLength()];
            this.v = new byte[mac.getMacLength()];
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // pkg protected constructor for printing debug info
    @Deprecated
    Rfc6979(boolean debug) {
        this();
        this.debug = debug;
    }

    public void init(BigInteger e /* secret -> hashed twice */, BigInteger z /*H1 = H(message) -> hashed once*/) {
        // See https://tools.ietf.org/html/rfc6979#section-3.1.1
        try {
            z = reduceZ(z);  // Reduce z if very, very large.
            byte[] secret = bigIntToUnsignedByteArray.apply(e);  // Caching arguments' bytes for use in the next steps.
            byte[] h1 = bigIntToUnsignedByteArray.apply(z);   // Step A ( h1 = H(m) ) is performed by caller
            if (debug) {
                out.printf("init(%s, %s)\n", e.toString(HEX_RADIX), z.toString(HEX_RADIX));
                out.println("h1      = " + HEX.to64DigitPaddedHex(h1));
                out.println("secret = " + HEX.to64DigitPaddedHex(secret)); // correct
            }
            fill(v, ONE_BYTE[0]);    // Step B  set V = 0x01 0x01 0x01 ... 0x01
            fill(k, ZERO_BYTE[0]);   // Step C  set K = 0x00 0x00 0x00 ... 0x00
            doStepD(secret, h1);            // K = HMAC_K(V || 0x00 || secret || h1)
            doStepE();                      // V = HMAC_K(V)
            doStepF(secret, h1);            // K = HMAC_K(V || 0x01 || secret || h1)
            doStepG();                      // V = HMAC_K(V)
        } catch (InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
    }

    public BigInteger nextK() {
        try {
            while (true) {
                processHMACInputs(v);
                v = mac.doFinal();
                if (debug) {
                    out.println("nextK() -> V = " + HEX.to64DigitPaddedHex(v));
                }
                BigInteger candidate = new BigInteger(1, v);
                if (isPositive.test(candidate) && isLessThan.apply(candidate, N)) {
                    if (debug) {
                        out.println("nextK() -> candidate = " + HEX.encode(candidate));
                    }
                    return candidate;
                }
                processHMACInputsVAndZero();
            }
        } catch (InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
    }

    // pkg protected exact duplicate of nextK(), for testing only
    List<BigInteger> getTestKs(int numCandidates) throws InvalidKeyException {
        final List<BigInteger> testCandidates = new ArrayList<>();
        while (true) {
            processHMACInputs(v);
            v = mac.doFinal();
            if (debug) {
                out.println("nextK() -> V = " + HEX.to64DigitPaddedHex(v));
            }
            BigInteger candidate = new BigInteger(1, v);
            if (isPositive.test(candidate) && isLessThan.apply(candidate, N)) {
                if (debug) {
                    out.println("nextK() -> candidate = " + HEX.encode(candidate));
                }
                testCandidates.add(candidate);
            }
            processHMACInputsVAndZero();
            if (testCandidates.size() >= numCandidates) {
                return testCandidates;
            }
        }
    }

    private BigInteger reduceZ(BigInteger z) {
        if (isGreaterThan.apply(z, N)) {
            System.err.println("Reducing z for the first time!");
            return z.subtract(N);
        } else {
            return z;
        }
    }

    //  K = HMAC_K(V || 0x00 || int2octets(x) || bits2octets(h1))
    private void doStepD(byte[] secret, byte[] h1) throws InvalidKeyException {
        processHMACInputs(v, ZERO_BYTE, secret, h1);
        k = mac.doFinal();
        if (debug) {
            out.println("K = " + HEX.to64DigitPaddedHex(k));
        }
    }

    // V = HMAC_K(V)
    private void doStepE() throws InvalidKeyException {
        processHMACInputs(v);
        v = mac.doFinal();
        if (debug) {
            out.println("V = " + HEX.to64DigitPaddedHex(v));
        }
    }

    //  K = HMAC_K(V || 0x01 || int2octets(x) || bits2octets(h1))
    private void doStepF(byte[] secret, byte[] h1) throws InvalidKeyException {
        processHMACInputs(v, ONE_BYTE, secret, h1);
        k = mac.doFinal();
        if (debug) {
            out.println("K = " + HEX.to64DigitPaddedHex(k));
        }
    }

    // V = HMAC_K(V)
    private void doStepG() throws InvalidKeyException {
        processHMACInputs(v);
        v = mac.doFinal();
        if (debug) {
            out.println("V = " + HEX.to64DigitPaddedHex(v));
        }
    }

    private void processHMACInputsVAndZero() throws InvalidKeyException {
        processHMACInputs(v, ZERO_BYTE);
        k = mac.doFinal();
        if (debug) {
            out.println("nextK() -> K = " + HEX.to64DigitPaddedHex(k));
        }
        processHMACInputs(v);
        v = mac.doFinal();
        if (debug) {
            out.println("nextK() -> V = " + HEX.to64DigitPaddedHex(v));
        }
    }

    private void processHMACInputs(byte[]... inputs) throws InvalidKeyException {
        mac.init(new SecretKeySpec(k, MAC_ALGORITHM));
        stream(inputs).forEach(mac::update);
    }
}
