package mandioca.bitcoin.transaction;

import mandioca.bitcoin.script.Script;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.CurrencyFunctions.btcToSatoshis;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.script.Script.StandardScripts.addressToP2pkhScript;
import static mandioca.bitcoin.transaction.TransactionSerializer.serializeLocktime;
import static mandioca.bitcoin.transaction.TransactionSerializer.serializeVersion;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * https://github.com/jimmysong/programmingbitcoin/issues/63
 * <p>
 * "By far, this chapter wiped me out. Here are my thoughts/experiences/troubles."
 * ...
 * ...
 */
public class BookChapter7Example5CreateTxTest extends AbstractBookChapter7Example5And6Test {

    private static final Logger log = LoggerFactory.getLogger(BookChapter7Example5CreateTxTest.class);

    // $BITCOIN_HOME/bitcoin-cli decoderawtransaction "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006a47304402207db2402a3311a3b845b038885e3dd889c08126a8570f26a844e3e4049c482a11022010178cdca4129eacbeab7c44648bf5ac1f9cac217cd609d216ec2ebc8d242c0a012103935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67feffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600" true

    @BeforeClass
    public static void testSetupCorrect() {
        assertEquals(copyPastedRawTxHex, rawParts); // make sure handmade tx parts = the copy/pasted decoded tx hex
    }

    @Test
    public void testExample5() {
        // prev_tx = bytes.fromhex('0d6fe5213c0b3291f208cba8bfb59b7476dffacc4e5cb66f6eb20a080843a299')
        //>>> prev_index = 13
        //>>> tx_in = TxIn(prev_tx, prev_index)
        //>>> tx_outs = []
        //>>> change_amount = int(0.33*100000000)  # <1>
        //>>> change_h160 = decode_base58('mzx5YhAH9kNHtcN481u6WkjeHjYtVeKVh2')
        //>>> change_script = p2pkh_script(change_h160)
        //>>> change_output = TxOut(amount=change_amount, script_pubkey=change_script)
        //>>> target_amount = int(0.1*100000000)  # <1>
        //>>> target_h160 = decode_base58('mnrVtF8DWjMu839VW3rBfgYaAfKk8983Xf')
        //>>> target_script = p2pkh_script(target_h160)
        //>>> target_output = TxOut(amount=target_amount, script_pubkey=target_script)
        //>>> tx_obj = Tx(1, [tx_in], [change_output, target_output], 0, True)  # <2>
        //>>> print(tx_obj)
        //tx: cd30a8da777d28ef0e61efe68a9f7c559c1d3e5bcd7b265c850ccb4068598d11
        //version: 1
        //tx_ins:
        //0d6fe5213c0b3291f208cba8bfb59b7476dffacc4e5cb66f6eb20a080843a299:13
        //tx_outs:
        //33000000:OP_DUP OP_HASH160 d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f OP_EQUALVERIFY OP_CHECKSIG
        //10000000:OP_DUP OP_HASH160 507b27411ccf7f16f10297de6cef3f291623eddf OP_EQUALVERIFY OP_CHECKSIG
        //locktime: 0

        final String prevTxIdHex = "0d6fe5213c0b3291f208cba8bfb59b7476dffacc4e5cb66f6eb20a080843a299";
        byte[] prevTxId = HEX.decode(prevTxIdHex);
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "0d6fe5213c0b3291f208cba8bfb59b7476dffacc4e5cb66f6eb20a080843a299" true
        // Hex:  0100000001c847414138fc4e86c97bce0adfe0180d8716d0db7f43b955ebb7a80f3cbc2500000000006a47304402202f7e26dda5a70179eaa51e7a995b2fb6b3a705c59c792ae1fde3a4f4a58adaf60220406672081f8f2acfdfbeb327a5c618beb66ab226111da48ac9b150dad0d0ae52012103935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67ffffffff0e404b4c00000000001976a91477d946a68a9b95e851afa57006cf2d0c15ae8b3d88ac404b4c00000000001976a914325371fe093e259bdc7beca2c31f795e1b492b2088ac404b4c00000000001976a9144ccf8be232f0b1ee450a5edcc83cc4966703531388ac404b4c00000000001976a9146fe7d8cea1a39739508db7070b029d8497a0d85288ac404b4c00000000001976a91427813ea0d6e3439ffa3e30e47cd768c45bd27ab888ac404b4c00000000001976a914c16ac1981a4c73f1d51cc28f53d4757d3673a45c88ac404b4c00000000001976a9143a1806b04b0f3e14ab9b7c8cb045175d14014ac188ac404b4c00000000001976a914af39e20d8f115ecdbb3b96cda2710239e9259c5288ac404b4c00000000001976a914047357aff1cb49f6a26d71e48b88c1ba7c6ce92788ac404b4c00000000001976a9149637bebfa095f176b6cbffc79cec55fb55bf14de88ac404b4c00000000001976a9142dffa6b5f8ba2bf1ab487d1be1af9d9695350a4b88ac404b4c00000000001976a914fcf0cb53dccea9e4125a8472b8606e7f1769dad388ac404b4c00000000001976a9145a8398af0353464cf727d57a1dd79807eee50b1288ac00639f02000000001976a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac00000000
        int prevTxIndex = 13;
        TxIn txIn = new TxIn(prevTxId, intToBytes.apply(prevTxIndex));
        assertEquals("0d6fe5213c0b3291f208cba8bfb59b7476dffacc4e5cb66f6eb20a080843a299", HEX.encode(txIn.previousTransactionId));
        assertArrayEquals(TxIn.SEQUENCE_0xFFFFFFFF, txIn.sequence); // -1 little endian

        // Confirm jsong's cached tx.hex = my tx.hex
        String localCachedTxHex = localTxCache.get(prevTxIdHex);
        Tx tx = Tx.parse(hexToByteArrayInputStream.apply(localCachedTxHex), TESTNET3);
        String parsedTxHex = HEX.encode(tx.serialize());
        assertEquals(localCachedTxHex, parsedTxHex);

        long changeAmount = btcToSatoshis.apply("0.33");
        String changeAddress = "mzx5YhAH9kNHtcN481u6WkjeHjYtVeKVh2";
        Script changeScript = addressToP2pkhScript.apply(changeAddress);
        assertEquals("OP_DUP OP_HASH160 d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f OP_EQUALVERIFY OP_CHECKSIG", changeScript.asm());
        TxOut changeOutput = new TxOut(longToBytes.apply(changeAmount), changeScript);
        assertEquals(33000000L, changeOutput.getAmountAsLong());
        String expectedChangeScriptPubKeyAsm = "OP_DUP OP_HASH160 d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f OP_EQUALVERIFY OP_CHECKSIG";
        assertEquals(expectedChangeScriptPubKeyAsm, changeOutput.getScriptPubKey().asm());

        long targetAmount = btcToSatoshis.apply("0.1");
        String targetAddress = "mnrVtF8DWjMu839VW3rBfgYaAfKk8983Xf";
        Script targetScript = addressToP2pkhScript.apply(targetAddress);
        assertEquals("OP_DUP OP_HASH160 507b27411ccf7f16f10297de6cef3f291623eddf OP_EQUALVERIFY OP_CHECKSIG", targetScript.asm());
        TxOut targetOutput = new TxOut(longToBytes.apply(targetAmount), targetScript);
        assertEquals(10000000L, targetOutput.getAmountAsLong());
        String expectedTargetScriptPubKeyAsm = "OP_DUP OP_HASH160 507b27411ccf7f16f10297de6cef3f291623eddf OP_EQUALVERIFY OP_CHECKSIG";
        assertEquals(expectedTargetScriptPubKeyAsm, targetOutput.getScriptPubKey().asm());

        // TODO Remember: '[change_output, target_output]' translates in java to
        //              TxOut[] txOuts = new TxOut[]{targetOutput, changeOutput};
        //      NOT     TxOut[] txOuts = new TxOut[]{changeOutput, targetOutput};
        //      This bug took me days to discover.
        //      But here, the book seems to be inconsistent...
        TxOut[] txOuts = new TxOut[]{changeOutput, targetOutput};

        Tx txObj = new Tx(1, new TxIn[]{txIn}, txOuts, 0, TESTNET3);
        String expectedTxObjId = "cd30a8da777d28ef0e61efe68a9f7c559c1d3e5bcd7b265c850ccb4068598d11";
        assertEquals(expectedTxObjId, txObj.id());

        assertArrayEquals(serializeVersion.apply(1), txObj.version);

        assertEquals(1, txObj.getDeserializedInputs().length);
        String actualTxInPrevTxId = HEX.encode(txObj.getDeserializedInputs()[0].previousTransactionId);
        assertEquals("0d6fe5213c0b3291f208cba8bfb59b7476dffacc4e5cb66f6eb20a080843a299", actualTxInPrevTxId);

        assertEquals(2, txObj.getDeserializedOutputs().length);
        String actualTxOut1PubScriptAsm = txObj.getDeserializedOutputs()[0].getScriptPubKey().asm();
        assertEquals(expectedChangeScriptPubKeyAsm, actualTxOut1PubScriptAsm);
        String actualTxOut2PubScriptAsm = txObj.getDeserializedOutputs()[1].getScriptPubKey().asm();
        assertEquals(expectedTargetScriptPubKeyAsm, actualTxOut2PubScriptAsm);

        assertArrayEquals(serializeLocktime.apply(0), txObj.locktime);
    }
}
