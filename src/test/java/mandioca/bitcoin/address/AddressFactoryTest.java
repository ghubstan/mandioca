package mandioca.bitcoin.address;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.ecc.Secp256k1Point;
import mandioca.bitcoin.ecc.Secp256k1PrivateKey;
import org.junit.Test;

import java.math.BigInteger;

import static junit.framework.TestCase.assertTrue;
import static mandioca.bitcoin.address.AddressFactory.*;
import static mandioca.bitcoin.address.AddressType.P2PKH;
import static mandioca.bitcoin.network.NetworkType.MAINNET;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

public class AddressFactoryTest extends MandiocaTest {

    @Test
    public void testHashToP2pkhMainnetAddress() {
        // Test params from programmingbitcoin/code-ch08/helper.py  def test_p2pkh_address(self)
        byte[] hash = HEX.decode("74d691da1574e6b3c192ecfb52cc8984ee7b6c56");
        String expectedAddress = "1BenRpVUFK65JFWcQSuHnJKzc4M8ZP8Eqa";
        Address actualAddress = scriptHashToP2pkh.apply(hash, MAINNET);
        assertEquals(expectedAddress, actualAddress.value());
        assertTrue(actualAddress.validate()); // TODO
    }

    @Test
    public void testHashToP2pkhTestnetAddress() {
        // Test params from programmingbitcoin/code-ch08/helper.py   def test_p2pkh_address(self)
        byte[] hash = HEX.decode("74d691da1574e6b3c192ecfb52cc8984ee7b6c56");
        String expectedAddress = "mrAjisaT4LXL5MzE81sfcDYKU3wqWSvf9q";
        Address actualAddress = scriptHashToP2pkh.apply(hash, TESTNET3);
        assertEquals(expectedAddress, actualAddress.value());
        assertTrue(actualAddress.validate()); // TODO
    }

    @Test
    public void testHashToP2shMainnetAddress() {
        // Test params from programmingbitcoin/code-ch08/helper.py  def test_p2sh_address(self)
        byte[] hash = HEX.decode("74d691da1574e6b3c192ecfb52cc8984ee7b6c56");
        String expectedAddress = "3CLoMMyuoDQTPRD3XYZtCvgvkadrAdvdXh";
        Address actualAddress = scriptHashToP2sh.apply(hash, MAINNET);
        assertEquals(expectedAddress, actualAddress.value());
        assertTrue(actualAddress.validate()); // TODO
    }

    @Test
    public void testHashToP2shTestnetAddress() {
        // Test params from programmingbitcoin/code-ch08/helper.py  def test_p2sh_address(self)
        byte[] hash = HEX.decode("74d691da1574e6b3c192ecfb52cc8984ee7b6c56");
        String expectedAddress = "2N3u1R6uwQfuobCqbCgBkpsgBxvr1tZpe7B";
        Address actualAddress = scriptHashToP2sh.apply(hash, TESTNET3);
        assertEquals(expectedAddress, actualAddress.value());
        assertTrue(actualAddress.validate()); // TODO
    }

    @Test
    public void testPrivateKeyToP2pkhTestnetAddress1() {
        // Test params from Jimmy Song book, Chapter 4, Exercise 5 (use uncompressed SEC on testnet)
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(BigInteger.valueOf(5002));
        Secp256k1Point publicKey = privateKey.getPublicKey();
        Address actualAddress = publicKeyToP2pkhAddress.apply(publicKey, false, TESTNET3);
        String expectedAddress = "mmTPbXQFxboEtNRkwfh6K51jvdtHLxGeMA";
        assertEquals(expectedAddress, actualAddress.value());
        assertEquals(P2PKH, actualAddress.addressType());
        assertEquals(TESTNET3, actualAddress.networkType());
        assertTrue(actualAddress.validate()); // TODO
    }

    @Test
    public void testPrivateKeyToP2pkhTestnetAddress2() {
        // Test params from Jimmy Song book, Chapter 4, Exercise 5 (use compressed SEC on testnet)
        BigInteger secret = BigInteger.valueOf(2020).pow(5);
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        Address actualAddress = publicKeyToP2pkhAddress.apply(publicKey, true, TESTNET3);
        String expectedAddress = "mopVkxp8UhXqRYbCYJsbeE1h1fiF64jcoH";
        assertEquals(expectedAddress, actualAddress.value());
        assertEquals(P2PKH, actualAddress.addressType());
        assertEquals(TESTNET3, actualAddress.networkType());
        assertTrue(actualAddress.validate()); // TODO
    }

    @Test
    public void testPrivateKeyToP2pkhMainnetAddress1() {
        // Test params from Jimmy Song book, Chapter 4, Exercise 5 (use compressed SEC on mainnet)
        BigInteger secret = HEX.stringToBigInt.apply("0x12345deadbeef");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        Address actualAddress = publicKeyToP2pkhAddress.apply(publicKey, true, MAINNET);
        String expectedAddress = "1F1Pn2y6pDb68E5nYJJeba4TLg2U7B6KF1";
        assertEquals(expectedAddress, actualAddress.value());
        assertEquals(P2PKH, actualAddress.addressType());
        assertEquals(MAINNET, actualAddress.networkType());
        assertTrue(actualAddress.validate()); // TODO
    }
}
