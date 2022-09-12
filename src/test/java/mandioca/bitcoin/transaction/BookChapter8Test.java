package mandioca.bitcoin.transaction;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.ecc.Secp256k1Point;
import mandioca.bitcoin.ecc.Secp256k1PrivateKey;
import mandioca.bitcoin.ecc.Signature;
import mandioca.bitcoin.script.Script;
import mandioca.bitcoin.script.processing.OpCodeFunction;
import mandioca.bitcoin.script.processing.SigHashType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import static mandioca.bitcoin.function.ByteArrayFunctions.hexToByteArrayInputStream;
import static mandioca.bitcoin.function.ByteArrayFunctions.toByteArrayInputStream;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.network.NetworkType.MAINNET;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.transaction.TransactionSerializer.VERSION_1;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static mandioca.bitcoin.util.VarintUtils.VARINT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BookChapter8Test extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(BookChapter8Test.class);

    @BeforeClass
    public static void enableStackDebug() {
        OpCodeFunction.enableStackDebug();  // TODO add this to appropriate classes
    }

    @Ignore // TODO this TX not in TESTNET blockchain
    @Test
    public void testSigHash() {
        // From programmingbitcoin/code-ch08/tx.py
        // def test_sig_hash(self):
        //        tx = TxFetcher.fetch('452c629d67e41baec3ac6f04fe744b4b9617f8f859c63b3002f8684e7a4fee03')
        //        want = int('27e0c5994dec7824e56dec6b2fcb342eb7cdb0d0957c2fce9882f715e85d81a6', 16)
        //        self.assertEqual(tx.sig_hash(0), want)
        String txId = "452c629d67e41baec3ac6f04fe744b4b9617f8f859c63b3002f8684e7a4fee03";
        String rawTx = localTxCache.get(txId);
        Tx tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        BigInteger sigHash = tx.sigHash(0);
        BigInteger expectedSigHash = new BigInteger(1, HEX.decode("27e0c5994dec7824e56dec6b2fcb342eb7cdb0d0957c2fce9882f715e85d81a6"));
        assertEquals(expectedSigHash, sigHash);
    }

    @Ignore  // TODO this TX not in TESTNET blockchain
    @Test
    public void textVerifyP2pkh1() {
        // From programmingbitcoin/code-ch08/tx.py
        //     def test_verify_p2pkh(self):
        //        tx = TxFetcher.fetch('452c629d67e41baec3ac6f04fe744b4b9617f8f859c63b3002f8684e7a4fee03')
        //        self.assertTrue(tx.verify())
        String txId = "452c629d67e41baec3ac6f04fe744b4b9617f8f859c63b3002f8684e7a4fee03";
        String rawTx = localTxCache.get(txId);
        Tx tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        assertTrue(tx.verify());
    }

    @Test
    public void textVerifyP2pkh2() {
        // From programmingbitcoin/code-ch08/tx.py
        //     def test_verify_p2pkh(self):
        //        tx = TxFetcher.fetch('5418099cc755cb9dd3ebc6cf1a7888ad53a1a3beb5a025bce89eb1bf7f1650a2', testnet=True)
        //        self.assertTrue(tx.verify())
        String txId = "5418099cc755cb9dd3ebc6cf1a7888ad53a1a3beb5a025bce89eb1bf7f1650a2";
        String rawTx = localTxCache.get(txId);
        Tx tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        assertTrue(tx.verify());
    }

    @Test
    public void textVerifyP2pkh3() {
        // Version1 tx (Dec 30, 2019 2:40:05 PM)  3 inputs, 2 outputs
        String txId = "1e70a107433404f16a207d7a5722f29887a2823f35926f4a65cb1968f42e5eb8";
        Tx tx = TxFetcher.fetchRawTx(txId, true, TESTNET3);
        assertTrue(tx.verify());
    }

    @Test
    public void textVerifyP2pkh4() {
        // Version1 tx (Dec 30, 2019 2:40:05 PM)  13 inputs, 6 outputs
        String txId = "4dc73742dcba12a9804a38b799c6f25145eb86450e4498319e11fda58a47af82";
        Tx tx = TxFetcher.fetchRawTx(txId, true, TESTNET3);
        assertTrue(tx.verify());
    }

    @Ignore  // TODO this TX not in TESTNET blockchain
    @Test
    public void textVerifyP2sh1() {
        // From programmingbitcoin/code-ch08/tx.py
        //    def test_verify_p2sh(self):
        //        tx = TxFetcher.fetch('46df1a9484d0a81d03ce0ee543ab6e1a23ed06175c104a178268fad381216c2b')
        //        self.assertTrue(tx.verify())
        String txId = "46df1a9484d0a81d03ce0ee543ab6e1a23ed06175c104a178268fad381216c2b";
        String rawTx = localTxCache.get(txId);
        Tx tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        assertTrue(tx.verify());
    }

    @Test
    public void testSignInput() {
        // From programmingbitcoin/code-ch08/tx.py
        // def test_sign_input(self):
        //  private_key = PrivateKey(secret=8675309)
        //  stream = BytesIO(bytes.fromhex('010000000199a24308080ab26e6fb65c4eccfadf76749bb5bfa8cb08f291320b3c21e56f0d0d00000000ffffffff02408af701000000001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac80969800000000001976a914507b27411ccf7f16f10297de6cef3f291623eddf88ac00000000'))
        //  tx_obj = Tx.parse(stream, testnet=True)
        //  self.assertTrue(tx_obj.sign_input(0, private_key))
        //  want = '010000000199a24308080ab26e6fb65c4eccfadf76749bb5bfa8cb08f291320b3c21e56f0d0d0000006b4830450221008ed46aa2cf12d6d81065bfabe903670165b538f65ee9a3385e6327d80c66d3b502203124f804410527497329ec4715e18558082d489b218677bd029e7fa306a72236012103935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67ffffffff02408af701000000001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac80969800000000001976a914507b27411ccf7f16f10297de6cef3f291623eddf88ac00000000'
        //  self.assertEqual(tx_obj.serializeInternal().hex(), want)
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(BigInteger.valueOf(8675309));
        byte[] rawTx = HEX.decode("010000000199a24308080ab26e6fb65c4eccfadf76749bb5bfa8cb08f291320b3c21e56f0d0d00000000ffffffff02408af701000000001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac80969800000000001976a914507b27411ccf7f16f10297de6cef3f291623eddf88ac00000000");
        Tx txObj = Tx.parse(toByteArrayInputStream.apply(rawTx), TESTNET3);
        boolean isSigned = txObj.signInputs(new int[]{0}, privateKey);
        assertTrue(isSigned);
        String expectedSerializedHex = "010000000199a24308080ab26e6fb65c4eccfadf76749bb5bfa8cb08f291320b3c21e56f0d0d0000006b4830450221008ed46aa2cf12d6d81065bfabe903670165b538f65ee9a3385e6327d80c66d3b502203124f804410527497329ec4715e18558082d489b218677bd029e7fa306a72236012103935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67ffffffff02408af701000000001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac80969800000000001976a914507b27411ccf7f16f10297de6cef3f291623eddf88ac00000000";
        String actualSerializedHex = HEX.encode(txObj.serialize());
        assertEquals(expectedSerializedHex, actualSerializedHex);
    }

    @Test
    public void testExercise4ValidateSignatureFromVersion1MainnetTx() {
        String rawHex = "0100000001868278ed6ddfb6c1ed3ad5f8181eb0c7a385aa0836f01d5e4789e6" +
                "bd304d87221a000000db00483045022100dc92655fe37036f47756db8102e0d7d5e28b3beb83a8" +
                "fef4f5dc0559bddfb94e02205a36d4e4e6c7fcd16658c50783e00c341609977aed3ad00937bf4e" +
                "e942a8993701483045022100da6bee3c93766232079a01639d07fa869598749729ae323eab8eef" +
                "53577d611b02207bef15429dcadce2121ea07f233115c6f09034c0be68db99980b9a6c5e754022" +
                "01475221022626e955ea6ea6d98850c994f9107b036b1334f18ca8830bfff1295d21cfdb702103" +
                "b287eaf122eea69030a0e9feed096bed8045c8b98bec453e1ffac7fbdbd4bb7152aeffffffff04" +
                "d3b11400000000001976a914904a49878c0adfc3aa05de7afad2cc15f483a56a88ac7f40090000" +
                "0000001976a914418327e3f3dda4cf5b9089325a4b95abdfa0334088ac722c0c00000000001976" +
                "a914ba35042cfe9fc66fd35ac2224eebdafd1028ad2788acdc4ace020000000017a91474d691da" +
                "1574e6b3c192ecfb52cc8984ee7b6c568700000000";

        byte[] rawTx = HEX.decode(rawHex);
        Tx tx = Tx.parse(toByteArrayInputStream.apply(rawTx), MAINNET);

        byte[] sec = HEX.decode("03b287eaf122eea69030a0e9feed096bed8045c8b98bec453e1ffac7fbdbd4bb71");
        byte[] der = HEX.decode("3045022100da6bee3c93766232079a01639d07fa869598749729ae323eab8eef53577d611b02207bef15429dcadce2121ea07f233115c6f09034c0be68db99980b9a6c5e754022");

        byte[] rawRedeem = HEX.decode("475221022626e955ea6ea6d98850c994f9107b036b1334f18ca8830bfff1295d21cfdb702103b287eaf122eea69030a0e9feed096bed8045c8b98bec453e1ffac7fbdbd4bb7152ae");
        Script redeemScript = Script.parse(rawRedeem);

        final byte[] txWithRedeemScript;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            baos.write(VERSION_1);

            baos.write(VARINT.encode(tx.getDeserializedInputs().length));
            TxIn p2shInputWithEmptyScriptSig = tx.getDeserializedInputs()[0];

            TxIn p2shInputWithRedeemScript = new TxIn(
                    p2shInputWithEmptyScriptSig.previousTransactionId,
                    p2shInputWithEmptyScriptSig.previousTransactionIndex,
                    redeemScript,  //  ScriptPubKey is replaced by a RedeemScript
                    p2shInputWithEmptyScriptSig.sequence);
            baos.write(p2shInputWithRedeemScript.serialize());

            baos.write(VARINT.encode(tx.getDeserializedOutputs().length));
            for (TxOut txOut : tx.getDeserializedOutputs()) {
                baos.write(txOut.serialize());
            }
            baos.write(tx.locktime);
            baos.write(SigHashType.SIGHASH_ALL.littleEndian());

            txWithRedeemScript = baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error validating signature", e);
        }

        BigInteger z = new BigInteger(1, hash256.apply(txWithRedeemScript));
        Secp256k1Point publicKey = Secp256k1Point.parse(sec);
        Signature signature = Signature.parse(der);
        assertTrue(publicKey.verify(z, signature));
    }

    @AfterClass
    public static void disableStackDebug() {
        OpCodeFunction.disableStackDebug();  // TODO add this to appropriate classes
    }
}
