package mandioca.bitcoin.ecc;

import mandioca.bitcoin.address.Address;
import org.junit.Test;

import java.math.BigInteger;

import static java.math.BigInteger.ZERO;
import static java.math.BigInteger.valueOf;
import static mandioca.bitcoin.address.AddressFactory.publicKeyToP2pkhAddress;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

public class Secp256K1PointTest extends AbstractSecp256k1Test {

    @Test
    public void testGeneratorPointProperties() {
        Field gx = G.getX();
        Field gy = G.getY();
        assertEquals(new BigInteger("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16), gx.getNumber());
        assertEquals(new BigInteger("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8", 16), gy.getNumber());
        assertEquals(ZERO, A.getNumber());
        assertEquals(valueOf(7), B.getNumber());
        Secp256k1Point result = G.scalarMultiply(N);
        assertEquals(IDENTITY, result);
        // G's coordinates are 256 bit #s
        assertEquals(256, G.x.getNumber().toByteArray().length * Byte.SIZE);
        assertEquals(256, G.y.getNumber().toByteArray().length * Byte.SIZE);
    }

    @Test
    public void testChapter3_EXAMPLE_7_In_Hex() {  // NOT exercise 7
        BigInteger x = HEX.stringToBigInt.apply("0x79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
        BigInteger y = HEX.stringToBigInt.apply("0x483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8");
        Secp256k1Point g = getPoint(x, y);
        assertEquals(P, g.x.getPrime());
        assertEquals(P, g.y.getPrime());
        Secp256k1Point product = g.scalarMultiply(N);
        assertEquals(IDENTITY, product);
    }

    @Test
    public void testSecUncompressed() {
        BigInteger x = HEX.stringToBigInt.apply("0x79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
        BigInteger y = HEX.stringToBigInt.apply("0x483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8");
        Secp256k1Point g = getPoint(x, y);
        byte[] sec = g.getSec(false);
        String expectedSec = "04" // marker
                + "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798"    // x-coordinate
                + "483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8";   // y-coordinate
        assertEquals(expectedSec, HEX.encode(sec));
    }

    @Test
    public void testSecCompressedYIsEven() {
        BigInteger x = HEX.stringToBigInt.apply("0x79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
        BigInteger y = HEX.stringToBigInt.apply("0x483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8");
        Secp256k1Point g = getPoint(x, y);
        byte[] sec = g.getSec(true);
        String expectedSec = "02" // marker (y is even)
                + "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798";   // x-coordinate
        assertEquals(expectedSec, HEX.encode(sec));
    }

    // TODO testSecCompressedYIsOdd


}
