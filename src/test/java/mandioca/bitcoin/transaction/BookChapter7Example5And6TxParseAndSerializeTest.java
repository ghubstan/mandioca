package mandioca.bitcoin.transaction;

import mandioca.bitcoin.address.Address;
import mandioca.bitcoin.script.Script;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Integer.*;
import static java.lang.System.out;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static mandioca.bitcoin.address.AddressType.P2PKH;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.script.Script.StandardScripts.isP2pkhScriptPubKey;
import static mandioca.bitcoin.transaction.TxIn.SEQUENCE_0xFEFFFFFF;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertArrayEquals;

/**
 * https://github.com/jimmysong/programmingbitcoin/issues/63
 * <p>
 * "By far, this chapter wiped me out. Here are my thoughts/experiences/troubles."
 * ...
 * ...
 */
public class BookChapter7Example5And6TxParseAndSerializeTest extends AbstractBookChapter7Example5And6Test {

    private static final Logger log = LoggerFactory.getLogger(BookChapter7Example5And6TxParseAndSerializeTest.class);

    // $BITCOIN_HOME/bitcoin-cli decoderawtransaction "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006a47304402207db2402a3311a3b845b038885e3dd889c08126a8570f26a844e3e4049c482a11022010178cdca4129eacbeab7c44648bf5ac1f9cac217cd609d216ec2ebc8d242c0a012103935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67feffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600" true

    @BeforeClass
    public static void testSetupCorrect() {
        assertEquals(copyPastedRawTxHex, rawParts); // make sure handmade tx parts = the copy/pasted decoded tx hex
    }

    @Test
    public void testTxParseAndSerialize() {
        Tx tx = Tx.parse(stream.apply(rawBytes), TESTNET3);
        assertEquals("3fad891a9d0deda19862a073d35611d57089377adb4585e6c457bcb0f4470821", tx.id());

        assertArrayEquals(HEX.decode(version1Hex), tx.version); // little endian byte array comparison
        assertEquals(Long.valueOf(1), bytesToLong.apply(reverse.apply(tx.version))); // long(1) comparison

        assertEquals(1, tx.getDeserializedInputs().length);
        assertEquals(parseInt(numTxInputsHex, 10), tx.getDeserializedInputs().length); // test handmade tx hex
        assertEquals(2, tx.getDeserializedOutputs().length);
        assertEquals(parseInt(numTxOutputsHex, 10), tx.getDeserializedOutputs().length); // test handmade tx hex

        assertArrayEquals(HEX.decode(locktimeHex), tx.locktime); // little endian byte array comparison
        assertEquals(Long.valueOf(410393), bytesToLong.apply(reverse.apply(tx.locktime))); // long(410393) comparison

        byte[] serializedTx = tx.serialize();
        assertEquals(copyPastedRawTxHex, HEX.encode(serializedTx)); // re-serialized hex matches copy/pasted?
        out.println("TX PARSE & SERIALIZE TEST PASSED");
    }

    @Test
    public void testTxInParseAndSerialize() {
        // "vin": [
        //    {
        //      "txid": "d1c789a9c60383bf715f3f6ad9d14b91fe55f3deb369fe5d9280cb1a01793f81",
        //      "vout": 0,
        //      "scriptSig": {
        //        "asm": "304402207db2402a3311a3b845b038885e3dd889c08126a8570f26a844e3e4049c482a11022010178cdca4129eacbeab7c44648bf5ac1f9cac217cd609d216ec2ebc8d242c0a[ALL] 03935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67",
        //        "hex": "47304402207db2402a3311a3b845b038885e3dd889c08126a8570f26a844e3e4049c482a11022010178cdca4129eacbeab7c44648bf5ac1f9cac217cd609d216ec2ebc8d242c0a012103935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67"
        //      },
        //      "sequence": 4294967294
        //    }
        //  ]
        Tx tx = Tx.parse(stream.apply(rawBytes), TESTNET3);
        assertEquals("3fad891a9d0deda19862a073d35611d57089377adb4585e6c457bcb0f4470821", tx.id());
        TxIn txIn = tx.getDeserializedInputs()[0];
        checkTxInSequence(txIn);
        checkTxInPreviousTransactionId(txIn);
        checkTxInPreviousTransactionIndex(txIn);
        checkTxInScriptSig(txIn);
        checkTxInSerialization(txIn);
    }


    private void checkTxInSequence(TxIn txIn) {
        assertArrayEquals(SEQUENCE_0xFEFFFFFF, reverse.apply(txIn.sequence));
        // Does parsed sequence as a long match 4294967294, copy/pasted from bitcoind json response?
        long bitcoindSequenceValue = 4294967294L;
        long parsedSequenceAsLong = bytesToLong.apply(txIn.sequence);
        assertEquals(bitcoindSequenceValue, parsedSequenceAsLong);
    }

    private void checkTxInPreviousTransactionId(TxIn txIn) {
        // Does parsed txInPrevTxId match the txInPrevTxId copy/pasted from the bitcoind json response?
        assertEquals(SIZE, txIn.previousTransactionId.length);
        String expectedPreviousTxIdHex = "d1c789a9c60383bf715f3f6ad9d14b91fe55f3deb369fe5d9280cb1a01793f81";
        assertEquals(expectedPreviousTxIdHex, HEX.encode(txIn.previousTransactionId));

        // Does parsed txInPrevTxId match the txInPrevTxId copy/pasted from the Jimmy Song book example?
        byte[] previousTxIdLittleEndian = reverse.apply(txIn.previousTransactionId);
        assertEquals(txInPrevTxId, HEX.encode(previousTxIdLittleEndian));
    }

    private void checkTxInPreviousTransactionIndex(TxIn txIn) {
        assertEquals(BYTES, txIn.previousTransactionIndex.length);

        // Does parsed txInPrevTxIdx = 0?
        byte[] expectedPreviousTxIdx = intToBytes.apply(0);
        assertArrayEquals(expectedPreviousTxIdx, txIn.previousTransactionIndex);

        // Does parsed txInPrevTxIdx match the txInPrevTxIdx copy/pasted from the Jimmy Song book example?
        byte[] expectedPreviousTxIdxLittleEnding = HEX.decode(txInPrevTxIdx);
        assertArrayEquals(expectedPreviousTxIdxLittleEnding, reverse.apply(txIn.previousTransactionIndex));
    }

    private void checkTxInScriptSig(TxIn txIn) {
        // Test the handmade scriptsig.
        String txInScriptSigHex = txScriptSigLenHex + scriptSigCmd1LenHex + scriptSigHex
                + sighashAllHex
                + scriptSigCmd2LenHex + scriptSigCmd2Hex;
        Script scriptSig = Script.parse(hexToDataInputStream.apply(txInScriptSigHex));
        byte[] actualScriptSig = scriptSig.getScriptSig();
        String expectedScriptSigHex = "47304402207db2402a3311a3b845b038885e3dd889c08126a8570f26a844e3e4049c482a11022010178cdca4129eacbeab7c44648bf5ac1f9cac217cd609d216ec2ebc8d242c0a012103935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67";
        String actualScriptSigHex = HEX.encode(actualScriptSig);
        assertEquals(expectedScriptSigHex, actualScriptSigHex);

        // Test the parsed txin.scriptsig.asm.
        Script scriptSigFromTxIn = txIn.scriptSig;
        String expectedScriptSigAsm = "304402207db2402a3311a3b845b038885e3dd889c08126a8570f26a844e3e4049c482a11022010178cdca4129eacbeab7c44648bf5ac1f9cac217cd609d216ec2ebc8d242c0a[ALL] 03935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67";
        String actualScriptSigAsm = scriptSigFromTxIn.scriptSigAsm();
        assertEquals(expectedScriptSigAsm, actualScriptSigAsm);
    }

    private void checkTxInSerialization(TxIn txIn) {
        // Does serialized hex from parsed txIns match the txIns.hex copy/pasted from the Jimmy Song book example?
        String expectedSerializedTxInsHex = numTxInputsHex + txInPrevTxId + txInPrevTxIdx
                + txScriptSigLenHex + scriptSigCmd1LenHex + scriptSigHex
                + sighashAllHex + scriptSigCmd2LenHex + scriptSigCmd2Hex
                + txInSequenceHex;
        TxIn[] parsedTxIns = new TxIn[]{txIn};
        byte[] serializedTxIns = TransactionSerializer.serializeTransactionInputs(parsedTxIns);
        String actualSerializedTxInsHex = HEX.encode(serializedTxIns);
        assertEquals(expectedSerializedTxInsHex, actualSerializedTxInsHex);

        // Does serialized hex from parsed txInPrevTx match the txIn.hex copy/pasted from the Jimmy Song book example?
        String expectedSerializedTxInHex = txInPrevTxId + txInPrevTxIdx
                + txScriptSigLenHex + scriptSigCmd1LenHex + scriptSigHex
                + sighashAllHex + scriptSigCmd2LenHex + scriptSigCmd2Hex
                + txInSequenceHex;
        byte[] serializedTxIn = txIn.serialize();
        String actualSerializedTxInHex = HEX.encode(serializedTxIn);
        assertEquals(expectedSerializedTxInHex, actualSerializedTxInHex);
    }

    @Test
    public void testTxOutsParseAndSerialize() {
        // "vout": [
        //    {
        //      "value": 0.32454049,
        //      "n": 0,
        //      "scriptPubKey": {
        //        "asm": "OP_DUP OP_HASH160 bc3b654dca7e56b04dca18f2566cdaf02e8d9ada OP_EQUALVERIFY OP_CHECKSIG",
        //        "hex": "76a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac",
        //        "reqSigs": 1,
        //        "type": "pubkeyhash",
        //        "addresses": [
        //          "mxgEV1F3pxP4rJWcY19NuQpHJYukanKMBM"
        //        ]
        //      }
        //    },
        //    {
        //      "value": 0.10011545,
        //      "n": 1,
        //      "scriptPubKey": {
        //        "asm": "OP_DUP OP_HASH160 1c4bc762dd5423e332166702cb75f40df79fea12 OP_EQUALVERIFY OP_CHECKSIG",
        //        "hex": "76a9141c4bc762dd5423e332166702cb75f40df79fea1288ac",
        //        "reqSigs": 1,
        //        "type": "pubkeyhash",
        //        "addresses": [
        //          "mi6Zzdd6Wegi1PgcDkyakq1L7CvhBECeLa"
        //        ]
        //      }
        //    }
        //  ]
        Tx tx = Tx.parse(stream.apply(rawBytes), TESTNET3);
        assertEquals("3fad891a9d0deda19862a073d35611d57089377adb4585e6c457bcb0f4470821", tx.id());

        // Check txOut1's script and script address parsing.
        TxOut txOut1 = tx.getDeserializedOutputs()[0];
        TxOut txOut2 = tx.getDeserializedOutputs()[1];
        checkTxOut1PubScript(txOut1);
        checkTxOut2PubScript(txOut2);

        checkParsedTxOutsSerialization(new TxOut[]{txOut1, txOut2});
    }

    private void checkTxOut1PubScript(TxOut txOut1) {
        Script pubScript1 = txOut1.getScriptPubKey();
        assertTrue(isP2pkhScriptPubKey.test(pubScript1));
        assertEquals("OP_DUP OP_HASH160 bc3b654dca7e56b04dca18f2566cdaf02e8d9ada OP_EQUALVERIFY OP_CHECKSIG", pubScript1.asm());
        Address pubScript1Address = pubScript1.address(TESTNET3);
        assertEquals(P2PKH, pubScript1Address.addressType());
        assertEquals(TESTNET3, pubScript1Address.networkType());
        assertEquals("mxgEV1F3pxP4rJWcY19NuQpHJYukanKMBM", pubScript1Address.value());
    }

    private void checkTxOut2PubScript(TxOut txOut2) {
        Script pubScript2 = txOut2.getScriptPubKey();
        assertTrue(isP2pkhScriptPubKey.test(pubScript2));
        assertEquals("OP_DUP OP_HASH160 1c4bc762dd5423e332166702cb75f40df79fea12 OP_EQUALVERIFY OP_CHECKSIG", pubScript2.asm());
        Address pubScript2Address = pubScript2.address(TESTNET3);
        assertEquals(P2PKH, pubScript2Address.addressType());
        assertEquals(TESTNET3, pubScript2Address.networkType());
        assertEquals("mi6Zzdd6Wegi1PgcDkyakq1L7CvhBECeLa", pubScript2Address.value());
    }

    private void checkParsedTxOutsSerialization(TxOut[] parsedTxOuts) {
        // Does serialized hex from parsed txOuts match the txOuts.hex copy/pasted from the Jimmy Song book example?
        String expectedSerializedTxOutsHex = numTxOutputsHex + txOutsHex;
        byte[] serializedTxOuts = TransactionSerializer.serializeTransactionOutputs(parsedTxOuts);
        String actualSerializedTxOutsHex = HEX.encode(serializedTxOuts);
        assertEquals(expectedSerializedTxOutsHex, actualSerializedTxOutsHex);
    }

}
