package mandioca.bitcoin.transaction;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.ecc.Secp256k1Point;
import mandioca.bitcoin.ecc.Secp256k1PrivateKey;
import mandioca.bitcoin.ecc.Signature;
import mandioca.bitcoin.rpc.RpcClient;
import mandioca.bitcoin.rpc.RpcCommand;
import mandioca.bitcoin.script.processing.OpCodeFunction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import static mandioca.bitcoin.function.ByteArrayFunctions.hexToByteArrayInputStream;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.transaction.TxFetcher.fetchRawTx;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * https://github.com/jimmysong/programmingbitcoin/issues/63
 * <p>
 * "By far, this chapter wiped me out. Here are my thoughts/experiences/troubles."
 * ...
 * ...
 */
public class BookChapter7Test extends MandiocaTest {

    // TODO there's no p2sh verification test here because the chapter 7 testcase was for mainnet

    private static final Logger log = LoggerFactory.getLogger(BookChapter7Test.class);

    private final RpcClient rpcClient = new RpcClient(true);
    private final RpcCommand rpcCommand = new RpcCommand();

    @BeforeClass
    public static void enableStackDebug() {
        OpCodeFunction.enableStackDebug();  // TODO add this to appropriate classes
    }

    @Test
    public void testExample1() {
        // $BITCOIN_HOME/bitcoin-cli decoderawtransaction "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278afeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600"
        // String txId = "452c629d67e41baec3ac6f04fe744b4b9617f8f859c63b3002f8684e7a4fee03";
        // $BITCOIN_HOME/bitcoin-cli -testnet gettransaction "452c629d67e41baec3ac6f04fe744b4b9617f8f859c63b3002f8684e7a4fee03"
        String rawTx = "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278afeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600";
        Tx tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        String expectedTxId = "452c629d67e41baec3ac6f04fe744b4b9617f8f859c63b3002f8684e7a4fee03";
        assertEquals(expectedTxId, tx.id());
        String localCachedTxHex = localTxCache.get(tx.id());
        String parsedTxHex = HEX.encode(tx.serialize());
        assertEquals(localCachedTxHex, parsedTxHex);
    }

    @Test
    public void testExample1ForTxId_3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86() {
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86" true
        // Hex:  01000000018fe1ada6f06c2fd2080f0ae73fbbbd14061ca572eeaa6342a66191d6777c7c70010000008b483045022100c4558ec0232c4aaafad956b777d73637adb6b5eaf9ba1c8de86607043bc96bd6022075715ea626bd2ea281e531ac34816132b80995a7541ad2a33e233eea78572a4c0141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff02d8e3b40b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388acce7b0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000
        // TxId:        3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86
        String txId = "3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86";
        Tx tx = fetchRawTx(txId, true, TESTNET3);
        assertTrue(tx.fee() > 0L);
        assertEquals(20000L, tx.fee());
        String expectedSerializedHex = "01000000018fe1ada6f06c2fd2080f0ae73fbbbd14061ca572eeaa6342a66191d6777c7c70010000008b483045022100c4558ec0232c4aaafad956b777d73637adb6b5eaf9ba1c8de86607043bc96bd6022075715ea626bd2ea281e531ac34816132b80995a7541ad2a33e233eea78572a4c0141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff02d8e3b40b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388acce7b0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000";
        String actualSerializedHex = HEX.encode(tx.serialize());
        assertEquals(expectedSerializedHex, actualSerializedHex);
    }

    @Test
    public void testExample3() {
        // Empty ScriptSig is replaced with ScriptPubKey from a TxIn, then hash type
        // SIGHASH_ALL (a 4 byte, little endian encoded 0x01) is appended.
        byte[] modifiedTx = HEX.decode("0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000001976a914a802fc56c704ce87c42d7c92eb75e7896bdc41ae88acfeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac1943060001000000");
        // Now we can get the signature hash z.
        byte[] hash = hash256.apply(modifiedTx);
        BigInteger z = new BigInteger(1, hash);
        assertEquals("27e0c5994dec7824e56dec6b2fcb342eb7cdb0d0957c2fce9882f715e85d81a6", HEX.bigIntToHex.apply(z));
    }

    @Test
    public void testExample4() {
        // Building on example 3...

        // Empty ScriptSig is replaced with ScriptPubKey from a TxIn, then hash type
        // SIGHASH_ALL (a 4 byte, little endian encoded 0x01) is appended.
        byte[] modifiedTx = HEX.decode("0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000001976a914a802fc56c704ce87c42d7c92eb75e7896bdc41ae88acfeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac1943060001000000");
        byte[] hash = hash256.apply(modifiedTx);
        BigInteger z = new BigInteger(1, hash);
        assertEquals("27e0c5994dec7824e56dec6b2fcb342eb7cdb0d0957c2fce9882f715e85d81a6", HEX.bigIntToHex.apply(z));

        // Public Key (Secp256k1Point) SEC
        byte[] sec = HEX.decode("0349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278a");

        // Signature DER is in the ScriptSig.
        byte[] der = HEX.decode("3045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed");
        Secp256k1Point publicKey = Secp256k1Point.parse(sec);
        Signature signature = Signature.parse(der);

        // Verify the signature.
        assertTrue(publicKey.verify(z, signature));
    }

    @Test
    public void testVerifyP2pkh1() {
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86" true
        // Hex:  01000000018fe1ada6f06c2fd2080f0ae73fbbbd14061ca572eeaa6342a66191d6777c7c70010000008b483045022100c4558ec0232c4aaafad956b777d73637adb6b5eaf9ba1c8de86607043bc96bd6022075715ea626bd2ea281e531ac34816132b80995a7541ad2a33e233eea78572a4c0141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff02d8e3b40b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388acce7b0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000
        // TxId:        3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86
        String txId = "3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86";
        Tx tx = fetchRawTx(txId, true, TESTNET3);
        assertEquals(txId, tx.id());
        assertTrue(tx.fee() > 0L);
        assertEquals(20000L, tx.fee());
        assertTrue(tx.verify());
    }

    @Test
    public void testVerifyP2pkh2() {
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "5418099cc755cb9dd3ebc6cf1a7888ad53a1a3beb5a025bce89eb1bf7f1650a2" true
        // From programmingbitcoin/code-ch07/tx.py
        // def test_verify_p2pkh(self):
        //        TX.ID 452c629d67e41baec3ac6f04fe744b4b9617f8f859c63b3002f8684e7a4fee03 is NOT in testnet
        //        SKIP tx = TxFetcher.fetch('452c629d67e41baec3ac6f04fe744b4b9617f8f859c63b3002f8684e7a4fee03')
        //        SKIP self.assertTrue(tx.verify())
        //
        //        TX.ID 5418099cc755cb9dd3ebc6cf1a7888ad53a1a3beb5a025bce89eb1bf7f1650a2 is in testnet
        //        tx = TxFetcher.fetch('5418099cc755cb9dd3ebc6cf1a7888ad53a1a3beb5a025bce89eb1bf7f1650a2', testnet=True)
        //        self.assertTrue(tx.verify())
        String txId = "5418099cc755cb9dd3ebc6cf1a7888ad53a1a3beb5a025bce89eb1bf7f1650a2";
        Tx tx = fetchRawTx(txId, true, TESTNET3);
        assertEquals(txId, tx.id());
        assertTrue(tx.verify());
    }

    @Test
    public void testSignInput() {
        // From programmingbitcoin/code-ch08/tx.py
        // def test_sign_input(self):
        //        private_key = PrivateKey(secret=8675309)
        //        stream = BytesIO(bytes.fromhex('010000000199a24308080ab26e6fb65c4eccfadf76749bb5bfa8cb08f291320b3c21e56f0d0d00000000ffffffff02408af701000000001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac80969800000000001976a914507b27411ccf7f16f10297de6cef3f291623eddf88ac00000000'))
        //        tx_obj = Tx.parse(stream, testnet=True)
        //        self.assertTrue(tx_obj.sign_input(0, private_key))
        //        want = '010000000199a24308080ab26e6fb65c4eccfadf76749bb5bfa8cb08f291320b3c21e56f0d0d0000006b4830450221008ed46aa2cf12d6d81065bfabe903670165b538f65ee9a3385e6327d80c66d3b502203124f804410527497329ec4715e18558082d489b218677bd029e7fa306a72236012103935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67ffffffff02408af701000000001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac80969800000000001976a914507b27411ccf7f16f10297de6cef3f291623eddf88ac00000000'
        //        self.assertEqual(tx_obj.serializeInternal().hex(), want)
        BigInteger secret = new BigInteger("8675309");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        String rawTx = "010000000199a24308080ab26e6fb65c4eccfadf76749bb5bfa8cb08f291320b3c21e56f0d0d00000000ffffffff02408af701000000001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac80969800000000001976a914507b27411ccf7f16f10297de6cef3f291623eddf88ac00000000";
        Tx txObj = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        boolean isSigned = txObj.signInputs(new int[]{0}, privateKey);
        assertTrue(isSigned);

        String expectedSerializedHex = "010000000199a24308080ab26e6fb65c4eccfadf76749bb5bfa8cb08f291320b3c21e56f0d0d0000006b4830450221008ed46aa2cf12d6d81065bfabe903670165b538f65ee9a3385e6327d80c66d3b502203124f804410527497329ec4715e18558082d489b218677bd029e7fa306a72236012103935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67ffffffff02408af701000000001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac80969800000000001976a914507b27411ccf7f16f10297de6cef3f291623eddf88ac00000000";
        assertEquals(expectedSerializedHex, HEX.encode(txObj.serialize()));
    }

    @AfterClass
    public static void disableStackDebug() {
        OpCodeFunction.disableStackDebug();  // TODO add this to appropriate classes
    }
}
