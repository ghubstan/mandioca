package mandioca.bitcoin.script;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.address.Address;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.TestCase.assertTrue;
import static mandioca.bitcoin.address.AddressType.P2PKH;
import static mandioca.bitcoin.address.AddressType.P2SH;
import static mandioca.bitcoin.network.NetworkType.MAINNET;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.script.Script.StandardScripts;
import static mandioca.bitcoin.util.Base58.decodeChecked;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

public class ScriptAddressEncodingTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(ScriptAddressEncodingTest.class);

    @Test
    public void testP2pkhMainnetAddressEncoding() {
        // From Jimmy Song Chapter 13 Script.py tests
        String address = "1BenRpVUFK65JFWcQSuHnJKzc4M8ZP8Eqa";
        byte[] h160 = decodeChecked(address, true); // Always decodeChecked(remove prefix)
        String h160Hex = HEX.encode(h160);
        assertEquals("74d691da1574e6b3c192ecfb52cc8984ee7b6c56", h160Hex);

        Script p2pkhScript = StandardScripts.hashToP2pkhScript.apply(h160);
        assertEquals("OP_DUP OP_HASH160 74d691da1574e6b3c192ecfb52cc8984ee7b6c56 OP_EQUALVERIFY OP_CHECKSIG", p2pkhScript.asm());

        Address actualAddress = p2pkhScript.address(MAINNET);
        assertEquals(address, actualAddress.value());
        assertEquals(P2PKH, actualAddress.addressType());
        assertEquals(MAINNET, actualAddress.networkType());
        assertTrue(actualAddress.validate()); // TODO
    }

    @Test
    public void testP2pkhTestnetAddressEncoding1() {
        // From Jimmy Song Chapter 13 Script.py tests
        String address = "mrAjisaT4LXL5MzE81sfcDYKU3wqWSvf9q";
        byte[] h160 = decodeChecked(address, true); // Always decodeChecked(remove prefix)
        String h160Hex = HEX.encode(h160);
        assertEquals("74d691da1574e6b3c192ecfb52cc8984ee7b6c56", h160Hex);

        Script p2pkhScript = StandardScripts.hashToP2pkhScript.apply(h160);
        assertEquals("OP_DUP OP_HASH160 74d691da1574e6b3c192ecfb52cc8984ee7b6c56 OP_EQUALVERIFY OP_CHECKSIG", p2pkhScript.asm());

        Address actualAddress = p2pkhScript.address(TESTNET3);
        assertEquals(address, actualAddress.value());
        assertEquals(P2PKH, actualAddress.addressType());
        assertEquals(TESTNET3, actualAddress.networkType());
        assertTrue(actualAddress.validate()); // TODO
    }

    @Test
    public void testP2pkhTestnetAddressEncoding2() {
        // Generated by bitcoin-cli -testnet getnewaddress -> mt5F8GGQkL2Z3BrprLDqeERquWLXTCBjo9
        // $BITCOIN_HOME/bitcoin-cli -testnet getaddressinfo "mt5F8GGQkL2Z3BrprLDqeERquWLXTCBjo9"
        String address = "mt5F8GGQkL2Z3BrprLDqeERquWLXTCBjo9";
        byte[] h160 = decodeChecked(address, true); // Always decodeChecked(remove prefix)
        String h160Hex = HEX.encode(h160);
        assertEquals("89bcdec33b7e84349f85691a6d4b0af352638398", h160Hex);

        Script p2pkhScript = StandardScripts.hashToP2pkhScript.apply(h160);
        assertEquals("OP_DUP OP_HASH160 89bcdec33b7e84349f85691a6d4b0af352638398 OP_EQUALVERIFY OP_CHECKSIG", p2pkhScript.asm());

        Address actualAddress = p2pkhScript.address(TESTNET3);
        assertEquals(address, actualAddress.value());
        assertEquals(P2PKH, actualAddress.addressType());
        assertEquals(TESTNET3, actualAddress.networkType());
        assertTrue(actualAddress.validate()); // TODO
    }

    @Test
    public void testP2pkhTestnetAddressEncoding3() {
        // Generated by bitcoin-cli -testnet getnewaddress -> mmshHCCEABt9m4X3boB8r2GNnLuDnFrVD5
        // $BITCOIN_HOME/bitcoin-cli -testnet getaddressinfo "mmshHCCEABt9m4X3boB8r2GNnLuDnFrVD5"
        String address = "mmshHCCEABt9m4X3boB8r2GNnLuDnFrVD5";
        byte[] h160 = decodeChecked(address, true); // Always decodeChecked(remove prefix)
        String h160Hex = HEX.encode(h160);
        assertEquals("45bcf4c6446824e86f2068fb7afcec4611df04e0", h160Hex);

        Script p2pkhScript = StandardScripts.hashToP2pkhScript.apply(h160);
        assertEquals("OP_DUP OP_HASH160 45bcf4c6446824e86f2068fb7afcec4611df04e0 OP_EQUALVERIFY OP_CHECKSIG", p2pkhScript.asm());

        Address actualAddress = p2pkhScript.address(TESTNET3);
        assertEquals(address, actualAddress.value());
        assertEquals(P2PKH, actualAddress.addressType());
        assertEquals(TESTNET3, actualAddress.networkType());
        assertTrue(actualAddress.validate()); // TODO
    }


    @Test
    public void testP2shMainnetAddressEncoding() {
        // From Jimmy Song Chapter 13 Script.py tests
        String address = "3CLoMMyuoDQTPRD3XYZtCvgvkadrAdvdXh";
        byte[] h160 = decodeChecked(address, true); // Always decodeChecked(remove prefix)
        String h160Hex = HEX.encode(h160);
        assertEquals("74d691da1574e6b3c192ecfb52cc8984ee7b6c56", h160Hex);

        Script p2shScript = StandardScripts.hashToP2shScript.apply(h160);
        assertEquals("OP_HASH160 74d691da1574e6b3c192ecfb52cc8984ee7b6c56 OP_EQUAL", p2shScript.asm());

        Address actualAddress = p2shScript.address(MAINNET);
        assertEquals(address, actualAddress.value());
        assertEquals(P2SH, actualAddress.addressType());
        assertEquals(MAINNET, actualAddress.networkType());
        assertTrue(actualAddress.validate()); // TODO
    }

    @Test
    public void testP2shTestnetAddressEncoding() {
        // From Jimmy Song Chapter 13 Script.py tests
        String address = "2N3u1R6uwQfuobCqbCgBkpsgBxvr1tZpe7B";
        byte[] h160 = decodeChecked(address, true); // Always decodeChecked(remove prefix)
        String h160Hex = HEX.encode(h160);
        assertEquals("74d691da1574e6b3c192ecfb52cc8984ee7b6c56", h160Hex);

        Script p2shScript = StandardScripts.hashToP2shScript.apply(h160);
        assertEquals("OP_HASH160 74d691da1574e6b3c192ecfb52cc8984ee7b6c56 OP_EQUAL", p2shScript.asm());

        Address actualAddress = p2shScript.address(TESTNET3);
        assertEquals(address, actualAddress.value());
        assertEquals(P2SH, actualAddress.addressType());
        assertEquals(TESTNET3, actualAddress.networkType());
        assertTrue(actualAddress.validate()); // TODO
    }


}
