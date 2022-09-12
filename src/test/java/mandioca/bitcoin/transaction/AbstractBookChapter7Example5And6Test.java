package mandioca.bitcoin.transaction;

import mandioca.bitcoin.MandiocaTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static mandioca.bitcoin.util.HexUtils.HEX;

/**
 * Abstract test class defines raw decoded tx id="3fad891a9d0deda19862a073d35611d57089377adb4585e6c457bcb0f4470821"
 * from it's atomic parts.
 */
abstract class AbstractBookChapter7Example5And6Test extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractBookChapter7Example5And6Test.class);
    /*
     * https://github.com/jimmysong/programmingbitcoin/issues/63
     * <p>
     * "By far, this chapter wiped me out. Here are my thoughts/experiences/troubles."
     * ...
     * ...
     */
    // Test parse & serializeInternal from raw hex at end of Chapter 7, Example 6 @ http://localhost:8888/edit/code-ch07/examples.py

    // Copy/pasted from http://localhost:8888/edit/code-ch07/examples.py  LINE 100   >>> print(transaction.serializeInternal().hex())
    protected static final String copyPastedRawTxHex = "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006a47304402207db2402a3311a3b845b038885e3dd889c08126a8570f26a844e3e4049c482a11022010178cdca4129eacbeab7c44648bf5ac1f9cac217cd609d216ec2ebc8d242c0a012103935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67feffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600";

    // Breaking down the full raw hex into parts helps me understand how it is parsed & serialized,
    // and make asserts more understandable.

    protected static final String version1Hex = "01000000";      // little endian
    protected static final String numTxInputsHex = "01";         // little endian
    protected static final String txInPrevTxId = "813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1"; // little e
    protected static final String txInPrevTxIdx = "00000000";    // little endian

    protected static final String txScriptSigLenHex = "6a";      // 0x6a script length = 106  (varint)
    protected static final String scriptSigCmd1LenHex = "47";    // 0x47 = cmd.len = 71  (varint)
    protected static final String scriptSigHex = "304402207db2402a3311a3b845b038885e3dd889c08126a8570f26a844e3e4049c482a11022010178cdca4129eacbeab7c44648bf5ac1f9cac217cd609d216ec2ebc8d242c0a";
    protected static final String sighashAllHex = "01";          // 0x01 = [ALL]  SIGHASH_ALL
    protected static final String scriptSigCmd2LenHex = "21";    // 0x21 = cmd.len = 33  (varint)
    protected static final String scriptSigCmd2Hex = "03935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67";
    protected static final String txInSequenceHex = "feffffff";  // 4294967294

    protected static final String numTxOutputsHex = "02";
    protected static final String txOutsHex = "a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac";
    protected static final String locktimeHex = "19430600";      // little endian

    protected static final String rawParts = version1Hex
            + numTxInputsHex + txInPrevTxId + txInPrevTxIdx
            + txScriptSigLenHex + scriptSigCmd1LenHex + scriptSigHex
            + sighashAllHex + scriptSigCmd2LenHex + scriptSigCmd2Hex
            + txInSequenceHex
            + numTxOutputsHex + txOutsHex + locktimeHex;

    protected static final byte[] rawBytes = HEX.decode(rawParts);

    // Verbose results from bitcoin-cli decoderawtransaction command, to verify correctness of tx parsing & serializing.
    //
    // $BITCOIN_HOME/bitcoin-cli decoderawtransaction "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006a47304402207db2402a3311a3b845b038885e3dd889c08126a8570f26a844e3e4049c482a11022010178cdca4129eacbeab7c44648bf5ac1f9cac217cd609d216ec2ebc8d242c0a012103935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67feffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600" true
    // {
    //  "txid": "3fad891a9d0deda19862a073d35611d57089377adb4585e6c457bcb0f4470821",
    //  "hash": "3fad891a9d0deda19862a073d35611d57089377adb4585e6c457bcb0f4470821",
    //  "version": 1,
    //  "size": 225,
    //  "vsize": 225,
    //  "weight": 900,
    //  "locktime": 410393,
    //  "vin": [
    //    {
    //      "txid": "d1c789a9c60383bf715f3f6ad9d14b91fe55f3deb369fe5d9280cb1a01793f81",
    //      "vout": 0,
    //      "scriptSig": {
    //        "asm": "304402207db2402a3311a3b845b038885e3dd889c08126a8570f26a844e3e4049c482a11022010178cdca4129eacbeab7c44648bf5ac1f9cac217cd609d216ec2ebc8d242c0a[ALL] 03935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67",
    //        "hex": "47304402207db2402a3311a3b845b038885e3dd889c08126a8570f26a844e3e4049c482a11022010178cdca4129eacbeab7c44648bf5ac1f9cac217cd609d216ec2ebc8d242c0a012103935581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67"
    //      },
    //      "sequence": 4294967294
    //    }
    //  ],
    //  "vout": [
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
    // }
}
