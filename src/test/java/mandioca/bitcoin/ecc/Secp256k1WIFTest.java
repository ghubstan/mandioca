package mandioca.bitcoin.ecc;

import mandioca.bitcoin.address.Address;
import org.junit.Test;

import java.math.BigInteger;

import static mandioca.bitcoin.address.AddressFactory.publicKeyToP2pkhAddress;
import static mandioca.bitcoin.function.BigIntegerFunctions.HEX_RADIX;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

// TODO pass NetworkType argument, not boolean 'testnet',
//      but this would mean creating a bitcoin pkg dependency here in ecc.
//          Solution: move ecc to bitcoin pkg

public class Secp256k1WIFTest extends AbstractSecp256k1Test {

    @Test  // Test params from Jimmy Song book, Chapter 4, Exercise 6
    public void testPrivateKeyWIF1() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(BigInteger.valueOf(5003));
        String wif = privateKey.getWif(true, true);
        String expectedWif = "cMahea7zqjxrtgAbB7LSGbcQUr1uX1ojuat9jZodMN8rFTv2sfUK";
        assertEquals(expectedWif, wif);
    }


    @Test  // Test params from Jimmy Song book, Chapter 4, Exercise 6
    public void testPrivateKeyWIF2() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(BigInteger.valueOf(2021).pow(5));
        String wif = privateKey.getWif(false, true);
        String expectedWif = "91avARGdfge8E4tZfYLoxeJ5sGBdNJQH4kvjpWAxgzczjbCwxic";
        assertEquals(expectedWif, wif);
    }

    @Test  // Test params from Jimmy Song book, Chapter 4, Exercise 6
    public void testPrivateKeyWIF3() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(new BigInteger("54321deadbeef", HEX_RADIX));
        String wif = privateKey.getWif(true, false);
        String expectedWif = "KwDiBf89QgGbjEhKnhXJuH7LrciVrZi3qYjgiuQJv1h8Ytr2S53a";
        assertEquals(expectedWif, wif);
    }

    @Test
    public void testWifToPrivateKey1() {
        // Test data and psuedo-algo from https://en.bitcoin.it/wiki/Wallet_import_format
        // WIF to private key
        //  1 - Take a Wallet Import Format string
        //          5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ
        //  2 - Convert it to a byte string using Base58Check encoding
        //          800C28FCA386C7A227600B2FE50B7CAE11EC86D3BF1FBE471BE89827E19D72AA1D507A5B8D
        //          800c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d507a5b8d
        //  3 - Drop the last 4 checksum bytes from the byte string
        //          800C28FCA386C7A227600B2FE50B7CAE11EC86D3BF1FBE471BE89827E19D72AA1D
        //          800c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d
        //  4 - Drop the first byte ( [if mainnet address (sic)] it should be 0x80 ).
        //      If the private key corresponded to a compressed public key, also drop the last byte (it should be 0x01).
        //      If it corresponded to a compressed public key, the WIF string will have started with K or L instead
        //      of 5 (or c instead of 9 on testnet).
        //      This is the private key.
        //          0C28FCA386C7A227600B2FE50B7CAE11EC86D3BF1FBE471BE89827E19D72AA1D
        //          0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d
        String wif = "5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ";  // uncompressed, mainnet WIF
        byte[] rawPrivateKey = Secp256k1PrivateKey.wifToRawPrivateKey(wif, false);
        String expectedRawPrivateKeyHex = HEX.encode(rawPrivateKey);
        assertEquals(expectedRawPrivateKeyHex, HEX.encode(rawPrivateKey));
        BigInteger e = new BigInteger(1, rawPrivateKey);
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(e);
        String newWif = privateKey.getWif(false, false);  // original was an uncompressed, mainnet WIF
        assertEquals(wif, newWif);
    }


    @Test
    public void testWifToPrivateKey2() {
        // TODO move, and create txs using all supported scripts, then support more segwit script types
        // The private key (of the funding utxo 'ffc7416b95ce5340a10b6906e602c7dc25f1e034ed60dd74213ca868252d3a5c')
        // that will sign a new tx.
        String wif = "cRRJCXC7kgrN9bg9hxkDyhFaHbrTDni2tmfw2kMECmcKdixKQxtg";  // compressed, testnet WIF
        Secp256k1PrivateKey privateKey = Secp256k1PrivateKey.wifToPrivateKey(wif, true);
        String newWif = privateKey.getWif(true, true);  // original was an compressed, testnet WIF
        assertEquals(wif, newWif);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        Address address = publicKeyToP2pkhAddress.apply(publicKey, true, TESTNET3);
        // I know the wif for 'n29E94aJPTkyeNqTDh9aQxK5qxpm8Umjbs' by using bitcoin-cli command:
        // $BITCOIN_HOME/bitcoin-cli -testnet dumpprivkey "n29E94aJPTkyeNqTDh9aQxK5qxpm8Umjbs"
        assertEquals("n29E94aJPTkyeNqTDh9aQxK5qxpm8Umjbs", address.value());
    }


}
