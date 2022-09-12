package mandioca.bitcoin.transaction;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.script.Script;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.TestCase.assertFalse;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.script.Script.StandardScripts.isP2pkhScriptPubKey;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.*;

// Depends on running bitcoind -deprecatedrpc=generate -testnet -daemon on localhost

public class ParseAndSerializeTransactionTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(ParseAndSerializeTransactionTest.class);

    @Test
    public void testParseVersion() {
        // $BITCOIN_HOME/bitcoin-cli decoderawtransaction "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278afeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600"
        String rawTx = "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278afeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600";
        Tx tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        assertEquals(1, tx.getDeserializedVersion());
    }

    @Test
    public void testParseInputs() {
        // $BITCOIN_HOME/bitcoin-cli decoderawtransaction "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278afeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600"
        String rawTx = "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278afeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600";
        Tx tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        assertEquals(1, tx.getDeserializedInputs().length);

        String expectedPreviousTxIdAsHex = "d1c789a9c60383bf715f3f6ad9d14b91fe55f3deb369fe5d9280cb1a01793f81";
        byte[] expectedPreviousTxId = HEX.decode(expectedPreviousTxIdAsHex);
        byte[] actualPreviousTxId = tx.getDeserializedInputs()[0].previousTransactionId;
        assertArrayEquals(expectedPreviousTxId, actualPreviousTxId);
        String actualPreviousTxIdAsHex = HEX.encode(actualPreviousTxId);
        assertEquals(expectedPreviousTxIdAsHex, actualPreviousTxIdAsHex);

        // Confirm jsong's cached tx.hex = my tx.hex
        String localCachedTxHex = localTxCache.get(expectedPreviousTxIdAsHex);
        Tx myParsedTx = Tx.parse(hexToByteArrayInputStream.apply(localCachedTxHex), TESTNET3);
        String myParsedTxHex = HEX.encode(myParsedTx.serialize());
        assertEquals(localCachedTxHex, myParsedTxHex);

        byte[] expectedPreviousTxIdx = emptyArray.apply(4); // 0x00000000
        byte[] actualPreviousTxIdx = tx.getDeserializedInputs()[0].previousTransactionIndex;
        assertArrayEquals(expectedPreviousTxIdx, actualPreviousTxIdx);

        String expectedScriptSigAsHex = "6b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278a";
        String actualScriptSigAsHex = HEX.encode(tx.getDeserializedInputs()[0].scriptSig.serialize());
        assertEquals(expectedScriptSigAsHex, actualScriptSigAsHex);

        assertArrayEquals(TxIn.SEQUENCE_0xFEFFFFFF, reverse.apply(tx.getDeserializedInputs()[0].sequence));
    }

    @Test
    public void testParseOutputs() {
        // $BITCOIN_HOME/bitcoin-cli decoderawtransaction "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278afeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600"
        String rawTx = "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278afeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600";
        Tx tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        assertEquals(2, tx.getDeserializedOutputs().length);

        // check TxOut1
        TxOut txOut1 = tx.getDeserializedOutputs()[0];
        long expectedAmount = 32454049L;
        long actualAmount = txOut1.getAmountAsLong();
        assertEquals(expectedAmount, actualAmount);
        Script txOut1ScriptPubKey = txOut1.getScriptPubKey();
        assertEquals("OP_DUP OP_HASH160 bc3b654dca7e56b04dca18f2566cdaf02e8d9ada OP_EQUALVERIFY OP_CHECKSIG", txOut1ScriptPubKey.asm());


        // check TxOut2
        TxOut txOut2 = tx.getDeserializedOutputs()[1];
        expectedAmount = 10011545L;
        actualAmount = txOut2.getAmountAsLong();
        assertEquals(expectedAmount, actualAmount);
        Script txOut2ScriptPubKey = txOut2.getScriptPubKey();
        assertEquals("OP_DUP OP_HASH160 1c4bc762dd5423e332166702cb75f40df79fea12 OP_EQUALVERIFY OP_CHECKSIG", txOut2ScriptPubKey.asm());
    }

    @Test
    public void testParseLocktime() {
        // $BITCOIN_HOME/bitcoin-cli decoderawtransaction "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278afeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600"
        String rawTx = "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278afeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600";
        Tx tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        assertEquals(410393, tx.getDeserializedLocktime());
        assertTrue(tx.isLocktimeABlockNumber.get());
        assertFalse(tx.isLocktimeAUnixTimestamp.get());
        assertFalse(tx.isLocktimeIgnored.get());
    }

    @Test
    public void testFee() {
        // $BITCOIN_HOME/bitcoin-cli decoderawtransaction "0100000001eafb99c6b24e9285cf0ed457a028d723e61c80c443e370c84cf96507958dd610010000008b483045022100b95d83b8b81a0491cc24b7f91dc7b8f961a050b0ee5371eaa55e1d94bb89a7b902204524c390855cc384afc6d72e0b4f2ea72c9bad6a3a54f2ad5501b998b442fd750141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff02287d0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388acb34caf0b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000"
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "687f70bd4b071ca2733667f68c1b74c5561b6cf681406ed60a8d4c8effa0a9a7" true
        String rawTx = "0100000001eafb99c6b24e9285cf0ed457a028d723e61c80c443e370c84cf96507958dd610010000008b483045022100b95d83b8b81a0491cc24b7f91dc7b8f961a050b0ee5371eaa55e1d94bb89a7b902204524c390855cc384afc6d72e0b4f2ea72c9bad6a3a54f2ad5501b998b442fd750141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff02287d0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388acb34caf0b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000";
        Tx tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        assertEquals("687f70bd4b071ca2733667f68c1b74c5561b6cf681406ed60a8d4c8effa0a9a7", tx.id());
        assertEquals(20000L, tx.fee());
        // Fee confirmed at https://testnet.smartbit.com.au/tx/687f70bd4b071ca2733667f68c1b74c5561b6cf681406ed60a8d4c8effa0a9a7

        //
        // $BITCOIN_HOME/bitcoin-cli decoderawtransaction "01000000018fe1ada6f06c2fd2080f0ae73fbbbd14061ca572eeaa6342a66191d6777c7c70010000008b483045022100c4558ec0232c4aaafad956b777d73637adb6b5eaf9ba1c8de86607043bc96bd6022075715ea626bd2ea281e531ac34816132b80995a7541ad2a33e233eea78572a4c0141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff02d8e3b40b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388acce7b0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000"
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86" true
        // TxId:  3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86
        // Version:  1
        rawTx = "01000000018fe1ada6f06c2fd2080f0ae73fbbbd14061ca572eeaa6342a66191d6777c7c70010000008b483045022100c4558ec0232c4aaafad956b777d73637adb6b5eaf9ba1c8de86607043bc96bd6022075715ea626bd2ea281e531ac34816132b80995a7541ad2a33e233eea78572a4c0141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff02d8e3b40b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388acce7b0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000";
        tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        assertEquals("3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86", tx.id());
        assertEquals(20000L, tx.fee());
        // Fee confirmed at https://testnet.smartbit.com.au/tx/3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86

        //
        // $BITCOIN_HOME/bitcoin-cli decoderawtransaction "01000000014167b36d509c370d0efc9d97c6d0dffa5b83e31c2a58abc5f32a81a2cd2ffd4d000000008a47304402204669a0ca5922c22a13520a63722d434c17720f8e9fa9d1aa1202e7c81b248cd802202aa8e34018f9aa29655d4165ca4ab0fb49cd90b6911b261c9b40f18af62eb9730141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff02267f0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388acc6adb70b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000"
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "707c7c77d69161a64263aaee72a51c0614bdbb3fe70a0f08d22f6cf0a6ade18f" true
        rawTx = "01000000014167b36d509c370d0efc9d97c6d0dffa5b83e31c2a58abc5f32a81a2cd2ffd4d000000008a47304402204669a0ca5922c22a13520a63722d434c17720f8e9fa9d1aa1202e7c81b248cd802202aa8e34018f9aa29655d4165ca4ab0fb49cd90b6911b261c9b40f18af62eb9730141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff02267f0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388acc6adb70b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000";
        tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        assertEquals("707c7c77d69161a64263aaee72a51c0614bdbb3fe70a0f08d22f6cf0a6ade18f", tx.id());
        assertEquals(20000L, tx.fee());
        // Fee confirmed at https://testnet.smartbit.com.au/tx/707c7c77d69161a64263aaee72a51c0614bdbb3fe70a0f08d22f6cf0a6ade18f
    }

    @Test  // Jimmy Song's "Programming Bitcoin", Chapter 5, Exercise 5
    public void testJimmySongChapter5Exercise5() {
        // $BITCOIN_HOME/bitcoin-cli decoderawtransaction "010000000456919960ac691763688d3d3bcea9ad6ecaf875df5339e148a1fc61c6ed7a069e010000006a47304402204585bcdef85e6b1c6af5c2669d4830ff86e42dd205c0e089bc2a821657e951c002201024a10366077f87d6bce1f7100ad8cfa8a064b39d4e8fe4ea13a7b71aa8180f012102f0da57e85eec2934a82a585ea337ce2f4998b50ae699dd79f5880e253dafafb7feffffffeb8f51f4038dc17e6313cf831d4f02281c2a468bde0fafd37f1bf882729e7fd3000000006a47304402207899531a52d59a6de200179928ca900254a36b8dff8bb75f5f5d71b1cdc26125022008b422690b8461cb52c3cc30330b23d574351872b7c361e9aae3649071c1a7160121035d5c93d9ac96881f19ba1f686f15f009ded7c62efe85a872e6a19b43c15a2937feffffff567bf40595119d1bb8a3037c356efd56170b64cbcc160fb028fa10704b45d775000000006a47304402204c7c7818424c7f7911da6cddc59655a70af1cb5eaf17c69dadbfc74ffa0b662f02207599e08bc8023693ad4e9527dc42c34210f7a7d1d1ddfc8492b654a11e7620a0012102158b46fbdff65d0172b7989aec8850aa0dae49abfb84c81ae6e5b251a58ace5cfeffffffd63a5e6c16e620f86f375925b21cabaf736c779f88fd04dcad51d26690f7f345010000006a47304402200633ea0d3314bea0d95b3cd8dadb2ef79ea8331ffe1e61f762c0f6daea0fabde022029f23b3e9c30f080446150b23852028751635dcee2be669c2a1686a4b5edf304012103ffd6f4a67e94aba353a00882e563ff2722eb4cff0ad6006e86ee20dfe7520d55feffffff0251430f00000000001976a914ab0c0b2e98b1ab6dbf67d4750b0a56244948a87988ac005a6202000000001976a9143c82d7df364eb6c75be8c80df2b3eda8db57397088ac46430600"
        // TxId:  ee51510d7bbabe28052038d1deb10c03ec74f06a79e21913c6fcf48d56217c87
        // Version:  1
        String rawTx = "010000000456919960ac691763688d3d3bcea9ad6ecaf875df5339e148a1fc61c6ed7a069e010000006a47304402204585bcdef85e6b1c6af5c2669d4830ff86e42dd205c0e089bc2a821657e951c002201024a10366077f87d6bce1f7100ad8cfa8a064b39d4e8fe4ea13a7b71aa8180f012102f0da57e85eec2934a82a585ea337ce2f4998b50ae699dd79f5880e253dafafb7feffffffeb8f51f4038dc17e6313cf831d4f02281c2a468bde0fafd37f1bf882729e7fd3000000006a47304402207899531a52d59a6de200179928ca900254a36b8dff8bb75f5f5d71b1cdc26125022008b422690b8461cb52c3cc30330b23d574351872b7c361e9aae3649071c1a7160121035d5c93d9ac96881f19ba1f686f15f009ded7c62efe85a872e6a19b43c15a2937feffffff567bf40595119d1bb8a3037c356efd56170b64cbcc160fb028fa10704b45d775000000006a47304402204c7c7818424c7f7911da6cddc59655a70af1cb5eaf17c69dadbfc74ffa0b662f02207599e08bc8023693ad4e9527dc42c34210f7a7d1d1ddfc8492b654a11e7620a0012102158b46fbdff65d0172b7989aec8850aa0dae49abfb84c81ae6e5b251a58ace5cfeffffffd63a5e6c16e620f86f375925b21cabaf736c779f88fd04dcad51d26690f7f345010000006a47304402200633ea0d3314bea0d95b3cd8dadb2ef79ea8331ffe1e61f762c0f6daea0fabde022029f23b3e9c30f080446150b23852028751635dcee2be669c2a1686a4b5edf304012103ffd6f4a67e94aba353a00882e563ff2722eb4cff0ad6006e86ee20dfe7520d55feffffff0251430f00000000001976a914ab0c0b2e98b1ab6dbf67d4750b0a56244948a87988ac005a6202000000001976a9143c82d7df364eb6c75be8c80df2b3eda8db57397088ac46430600";
        Tx tx = Tx.parse(hexToByteArrayInputStream.apply(rawTx), TESTNET3);
        byte[] serializedTx = tx.serialize();
        String serializedTxHex = HEX.encode(serializedTx);
        assertEquals(rawTx, serializedTxHex);
        String expectedTxId = "ee51510d7bbabe28052038d1deb10c03ec74f06a79e21913c6fcf48d56217c87";
        String parsedTxId = tx.id();
        assertEquals(expectedTxId, parsedTxId);

        // What is the script sig for the 2nd output of this tx?
        TxIn txIn2 = tx.getDeserializedInputs()[1];
        Script txIn2ScriptSig = txIn2.scriptSig;
        String expectedTxIn2ScriptSigAsm = "304402207899531a52d59a6de200179928ca900254a36b8dff8bb75f5f5d71b1cdc26125022008b422690b8461cb52c3cc30330b23d574351872b7c361e9aae3649071c1a716[ALL] 035d5c93d9ac96881f19ba1f686f15f009ded7c62efe85a872e6a19b43c15a2937";
        String actualTxIn2ScriptSigAsm = txIn2ScriptSig.scriptSigAsm();
        assertEquals(expectedTxIn2ScriptSigAsm, actualTxIn2ScriptSigAsm);

        // What is the Script Pub Key for the 1st output of this tx?
        TxOut txOut1 = tx.getDeserializedOutputs()[0];
        Script txOut1ScriptPubKey = txOut1.getScriptPubKey();
        assertTrue(isP2pkhScriptPubKey.test(txOut1ScriptPubKey));
        String expectedAsm = "OP_DUP OP_HASH160 ab0c0b2e98b1ab6dbf67d4750b0a56244948a879 OP_EQUALVERIFY OP_CHECKSIG";
        assertEquals(expectedAsm, txOut1ScriptPubKey.asm());

        // What is the amount of the 2nd output for this tx?
        TxOut txOut2 = tx.getDeserializedOutputs()[1];
        long expectedAmount = 40000000L;
        byte[] expectedAmountBytes = longToBytes.apply(expectedAmount); // 40000000
        assertArrayEquals(expectedAmountBytes, txOut2.getAmount());
        assertEquals(expectedAmount, txOut2.getAmountAsLong());
    }
}
