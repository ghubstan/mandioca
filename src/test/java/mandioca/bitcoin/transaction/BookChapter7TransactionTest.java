package mandioca.bitcoin.transaction;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.address.Address;
import mandioca.bitcoin.script.Script;
import mandioca.bitcoin.script.processing.OpCodeFunction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static mandioca.bitcoin.address.AddressFactory.legacyAddressToHash;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.script.Script.StandardScripts;
import static mandioca.bitcoin.script.Script.StandardScripts.isP2pkhScriptPubKey;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BookChapter7TransactionTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(BookChapter7TransactionTest.class);

    @BeforeClass
    public static void enableStackDebug() {
        OpCodeFunction.enableStackDebug();  // TODO add this to appropriate classes
    }

    @Test
    public void testCreateFirstBogusTransaction() {
    }

    @Test
    public void testP2pkhTestnetSenderAddressEncoding() {
        // $BITCOIN_HOME/bitcoin-cli -testnet getnewaddress "4th"
        //              n29E94aJPTkyeNqTDh9aQxK5qxpm8Umjbs
        // $BITCOIN_HOME/bitcoin-cli -testnet getaddressinfo "n29E94aJPTkyeNqTDh9aQxK5qxpm8Umjbs"
        // Balance as of 2019-12-23T16:07:56-03:00   0.0500955 tBTC  (Send exactly 0.05 tBTC  w/ fee 2.0 sats per byte)
        // $BITCOIN_HOME/bitcoin-cli -testnet getreceivedbyaddress "n29E94aJPTkyeNqTDh9aQxK5qxpm8Umjbs"
        String address = "n29E94aJPTkyeNqTDh9aQxK5qxpm8Umjbs";
        byte[] h160 = legacyAddressToHash.apply(address);
        String h160Hex = HEX.encode(h160);
        assertEquals("e23eb78e6ef8e10fd70f121a6e26edaa15728182", h160Hex);

        Script p2pkhScript = StandardScripts.hashToP2pkhScript.apply(h160);
        assertEquals("OP_DUP OP_HASH160 e23eb78e6ef8e10fd70f121a6e26edaa15728182 OP_EQUALVERIFY OP_CHECKSIG", p2pkhScript.asm());
        assertTrue(isP2pkhScriptPubKey.test(p2pkhScript));

        Address addressFromScript = p2pkhScript.address(TESTNET3);
        assertEquals(address, addressFromScript.value());
    }

    @Test
    public void testP2pkhTestnetRecipientAddressEncoding() {
        // $BITCOIN_HOME/bitcoin-cli -testnet getnewaddress "1st created tx recipient address"
        //              mvF8HKtE6gJ63gCVXED7Z1o8bCYP491bgo
        // $BITCOIN_HOME/bitcoin-cli -testnet getaddressinfo "mvF8HKtE6gJ63gCVXED7Z1o8bCYP491bgo"
        // Balance as of 2019-12-23T16:07:56-03:00   0.00 tBTC
        // $BITCOIN_HOME/bitcoin-cli -testnet getreceivedbyaddress "mvF8HKtE6gJ63gCVXED7Z1o8bCYP491bgo"
        String address = "mvF8HKtE6gJ63gCVXED7Z1o8bCYP491bgo";
        byte[] h160 = legacyAddressToHash.apply(address);
        String h160Hex = HEX.encode(h160);
        assertEquals("a18b918798eb17bd9971f617c6c01852d3bddf3a", h160Hex);

        Script p2pkhScript = StandardScripts.hashToP2pkhScript.apply(h160);
        assertEquals("OP_DUP OP_HASH160 a18b918798eb17bd9971f617c6c01852d3bddf3a OP_EQUALVERIFY OP_CHECKSIG", p2pkhScript.asm());
        assertTrue(isP2pkhScriptPubKey.test(p2pkhScript));

        Address addressFromScript = p2pkhScript.address(TESTNET3);
        assertEquals(address, addressFromScript.value());
    }


    @AfterClass
    public static void disableStackDebug() {
        OpCodeFunction.disableStackDebug();  // TODO add this to appropriate classes
    }
}
