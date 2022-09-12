package mandioca.bitcoin.ecc;

import mandioca.bitcoin.util.HashUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static java.math.BigInteger.ONE;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

// Important:  https://github.com/warner/python-ecdsa
//
// Find test data here:
// http://dbis.rwth-aachen.de/~renzel/mobsos/lib/js/jsrsasign/sample-ecdsa.html

// Test data with given k values:
// https://crypto.stackexchange.com/questions/784/are-there-any-secp256k1-ecdsa-test-examples-available

// For test data after after DER impl ->
// https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
// https://github.com/bitcoin-core/secp256k1

public class Rfc6979Test extends AbstractSecp256k1Test {

    private static final Logger log = LoggerFactory.getLogger(Rfc6979Test.class);

    private static final String MAC_ALGORITHM = "HmacSHA256";

    @Test
    public void testHmac() throws InvalidKeyException, NoSuchAlgorithmException {
        final Mac mac = Mac.getInstance(MAC_ALGORITHM);
        final SecretKeySpec key = new SecretKeySpec("key".getBytes(StandardCharsets.UTF_8), MAC_ALGORITHM);
        mac.init(key);
        final byte[] k = mac.doFinal("hello".getBytes(StandardCharsets.UTF_8));
        assertEquals("9307b3b915efb5171ff14d8cb55fbcc798c6c0ef1456d66ded1a6aa723a58b7b", HEX.to64DigitPaddedHex(k));
    }


    @Test  // Test parameters e, z and expected k from https://github.com/jimmysong/pybtcfork/blob/master/ecc_test.py
    public void test1() {
        Rfc6979 rfc6979 = new Rfc6979();
        BigInteger e = new BigInteger("1111111111111111111111111111111111111111111111111111111111111111", 16);  // secret
        BigInteger z = new BigInteger("2222222222222222222222222222222222222222222222222222222222222222", 16);  // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        BigInteger k = rfc6979.nextK();
        // out.println("k = " + HEX.encode(k));
        assertEquals("6931E1828BA0AFBA580ED7C833BFE082C84F1331AFA33A6B98AD8C493CC5EDD0", HEX.encode(k).toUpperCase());
    }

    @Test
    // Test parameters e, z and expected k from https://github.com/bitcoinjs/bitcoinjs-lib/blob/master/test/fixtures/ecdsa.json
    public void test2() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "test data";
        BigInteger e = new BigInteger("fee0a1f7afebf9d2a5a80c0c98a31c709681cce195cbcd06342b517970c0be1e", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(16);
        // testCandidates.forEach(c -> {out.println(c.toString(16));});
        assertEquals("fcce1de7a9bcd6b2d3defade6afa1913fb9229e3b7ddf4749b55c4848b2a196e", HEX.encode(testCandidates.get(0)));
        assertEquals("727fbcb59eb48b1d7d46f95a04991fc512eb9dbf9105628e3aec87428df28fd8", HEX.encode(testCandidates.get(1)));
        assertEquals("398f0e2c9f79728f7b3d84d447ac3a86d8b2083c8f234a0ffa9c4043d68bd258", HEX.encode(testCandidates.get(15)));
    }


    @Test
    // Test parameters e, z and expected k from https://github.com/bitcoinjs/bitcoinjs-lib/blob/master/test/fixtures/ecdsa.json
    public void test3() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "Everything should be made as simple as possible, but not simpler.";
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(ONE, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(16);
        // testCandidates.forEach(c -> {out.println(c.toString(16));});
        assertEquals("ec633bd56a5774a0940cb97e27a9e4e51dc94af737596a0c5cbb3d30332d92a5", HEX.encode(testCandidates.get(0)));
        assertEquals("df55b6d1b5c48184622b0ead41a0e02bfa5ac3ebdb4c34701454e80aabf36f56", HEX.encode(testCandidates.get(1)));
        assertEquals("def007a9a3c2f7c769c75da9d47f2af84075af95cadd1407393dc1e26086ef87", HEX.encode(testCandidates.get(15)));
    }


    @Test
    // Test parameters e, z and expected k from https://github.com/bitcoinjs/bitcoinjs-lib/blob/master/test/fixtures/ecdsa.json
    public void test4() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "Satoshi Nakamoto";
        BigInteger e = BigInteger.TWO;
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(16);
        // testCandidates.forEach(c -> { out.println(c.toString(16)); });
        assertEquals("d3edc1b8224e953f6ee05c8bbf7ae228f461030e47caf97cde91430b4607405e", HEX.encode(testCandidates.get(0)));
        assertEquals("f86d8e43c09a6a83953f0ab6d0af59fb7446b4660119902e9967067596b58374", HEX.encode(testCandidates.get(1)));
        assertEquals("241d1f57d6cfd2f73b1ada7907b199951f95ef5ad362b13aed84009656e0254a", HEX.encode(testCandidates.get(15)));
    }

    @Test
    // Test parameters e, z and expected k from https://github.com/bitcoinjs/bitcoinjs-lib/blob/master/test/fixtures/ecdsa.json
    public void test5() throws InvalidKeyException {
        Rfc6979
                rfc6979 = new Rfc6979();
        String message = "Diffie Hellman";
        BigInteger e = new BigInteger("7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(16);
        assertEquals("c378a41cb17dce12340788dd3503635f54f894c306d52f6e9bc4b8f18d27afcc", HEX.encode(testCandidates.get(0)));
        assertEquals("90756c96fef41152ac9abe08819c4e95f16da2af472880192c69a2b7bac29114", HEX.encode(testCandidates.get(1)));
        assertEquals("7b3f53300ab0ccd0f698f4d67db87c44cf3e9e513d9df61137256652b2e94e7c", HEX.encode(testCandidates.get(15)));
    }

    @Test
    // Test parameters e, z and expected k from https://github.com/bitcoinjs/bitcoinjs-lib/blob/master/test/fixtures/ecdsa.json
    public void test6() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "Japan";
        BigInteger e = new BigInteger("8080808080808080808080808080808080808080808080808080808080808080", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(16);
        assertEquals("f471e61b51d2d8db78f3dae19d973616f57cdc54caaa81c269394b8c34edcf59", HEX.encode(testCandidates.get(0)));
        assertEquals("6819d85b9730acc876fdf59e162bf309e9f63dd35550edf20869d23c2f3e6d17", HEX.encode(testCandidates.get(1)));
        assertEquals("d8e8bae3ee330a198d1f5e00ad7c5f9ed7c24c357c0a004322abca5d9cd17847", HEX.encode(testCandidates.get(15)));
    }


    @Test
    // Test parameters e, z and expected k from https://github.com/bitcoinjs/bitcoinjs-lib/blob/master/test/fixtures/ecdsa.json
    public void test7() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "Bitcoin";
        BigInteger e = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364140", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(16);
        assertEquals("36c848ffb2cbecc5422c33a994955b807665317c1ce2a0f59c689321aaa631cc", HEX.encode(testCandidates.get(0)));
        assertEquals("4ed8de1ec952a4f5b3bd79d1ff96446bcd45cabb00fc6ca127183e14671bcb85", HEX.encode(testCandidates.get(1)));
        assertEquals("56b6f47babc1662c011d3b1f93aa51a6e9b5f6512e9f2e16821a238d450a31f8", HEX.encode(testCandidates.get(15)));
    }


    @Test
    // Test parameters e, z and expected k from https://github.com/bitcoinjs/bitcoinjs-lib/blob/master/test/fixtures/ecdsa.json
    public void test8() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "i2FLPP8WEus5WPjpoHwheXOMSobUJVaZM1JPMQZq";
        BigInteger e = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364140", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(16);
        assertEquals("6e9b434fcc6bbb081a0463c094356b47d62d7efae7da9c518ed7bac23f4e2ed6", HEX.encode(testCandidates.get(0)));
        assertEquals("ae5323ae338d6117ce8520a43b92eacd2ea1312ae514d53d8e34010154c593bb", HEX.encode(testCandidates.get(1)));
        assertEquals("3eaa1b61d1b8ab2f1ca71219c399f2b8b3defa624719f1e96fe3957628c2c4ea", HEX.encode(testCandidates.get(15)));
    }


    @Test
    // Test parameters e, z and expected k from https://github.com/bitcoinjs/bitcoinjs-lib/blob/master/test/fixtures/ecdsa.json
    public void test9() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "lEE55EJNP7aLrMtjkeJKKux4Yg0E8E1SAJnWTCEh";
        BigInteger e = new BigInteger("3881e5286abc580bb6139fe8e83d7c8271c6fe5e5c2d640c1f0ed0e1ee37edc9", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(16);
        assertEquals("5b606665a16da29cc1c5411d744ab554640479dd8abd3c04ff23bd6b302e7034", HEX.encode(testCandidates.get(0)));
        assertEquals("f8b25263152c042807c992eacd2ac2cc5790d1e9957c394f77ea368e3d9923bd", HEX.encode(testCandidates.get(1)));
        assertEquals("ea624578f7e7964ac1d84adb5b5087dd14f0ee78b49072aa19051cc15dab6f33", HEX.encode(testCandidates.get(15)));
    }


    @Test
    // Test parameters e, z and expected k from https://github.com/bitcoinjs/bitcoinjs-lib/blob/master/test/fixtures/ecdsa.json
    public void test10() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "2SaVPvhxkAPrayIVKcsoQO5DKA8Uv5X/esZFlf+y";
        BigInteger e = new BigInteger("7259dff07922de7f9c4c5720d68c9745e230b32508c497dd24cb95ef18856631", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(16);
        assertEquals("3ab6c19ab5d3aea6aa0c6da37516b1d6e28e3985019b3adb388714e8f536686b", HEX.encode(testCandidates.get(0)));
        assertEquals("19af21b05004b0ce9cdca82458a371a9d2cf0dc35a813108c557b551c08eb52e", HEX.encode(testCandidates.get(1)));
        assertEquals("117a32665fca1b7137a91c4739ac5719fec0cf2e146f40f8e7c21b45a07ebc6a", HEX.encode(testCandidates.get(15)));
    }

    @Test
    // Test parameters e, z and expected k from https://github.com/bitcoinjs/bitcoinjs-lib/blob/master/test/fixtures/ecdsa.json
    public void test11() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "00A0OwO2THi7j5Z/jp0FmN6nn7N/DQd6eBnCS+/b";
        BigInteger e = new BigInteger("0d6ea45d62b334777d6995052965c795a4f8506044b4fd7dc59c15656a28f7aa", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(16);
        assertEquals("79487de0c8799158294d94c0eb92ee4b567e4dc7ca18addc86e49d31ce1d2db6", HEX.encode(testCandidates.get(0)));
        assertEquals("9561d2401164a48a8f600882753b3105ebdd35e2358f4f808c4f549c91490009", HEX.encode(testCandidates.get(1)));
        assertEquals("b0d273634129ff4dbdf0df317d4062a1dbc58818f88878ffdb4ec511c77976c0", HEX.encode(testCandidates.get(15)));
    }

    @Test // Test parameters e, z and expected k from https://bitcointalk.org/index.php?topic=285142.40
    public void test112() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "Alan Turing";
        BigInteger e = new BigInteger("f8b8af8ce3c7cca5e300d33939540c10d45ce001b8f252bfbc57ba0342904181", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(1);
        // testCandidates.forEach(c -> { out.println(c.toString(16)); });
        assertEquals("525a82b70e67874398067543fd84c83d30c175fdc45fdeee082fe13b1d7cfdf1", HEX.encode(testCandidates.get(0)));
    }

    @Test // Test parameters e, z and expected k from https://bitcointalk.org/index.php?topic=285142.40
    public void test13() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "There is a computer disease that anybody who works with computers knows about. It's a very serious disease and it interferes completely with the work. The trouble with computers is that you 'play' with them!";
        BigInteger e = new BigInteger("e91671c46231f833a6406ccbea0e3e392c76c167bac1cb013f6f1013980455c2", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(1);
        // testCandidates.forEach(c -> { out.println(c.toString(16)); });
        assertEquals("1f4b84c23a86a221d233f2521be018d9318639d5b8bbd6374a8a59232d16ad3d", HEX.encode(testCandidates.get(0)));
    }

    @Test // Test parameters e, z and expected k from https://bitcointalk.org/index.php?topic=285142.40
    public void test14() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "Satoshi Nakamoto";
        BigInteger e = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364140", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(1);
        // testCandidates.forEach(c -> { out.println(c.toString(16)); });
        assertEquals("33a19b60e25fb6f4435af53a3d42d493644827367e6453928554f43e49aa6f90", HEX.encode(testCandidates.get(0)));
    }

    @Test // Test parameters e, z and expected k from https://bitcointalk.org/index.php?topic=285142.40
    public void test15() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "All those moments will be lost in time, like tears in rain. Time to die...";
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(ONE, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(1);
        // testCandidates.forEach(c -> { out.println(c.toString(16)); });
        assertEquals("38aa22d72376b4dbc472e06c3ba403ee0a394da63fc58d88686c611aba98d6b3", HEX.encode(testCandidates.get(0)));
    }

    @Test
    // Test parameters e, z and expected k from https://tools.ietf.org/html/rfc6979  (A.2.5 ECDSA, 256 Bits With SHA-256)
    public void test16() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "sample";
        BigInteger e = new BigInteger("C9AFA9D845BA75166B5C215767B1D6934E50C3DB36E89B127B8A622B120F6721", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(1);
        // testCandidates.forEach(c -> { out.println(c.toString(16)); });
        assertEquals("a6e3c57dd01abe90086538398355dd4c3b17aa873382b0f24d6129493d8aad60", HEX.encode(testCandidates.get(0)));
    }

    @Test
    // Test parameters e, z and expected k from https://tools.ietf.org/html/rfc6979  (A.2.5 ECDSA, 256 Bits With SHA-256)
    public void test17() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "test";
        BigInteger e = new BigInteger("C9AFA9D845BA75166B5C215767B1D6934E50C3DB36E89B127B8A622B120F6721", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(1);
        // testCandidates.forEach(c -> { out.println(c.toString(16)); });
        assertEquals("d16b6ae827f17175e040871a1c7ec3500192c4c92677336ec2537acaee0008e0", HEX.encode(testCandidates.get(0)));
    }

    @Test
    // Test parameters e, z and expected k from https://tools.ietf.org/html/rfc6979  (A.2.5 ECDSA, 256 Bits With SHA-256)
    public void test18Dummy() throws InvalidKeyException {
        Rfc6979 rfc6979 = new Rfc6979();
        String message = "test";
        BigInteger e = new BigInteger("C9AFA9D845BA75166B5C215767B1D6934E50C3DB36E89B127B8A622B120F6721", 16);
        BigInteger z = HashUtils.getSHA256HashAsInteger(message); // H1 = H(m) -> hashed once
        rfc6979.init(e, z);
        List<BigInteger> testCandidates = rfc6979.getTestKs(1);
        // testCandidates.forEach(c -> { out.println(c.toString(16)); });
        // assertEquals("D16B6AE827F17175E040871A1C7EC3500192C4C92677336EC2537ACAEE0008E0", HEX.encode(testCandidates.get(0)));
    }
}
