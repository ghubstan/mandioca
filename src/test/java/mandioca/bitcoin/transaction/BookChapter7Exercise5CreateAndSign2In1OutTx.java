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


public class BookChapter7Exercise5CreateAndSign2In1OutTx extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(BookChapter7Exercise5CreateAndSign2In1OutTx.class);

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
        String expectedSignedTxHex = "01000000022f2afe57bde0822c793604baae834f2cd26155bf1c0d37480212c107e75cd0110100" +
                "00006a47304402204cc5fe11b2b025f8fc9f6073b5e3942883bbba266b71751068badeb8f11f03" +
                "64022070178363f5dea4149581a4b9b9dbad91ec1fd990e3fa14f9de3ccb421fa5b26901210393" +
                "5581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67ffffffff153db020" +
                "2de27e7944c7fd651ec1d0fab1f1aaed4b0da60d9a1b06bd771ff651010000006b483045022100" +
                "b7a938d4679aa7271f0d32d83b61a85eb0180cf1261d44feaad23dfd9799dafb02205ff2f366dd" +
                "d9555f7146861a8298b7636be8b292090a224c5dc84268480d8be1012103935581e52c354cd2f4" +
                "84fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67ffffffff01d0754100000000001976a9" +
                "14ad346f8eb57dee9a37981716e498120ae80e44f788ac00000000";
        out.println("Expected Signed Raw = " + expectedSignedTxHex);
    }

    @Test
    public void testExercise5() {
        final String prevTx1IdHex = "11d05ce707c1120248370d1cbf5561d22c4f83aeba0436792c82e0bd57fe2a2f";
        byte[] prevTx1Id = HEX.decode(prevTx1IdHex);
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "11d05ce707c1120248370d1cbf5561d22c4f83aeba0436792c82e0bd57fe2a2f" true
        // Hex:  0100000001982e32eec93006501d6b933d7d0d47042d1c8d8e33dc394e2f9097e8652867dd120000006a47304402207d1eaffeb78e4a90129d8df0c9ad6d4d434abfa4aa433fcc0b57b77ebbea876602205e68b3c5385ab7d34f3932bb5093e8e1f90d6e3eb962cdfcc3db1430f3225ce0012103c0f7c36b1105c663eebdaa00ce200acfba4f6e6454e6b200360776aae1a3508dffffffff0280841e00000000001976a914507b27411ccf7f16f10297de6cef3f291623eddf88ace0fd1c00000000001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac00000000
        int prevTx1Index = 1;
        TxIn txIn1 = new TxIn(prevTx1Id, intToBytes.apply(prevTx1Index));
        assertEquals("11d05ce707c1120248370d1cbf5561d22c4f83aeba0436792c82e0bd57fe2a2f", HEX.encode(txIn1.previousTransactionId));
        assertArrayEquals(TxIn.SEQUENCE_0xFFFFFFFF, txIn1.sequence); // -1 little endian

        final String prevTx2IdHex = "51f61f77bd061b9a0da60d4bedaaf1b1fad0c11e65fdc744797ee22d20b03d15";
        byte[] prevTx2Id = HEX.decode(prevTx2IdHex);
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "51f61f77bd061b9a0da60d4bedaaf1b1fad0c11e65fdc744797ee22d20b03d15" true
        // Hex:  010000000141b0155a687ddfe4663277fc824381be6500fe0fca1cc9b8ef23aa6ed82df275070000006a4730440220344f4e3a694cec208efa1363520a667c9a9507ca50300610138da93e4f45ba1202203d3ae276310b9eeebcbb321d74b1e10998bb7a5ce25ca739d806c48bb0a3b395012103cf540c0d01cc96b95716d0f72556273941a767b109a1def6e63bf00c6f85e93bffffffff0280841e00000000001976a914507b27411ccf7f16f10297de6cef3f291623eddf88ac009f2400000000001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac00000000
        int prevTx2Index = 1;
        TxIn txIn2 = new TxIn(prevTx2Id, intToBytes.apply(prevTx2Index));
        assertEquals("51f61f77bd061b9a0da60d4bedaaf1b1fad0c11e65fdc744797ee22d20b03d15", HEX.encode(txIn2.previousTransactionId));
        assertArrayEquals(TxIn.SEQUENCE_0xFFFFFFFF, txIn2.sequence); // -1 little endian

        long targetAmount = btcToSatoshis.apply("0.0429");
        String targetAddress = "mwJn1YPMq7y5F8J3LkC5Hxg9PHyZ5K4cFv";
        Script targetScript = addressToP2pkhScript.apply(targetAddress);

        TxOut targetOutput = new TxOut(longToBytes.apply(targetAmount), targetScript);
        assertEquals(4290000L, targetOutput.getAmountAsLong());

        // TODO be careful txIns ordering is not backwards
        TxIn[] txIns = new TxIn[]{txIn1, txIn2};
        Tx txObj = new Tx(1, txIns, new TxOut[]{targetOutput}, 0, TESTNET3);

        BigInteger secret = new BigInteger("8675309");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        boolean isSigned = txObj.signAllInputs(privateKey);
        assertTrue(isSigned);

        /*
        // Sign the one input, or loop through all and sign additional inputs as required (TODO)
        for (int i = 0; i < txIns.length; i++) {
            boolean isSigned = txObj.signInput(i, privateKey);
            assertTrue(isSigned);
        }
        txObj.serializeSignedTxInputs();   // Re assign the txInputs field on the TX object
        */

        String expectedSignedTxHex = "01000000022f2afe57bde0822c793604baae834f2cd26155bf1c0d37480212c107e75cd0110100" +
                "00006a47304402204cc5fe11b2b025f8fc9f6073b5e3942883bbba266b71751068badeb8f11f03" +
                "64022070178363f5dea4149581a4b9b9dbad91ec1fd990e3fa14f9de3ccb421fa5b26901210393" +
                "5581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67ffffffff153db020" +
                "2de27e7944c7fd651ec1d0fab1f1aaed4b0da60d9a1b06bd771ff651010000006b483045022100" +
                "b7a938d4679aa7271f0d32d83b61a85eb0180cf1261d44feaad23dfd9799dafb02205ff2f366dd" +
                "d9555f7146861a8298b7636be8b292090a224c5dc84268480d8be1012103935581e52c354cd2f4" +
                "84fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67ffffffff01d0754100000000001976a9" +
                "14ad346f8eb57dee9a37981716e498120ae80e44f788ac00000000";

        String actualSignedTxHex = HEX.encode(txObj.serialize());
        // out.println("expectedSignedTxHex = " + expectedSignedTxHex);
        // out.println("actualSignedTxHex   = " + actualSignedTxHex);


        // I know id() from https://live.blockcypher.com/btc-testnet/decodetx/ <expectedSignedTxHex>
        //      "hash": "45e9541741ebf85e5df3b657f24b21c15760992ae0b761e772054a65c9a2ce58"
        String expectedTxObjId = "45e9541741ebf85e5df3b657f24b21c15760992ae0b761e772054a65c9a2ce58";
        assertEquals(expectedTxObjId, txObj.id());

        assertEquals(expectedSignedTxHex, actualSignedTxHex);
    }

    @AfterClass
    public static void disableStackDebug() {
        OpCodeFunction.disableStackDebug();  // TODO add this to appropriate classes
    }
}
