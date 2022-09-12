package mandioca.bitcoin.ecc;

import mandioca.bitcoin.function.QuadriFunction;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.function.Function;

import static java.math.BigInteger.TWO;
import static java.util.Arrays.copyOfRange;
import static mandioca.bitcoin.ecc.Secp256k1Point.G;
import static mandioca.bitcoin.ecc.curveparams.Secp256k1CurveParameters.N;
import static mandioca.bitcoin.function.ByteArrayFunctions.bigIntToUnsignedByteArray;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.util.Base58.decodeChecked;
import static mandioca.bitcoin.util.Base58.encode;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class Secp256k1PrivateKey {

    private static final Function<BigInteger, BigInteger> kInverse = (k) -> k.modPow(N.subtract(TWO), N);
    private static final Function<Boolean, byte[]> networkPrefix = (testnet) -> testnet ? new byte[]{(byte) 0xef} : new byte[]{(byte) 0x80};
    private static final Function<Boolean, byte[]> wifSuffix = (compressed) -> compressed ? new byte[]{(byte) 0x01} : new byte[0];
    private static final Rfc6979 kalkulator = new Rfc6979();
    private static final Function<BigInteger, BigInteger> calcR = (k) -> G.scalarMultiply(k).getX().getNumber(); // x coordinate of kG
    private static final QuadriFunction<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> calcS = (z, r, e, kInv) -> z.add(r.multiply(e)).multiply(kInv).mod(N);   // ((z + r*e) * kInv) % N, or s=(z+re)/k mod N
    private static final Function<BigInteger, Boolean> sValueIsNotLowEnoughForTxRelay = (s) -> s.compareTo(N.divide(TWO)) > 0;

    private final BigInteger e;                 // Private Key (double-hashed secret)
    private final Secp256k1Point publicKey;     // Public Key P = eG

    public Secp256k1PrivateKey(BigInteger secret) {
        this.e = secret;
        this.publicKey = G.scalarMultiply(e);
    }

    // K is deterministic, calculated according to RFC 6979
    @SuppressWarnings("DuplicatedCode")
    public Signature sign(BigInteger z) {
        kalkulator.init(e, z);
        BigInteger k = kalkulator.nextK();
        BigInteger r = calcR.apply(k);         // x coordinate of kG
        BigInteger kInv = kInverse.apply(k);
        BigInteger s = calcS.apply(z, r, e, kInv); // ((z + r*e) * kInv) % N, or s=(z+re)/k mod N
        if (sValueIsNotLowEnoughForTxRelay.apply(s)) {
            s = N.subtract(s); // A low s value induces bitcoin nodes to relay Tx for malleability reasons. -Jimmy Song
        }
        return new Signature(r, s);
    }

    // pkg protected signing method taking 'k' argument -- for testing only
    @SuppressWarnings("DuplicatedCode")
    Signature sign(BigInteger z, BigInteger k) {
        BigInteger r = calcR.apply(k);         // x coordinate of kG
        BigInteger kInv = kInverse.apply(k);
        BigInteger s = calcS.apply(z, r, e, kInv); // ((z + r*e) * kInv) % N, or s=(z+re)/k mod N
        if (sValueIsNotLowEnoughForTxRelay.apply(s)) {
            s = N.subtract(s); // A low s value induces bitcoin nodes to relay Tx for malleability reasons. -Jimmy Song
        }
        return new Signature(r, s);
    }

    public Secp256k1Point getPublicKey() {
        return publicKey;
    }

    public String getWif(boolean compressed, boolean testnet) {
        // TODO take NetworkType argument, not boolean 'testnet',
        //      but this would mean creating a bitcoin pkg dependency here in ecc.
        //          Solution: move ecc to bitcoin pkg
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Algorithm from Jimmy Song's book, Chapter 4
            baos.write(networkPrefix.apply(testnet));
            baos.write(bigIntToUnsignedByteArray.apply(e));
            baos.write(wifSuffix.apply(compressed));
            byte[] payload = baos.toByteArray();
            byte[] checksum = copyOfRange(hash256.apply(payload), 0, 4);
            baos.write(checksum);
            return encode(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error creating private key WIF", e);
        }
    }

    public static Secp256k1PrivateKey wifToPrivateKey(String wif, boolean compressed) {
        byte[] rawPrivateKey = wifToRawPrivateKey(wif, compressed);
        BigInteger e = new BigInteger(1, rawPrivateKey);
        return new Secp256k1PrivateKey(e);
    }

    /**
     * Convert a WIF to raw private key byte array.
     *
     * @param wif wallet input format string
     * @return private key bytes
     */
    public static byte[] wifToRawPrivateKey(String wif, boolean compressed) {

        // TODO take NetworkType argument, not boolean 'testnet',
        //      but this would mean creating a bitcoin pkg dependency here in ecc.
        //          Solution: move ecc to bitcoin pkg

        // From https://en.bitcoin.it/wiki/Wallet_import_format
        //
        // WIF to private key
        //
        //  1 - Take a Wallet Import Format string
        //
        //          5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ
        //
        //  2 - Convert it to a byte string using Base58Check encoding
        //
        //          800C28FCA386C7A227600B2FE50B7CAE11EC86D3BF1FBE471BE89827E19D72AA1D507A5B8D
        //          800c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d507a5b8d
        //
        //  3 - Drop the last 4 checksum bytes from the byte string
        //
        //          800C28FCA386C7A227600B2FE50B7CAE11EC86D3BF1FBE471BE89827E19D72AA1D
        //          800c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d
        //
        //  4 - Drop the first byte ( [if mainnet address (sic)] it should be 0x80 ).
        //      If the private key corresponded to a compressed public key, also drop the last byte (it should be 0x01).
        //      If it corresponded to a compressed public key, the WIF string will have started with K or L instead
        //      of 5 (or c instead of 9 on testnet).
        //      This is the private key.
        //
        //          0C28FCA386C7A227600B2FE50B7CAE11EC86D3BF1FBE471BE89827E19D72AA1D
        //          0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d
        //
        // TODO: 12/29/19 Check incoming wif's first character against
        //      rules for mainnet, testnet, regtest, compressed, uncompressed
        // System.out.println("wif = " + wif + "  compressed = " + compressed);

        byte[] prefixedKey = decodeChecked(wif, false); // steps 2 & 3 combined
        // out.println("prefixedKey = " + HexUtils.toHexString(prefixedKey));

        int to = compressed ? prefixedKey.length - 1 : prefixedKey.length;
        return copyOfRange(prefixedKey, 1, to);
    }

    @Override
    public String toString() {
        return "Secp256k1PrivateKey{\n" +
                "  e=" + HEX.toPrettyHex(e) + "\n" +
                ", publicKey=" + publicKey +
                "";
    }
}
