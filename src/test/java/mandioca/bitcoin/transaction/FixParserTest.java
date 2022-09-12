package mandioca.bitcoin.transaction;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.script.Script;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static mandioca.bitcoin.function.ByteArrayFunctions.hexToByteArrayInputStream;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.script.Script.StandardScripts.isP2pkhScriptPubKey;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertArrayEquals;

// See https://en.bitcoin.it/wiki/Transaction
// See https://en.bitcoin.it/wiki/Raw_Transactions

public class FixParserTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(FixParserTest.class);
    // Also test
    // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "146183a58106544e81573e9f1ad13c7a2d7398dcaf42bb298fcea7858839d3b9" true

    @Test
    public void testParseVersion1Transaction() {
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "1ce75a98e716588ed66d80d7efbf4ab5a493efce93fc25608232654cea0a39c8" true
        String txId = "1ce75a98e716588ed66d80d7efbf4ab5a493efce93fc25608232654cea0a39c8";
        // hex =  (string) The serialized, hex-encoded data for 'txid'
        String rawTx = "0100000001ba26dc6765c544e75b0c8e1b9d4d304bddc5839b9ae544cbbffb0de5bd5b8d9b000000006a47304402201529f6adabd623c1930c8d9601c4ce771f5a600fbb776358da38df379bf55e1202203546b0dcf75b68a33273fca4c43ca58b6e1758a581e89791abb83deab7ddaaa2012103ae329dbe0a3c64fb92b1494f2c524567e399cdd111c497ae4e82deb16750d988ffffffff0250117100000000001976a914f97b598ad05e7ba87a9a0862c834889147415c8188ac40420f00000000001976a914d0fce8f064cd1059a6a11501dd66fe42368572b088ac00000000";
        Tx tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        assertEquals(txId, tx.id());

        assertEquals(1, tx.getDeserializedInputs().length);
        checkInput(tx.getDeserializedInputs()[0]);

        assertEquals(2, tx.getDeserializedOutputs().length);
        check1stOutput(tx.getDeserializedOutputs()[0]);

        // Check locktime
        assertEquals(0, tx.getDeserializedLocktime());
        assertTrue(tx.isLocktimeIgnored.get());
    }


    private void checkInput(TxIn txIn) {
        String expectedPreviousTxIdAsHex = "9b8d5bbde50dfbbfcb44e59a9b83c5dd4b304d9d1b8e0c5be744c56567dc26ba";
        byte[] expectedPreviousTxId = HEX.decode(expectedPreviousTxIdAsHex);
        byte[] actualPreviousTxId = txIn.previousTransactionId;
        assertArrayEquals(expectedPreviousTxId, actualPreviousTxId);
        String actualPreviousTxIdAsHex = HEX.encode(actualPreviousTxId);
        assertEquals(expectedPreviousTxIdAsHex, actualPreviousTxIdAsHex);

        byte[] scriptSig = txIn.scriptSig.getScriptSig();
        String actualScriptSigHex = HEX.encode(scriptSig);
        String expectedScriptSigHex = "47304402201529f6adabd623c1930c8d9601c4ce771f5a600fbb776358da38df379bf55e1202203546b0dcf75b68a33273fca4c43ca58b6e1758a581e89791abb83deab7ddaaa2012103ae329dbe0a3c64fb92b1494f2c524567e399cdd111c497ae4e82deb16750d988";
        assertEquals(actualScriptSigHex, expectedScriptSigHex);
        String expectedScriptSigAsm = "304402201529f6adabd623c1930c8d9601c4ce771f5a600fbb776358da38df379bf55e1202203546b0dcf75b68a33273fca4c43ca58b6e1758a581e89791abb83deab7ddaaa2"
                + "[ALL] "
                + "03ae329dbe0a3c64fb92b1494f2c524567e399cdd111c497ae4e82deb16750d988";
        String actualScriptSigAsm = txIn.scriptSig.scriptSigAsm();
        assertEquals(expectedScriptSigAsm, actualScriptSigAsm);
    }


    private void check1stOutput(TxOut txOut) {
        long expectedAmount = 7410000L; // 0.07410000 BTC as per $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction
        long actualAmount = txOut.getAmountAsLong();
        assertEquals(expectedAmount, actualAmount);
        Script script = txOut.getScriptPubKey();
        assertTrue(isP2pkhScriptPubKey.test(script));
        String expectedAsm = "OP_DUP OP_HASH160 f97b598ad05e7ba87a9a0862c834889147415c81 OP_EQUALVERIFY OP_CHECKSIG";
        assertEquals(expectedAsm, script.asm());
    }

}
