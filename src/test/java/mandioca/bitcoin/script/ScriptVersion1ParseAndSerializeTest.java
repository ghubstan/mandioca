package mandioca.bitcoin.script;

import mandioca.bitcoin.MandiocaTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static mandioca.bitcoin.function.ByteArrayFunctions.hexToDataInputStream;
import static mandioca.bitcoin.script.Script.parse;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

// http://www.righto.com/2014/02/bitcoins-hard-way-using-raw-bitcoin.html

// Try tests from
// https://raw.githubusercontent.com/bitcoinj/bitcoinj/master/core/src/test/java/org/bitcoinj/script/ScriptTest.java

public class ScriptVersion1ParseAndSerializeTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(ScriptVersion1ParseAndSerializeTest.class);

    @Test
    public void testParseScriptHex1() {
        String scriptHex = "6b"  // 0x6b script length = 107
                + "48"           // 0x48 = cmd.len = 72
                + "3045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed"
                + "01"          // 0x01 = [ALL]  SIGHASH_ALL
                + "21"          // 0x21 = cmd.len = 33
                + "0349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278a";
        Script scriptSig = Script.parse(hexToDataInputStream.apply(scriptHex));
        byte[] actualScriptSig = scriptSig.getScriptSig();
        // Parser includes 2 varint(cmd-len) codes (l1 = 0x48,  l2 = 0x21) in the returned script sig
        String expectedScriptSigHex = "483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278a";
        String actualScriptSigHex = HEX.encode(actualScriptSig);
        // out.println("scriptSig.asm   " + scriptSig.asm());
        // out.println("expectedScriptSigHex  " + expectedScriptSigHex);
        // out.println("actualScriptSigHex    " + actualScriptSigHex);
        assertEquals(expectedScriptSigHex, actualScriptSigHex);

        byte[] serializedScript = scriptSig.serialize();
        String serializedHex = HEX.encode(serializedScript);
        assertEquals(scriptHex, serializedHex);
    }

    @Test
    public void testParseScriptHex2() {
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "1ce75a98e716588ed66d80d7efbf4ab5a493efce93fc25608232654cea0a39c8" true
        // "scriptSig": {
        //        "asm": "304402201529f6adabd623c1930c8d9601c4ce771f5a600fbb776358da38df379bf55e1202203546b0dcf75b68a33273fca4c43ca58b6e1758a581e89791abb83deab7ddaaa2[ALL] 03ae329dbe0a3c64fb92b1494f2c524567e399cdd111c497ae4e82deb16750d988",
        //        "hex": "47304402201529f6adabd623c1930c8d9601c4ce771f5a600fbb776358da38df379bf55e1202203546b0dcf75b68a33273fca4c43ca58b6e1758a581e89791abb83deab7ddaaa2012103ae329dbe0a3c64fb92b1494f2c524567e399cdd111c497ae4e82deb16750d988"
        //      },
        String scriptHex = "6a"  // 0x6a script length = 106
                + "47"           // 0x47 = cmd.len = 71
                + "304402201529f6adabd623c1930c8d9601c4ce771f5a600fbb776358da38df379bf55e1202203546b0dcf75b68a33273fca4c43ca58b6e1758a581e89791abb83deab7ddaaa2"
                + "01"          // 0x01 = [ALL]  SIGHASH_ALL
                + "21"          // 0x21 = cmd.len = 33
                + "03ae329dbe0a3c64fb92b1494f2c524567e399cdd111c497ae4e82deb16750d988";
        Script scriptSig = Script.parse(hexToDataInputStream.apply(scriptHex));
        byte[] actualScriptSig = scriptSig.getScriptSig();
        // Parser includes 2 varint(cmd-len) codes (0x48,  0x21) in the returned script sig
        String expectedScriptSigHex = "47304402201529f6adabd623c1930c8d9601c4ce771f5a600fbb776358da38df379bf55e1202203546b0dcf75b68a33273fca4c43ca58b6e1758a581e89791abb83deab7ddaaa2012103ae329dbe0a3c64fb92b1494f2c524567e399cdd111c497ae4e82deb16750d988";
        String actualScriptSigHex = HEX.encode(actualScriptSig);
        // out.println("scriptSig.asm   " + scriptSig.asm());
        // out.println("expectedScriptSigHex  " + expectedScriptSigHex);
        // out.println("actualScriptSigHex    " + actualScriptSigHex);
        assertEquals(expectedScriptSigHex, actualScriptSigHex);

        byte[] serializedScript = scriptSig.serialize();
        String serializedHex = HEX.encode(serializedScript);
        assertEquals(scriptHex, serializedHex);
    }

    @Test
    public void testParseScriptHex3() {
        // https://testnet.smartbit.com.au/block/500233
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "c2f8935eb62dd1599e63c608f88826ebe290af0ed725f7775cac12ce644fed69" true
        String scriptHex = "6a"  // 0x6a script length = 106
                + "47"           // 0x47 = cmd.len = 71
                + "3044022024184d9f62047e519c766540ad981aa08c63e8dc726e5473145d93fec4afe67302201de0f0fd3817a4236cca6e0766195541584228156d41fe901297d82585037e89"
                + "01"          // 0x01 = [ALL]  SIGHASH_ALL
                + "21"          // 0x21 = cmd.len = 33
                + "0375106f3269c68bd8634a3859ff78cb7b089a0a242b9a8d4400aa6e1a51a49284";
        Script scriptSig = Script.parse(hexToDataInputStream.apply(scriptHex));
        byte[] actualScriptSig = scriptSig.getScriptSig();
        // Parser includes 2 varint(cmd-len) codes (0x47,  0x21) in the returned script sig
        String expectedScriptSigHex = "473044022024184d9f62047e519c766540ad981aa08c63e8dc726e5473145d93fec4afe67302201de0f0fd3817a4236cca6e0766195541584228156d41fe901297d82585037e8901210375106f3269c68bd8634a3859ff78cb7b089a0a242b9a8d4400aa6e1a51a49284";
        String actualScriptSigHex = HEX.encode(actualScriptSig);
        // out.println("scriptSig.asm   " + scriptSig.asm());
        // out.println("expectedScriptSigHex  " + expectedScriptSigHex);
        // out.println("actualScriptSigHex    " + actualScriptSigHex);
        assertEquals(expectedScriptSigHex, actualScriptSigHex);

        byte[] serializedScript = scriptSig.serialize();
        String serializedHex = HEX.encode(serializedScript);
        assertEquals(scriptHex, serializedHex);
    }

    @Test
    public void testParseScriptHex4() {
        // https://testnet.smartbit.com.au/block/500235
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "52f41c9180bd563c0b0af4f432a1a792f9d0ad724cf8320a613bacc60f333ef8" true
        String scriptHex = "6a"  // 0x6a script length = 106
                + "47"           // 0x47 = cmd.len = 71
                + "30440220240230dfa4109e1548303e0c44ff7f175f2fe6b3edea95b911ed4252b57a0795022002df080ce4d4ff6a7fc4ebd9ad3ca5ee55133e4bb12724ca3e9ad188ac127bb5"
                + "01"          // 0x01 = [ALL]  SIGHASH_ALL
                + "21"          // 0x21 = cmd.len = 33
                + "032c4c05ae330a98d9705091dfb58349949396c704ef5076049178071ee1a29b4a";
        Script scriptSig = Script.parse(hexToDataInputStream.apply(scriptHex));
        byte[] actualScriptSig = scriptSig.getScriptSig();
        // Parser includes 2 varint(cmd-len) codes (0x47,  0x21) in the returned script sig
        String expectedScriptSigHex = "4730440220240230dfa4109e1548303e0c44ff7f175f2fe6b3edea95b911ed4252b57a0795022002df080ce4d4ff6a7fc4ebd9ad3ca5ee55133e4bb12724ca3e9ad188ac127bb50121032c4c05ae330a98d9705091dfb58349949396c704ef5076049178071ee1a29b4a";
        String actualScriptSigHex = HEX.encode(actualScriptSig);
        // out.println("scriptSig.asm   " + scriptSig.asm());
        // out.println("expectedScriptSigHex  " + expectedScriptSigHex);
        // out.println("actualScriptSigHex    " + actualScriptSigHex);
        assertEquals(expectedScriptSigHex, actualScriptSigHex);

        byte[] serializedScript = scriptSig.serialize();
        String serializedHex = HEX.encode(serializedScript);
        assertEquals(scriptHex, serializedHex);
    }

    @Test
    public void testParseScriptHex5() {
        // https://testnet.smartbit.com.au/block/500244
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "6991a4816ba3ea5326ce02aeac47cc5738e3b8c8246b8ee3b711268956daaf2d" true
        String scriptHex = "6a"  // 0x6a script length = 106
                + "47"           // 0x47 = cmd.len = 71
                + "30440220422a75fb9f1e756137ff86684ceb022340186f2c77622f3f32ac964e43bd3916022027bedaf4be58489f2f7fa0a88164731928f7cd79d528b6611e37ca3f166fce9d"
                + "01"          // 0x01 = [ALL]  SIGHASH_ALL
                + "21"          // 0x21 = cmd.len = 33
                + "0213484d11a257e879cbc2fd05e92ddc4adfb90fa56deced1fa24c3ffaac363813";
        Script scriptSig = Script.parse(hexToDataInputStream.apply(scriptHex));
        byte[] actualScriptSig = scriptSig.getScriptSig();
        // Parser includes 2 varint(cmd-len) codes (0x47,  0x21) in the returned script sig
        String expectedScriptSigHex = "4730440220422a75fb9f1e756137ff86684ceb022340186f2c77622f3f32ac964e43bd3916022027bedaf4be58489f2f7fa0a88164731928f7cd79d528b6611e37ca3f166fce9d01210213484d11a257e879cbc2fd05e92ddc4adfb90fa56deced1fa24c3ffaac363813";
        String actualScriptSigHex = HEX.encode(actualScriptSig);
        // out.println("scriptSig.asm   " + scriptSig.asm());
        // out.println("expectedScriptSigHex  " + expectedScriptSigHex);
        // out.println("actualScriptSigHex    " + actualScriptSigHex);
        assertEquals(expectedScriptSigHex, actualScriptSigHex);

        byte[] serializedScript = scriptSig.serialize();
        String serializedHex = HEX.encode(serializedScript);
        assertEquals(scriptHex, serializedHex);
    }

    @Test  // Test data from https://raw.githubusercontent.com/jimmysong/programmingbitcoin/master/code-ch05/script.py
    public void testParseScriptPubKey() {
        String scriptPubKeyHex = "6a47304402207899531a52d59a6de200179928ca900254a36b8dff8bb75f5f5d71b1cdc26125022008b422690b8461cb52c3cc30330b23d574351872b7c361e9aae3649071c1a7160121035d5c93d9ac96881f19ba1f686f15f009ded7c62efe85a872e6a19b43c15a2937";
        Script script = parse(hexToDataInputStream.apply(scriptPubKeyHex));
        // out.println("script.asm   " + script.asm());
        byte[] expectedScript = HEX.decode("304402207899531a52d59a6de200179928ca900254a36b8dff8bb75f5f5d71b1cdc26125022008b422690b8461cb52c3cc30330b23d574351872b7c361e9aae3649071c1a71601");
        assertArrayEquals(expectedScript, script.getCmds()[0]);
        expectedScript = HEX.decode("035d5c93d9ac96881f19ba1f686f15f009ded7c62efe85a872e6a19b43c15a2937");
        assertArrayEquals(expectedScript, script.getCmds()[1]);

        byte[] serializedScript = script.serialize();
        String serializedHex = HEX.encode(serializedScript);
        assertEquals(scriptPubKeyHex, serializedHex);
    }

}
