package mandioca.bitcoin.transaction;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.ecc.Secp256k1PrivateKey;
import mandioca.bitcoin.script.Script;
import mandioca.bitcoin.script.processing.OpCodeFunction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import static java.lang.System.out;
import static mandioca.bitcoin.function.ByteArrayFunctions.intToBytes;
import static mandioca.bitcoin.function.ByteArrayFunctions.longToBytes;
import static mandioca.bitcoin.function.CurrencyFunctions.btcToSatoshis;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.script.Script.StandardScripts.addressToP2pkhScript;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.*;

public class BookChapter7Exercise4CreateAndSign1In1OutTx extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(BookChapter7Exercise4CreateAndSign1In1OutTx.class);

    @BeforeClass
    public static void enableStackDebug() {
        OpCodeFunction.enableStackDebug();  // TODO add this to appropriate classes
    }

    @Ignore
    @Test
    public void testDecodeExpectedRaw() {
        // The book has many typos, such as incorrect addresses in the examples.
        // This is how I find the correct test data:
        // https://live.blockcypher.com/btc-testnet/decodetx/ <expectedSignedTxHex>
        String expectedSignedTxHex = "01000000011c5fb4a35c40647bcacfeffcb8686f1e9925774c07a1dd26f6551f67bcc4a1750100" +
                "00006b483045022100a08ebb92422b3599a2d2fcdaa11f8f807a66ccf33e7f4a9ff0a3c51f1b1e" +
                "c5dd02205ed21dfede5925362b8d9833e908646c54be7ac6664e31650159e8f69b6ca539012103" +
                "935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67ffffffff024042" +
                "0f00000000001976a9141ec51b3654c1f1d0f4929d11a1f702937eaf50c888ac9fbb0d00000000" +
                "001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac00000000";
        out.println("Expected Signed Raw = " + expectedSignedTxHex);
    }

    @Test
    public void testExercise4() {
        final String prevTxIdHex = "75a1c4bc671f55f626dda1074c7725991e6f68b8fcefcfca7b64405ca3b45f1c";
        byte[] prevTxId = HEX.decode(prevTxIdHex);
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "75a1c4bc671f55f626dda1074c7725991e6f68b8fcefcfca7b64405ca3b45f1c" true
        // Hex:  0100000001982e32eec93006501d6b933d7d0d47042d1c8d8e33dc394e2f9097e8652867dd0d0000006b483045022100be0a2f95c1ad0f7f00a42195c4510e38041c0d095aed589be19d6173432cc225022028c0bda892da40fc9d514b5764b05234f045865fd6e0b6bf619f26405971f3770121035497d852f416b4844cb239686deb16aadf3e20a7607f3872a8b83b7131175735ffffffff0280841e00000000001976a914507b27411ccf7f16f10297de6cef3f291623eddf88ace0fd1c00000000001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac00000000
        int prevTxIndex = 1;
        TxIn txIn = new TxIn(prevTxId, intToBytes.apply(prevTxIndex));
        assertEquals("75a1c4bc671f55f626dda1074c7725991e6f68b8fcefcfca7b64405ca3b45f1c", HEX.encode(txIn.previousTransactionId));
        assertArrayEquals(TxIn.SEQUENCE_0xFFFFFFFF, txIn.sequence); // -1 little endian

        long targetAmount = btcToSatoshis.apply("0.01");
        // https://testnet.smartbit.com.au/address/miKegze5FQNCnGw6PKyqUbYUeBa4x2hFeM
        String targetAddress = "miKegze5FQNCnGw6PKyqUbYUeBa4x2hFeM";
        Script targetScript = addressToP2pkhScript.apply(targetAddress);
        assertEquals("OP_DUP OP_HASH160 1ec51b3654c1f1d0f4929d11a1f702937eaf50c8 OP_EQUALVERIFY OP_CHECKSIG", targetScript.asm());
        TxOut targetOutput = new TxOut(longToBytes.apply(targetAmount), targetScript);
        assertEquals(1_000_000L, targetOutput.getAmountAsLong());
        // I know asm from $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "<prevTxId>" true
        String expectedTargetScriptPubKeyAsm = "OP_DUP OP_HASH160 1ec51b3654c1f1d0f4929d11a1f702937eaf50c8 OP_EQUALVERIFY OP_CHECKSIG";
        assertEquals(expectedTargetScriptPubKeyAsm, targetOutput.getScriptPubKey().asm());

        long changeAmount = btcToSatoshis.apply("0.00899999");
        // https://testnet.smartbit.com.au/address/mzx5YhAH9kNHtcN481u6WkjeHjYtVeKVh2
        String changeAddress = "mzx5YhAH9kNHtcN481u6WkjeHjYtVeKVh2";
        Script changeScript = addressToP2pkhScript.apply(changeAddress);
        // I know asm from $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "<prevTxId>" true
        assertEquals("OP_DUP OP_HASH160 d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f OP_EQUALVERIFY OP_CHECKSIG", changeScript.asm());
        TxOut changeOutput = new TxOut(longToBytes.apply(changeAmount), changeScript);
        assertEquals(899_999L, changeOutput.getAmountAsLong());
        String expectedChangeScriptPubKeyAsm = "OP_DUP OP_HASH160 d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f OP_EQUALVERIFY OP_CHECKSIG";
        assertEquals(expectedChangeScriptPubKeyAsm, changeOutput.getScriptPubKey().asm());

        // TODO remember the python array append method was blocking me for days:
        //          tx_outs.append (changeOutput)
        //          tx_outs.append (targetOutput)
        //      BUG:  I was translating this to java as:
        //          TxOut[] txOuts = new TxOut[]{changeOutput, targetOutput};
        //      not TxOut[] txOuts = new TxOut[]{targetOutput, changeOutput};
        //      This is why my tx.serializeInternal was not working... I had the outputs ordering backwards.
        //      In the future, remember this if raw-decode is showing revered addresses in the ins/outs.

        TxOut[] txOuts = new TxOut[]{targetOutput, changeOutput};
        int version = 1;
        Tx txObj = new Tx(version, new TxIn[]{txIn}, txOuts, 0, TESTNET3);

        BigInteger secret = new BigInteger("8675309");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);

        // Sign the one input, or loop through all and sign additional inputs as required (TODO)
        boolean isSigned = txObj.signInputs(new int[]{0}, privateKey);
        assertTrue(isSigned);

        String expectedSignedTxHex = "01000000011c5fb4a35c40647bcacfeffcb8686f1e9925774c07a1dd26f6551f67bcc4a1750100" +
                "00006b483045022100a08ebb92422b3599a2d2fcdaa11f8f807a66ccf33e7f4a9ff0a3c51f1b1e" +
                "c5dd02205ed21dfede5925362b8d9833e908646c54be7ac6664e31650159e8f69b6ca539012103" +
                "935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67ffffffff024042" +
                "0f00000000001976a9141ec51b3654c1f1d0f4929d11a1f702937eaf50c888ac9fbb0d00000000" +
                "001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac00000000";
        String actualSignedTxHex = HEX.encode(txObj.serialize());

        // I know id() from https://live.blockcypher.com/btc-testnet/decodetx/ <expectedSignedTxHex>
        //      "hash": "b2254147e6ea143b098b57d8d336e486d75675412f33455d9a72b72f42cc605e"
        String expectedTxObjId = "b2254147e6ea143b098b57d8d336e486d75675412f33455d9a72b72f42cc605e";
        assertEquals(expectedTxObjId, txObj.id());


        // out.println("expectedSignedTxHex = " + expectedSignedTxHex);
        // out.println("actualSignedTxHex   = " + actualSignedTxHex);
        assertEquals(expectedSignedTxHex, actualSignedTxHex);

    }

    @AfterClass
    public static void disableStackDebug() {
        OpCodeFunction.disableStackDebug();  // TODO add this to appropriate classes
    }
}
