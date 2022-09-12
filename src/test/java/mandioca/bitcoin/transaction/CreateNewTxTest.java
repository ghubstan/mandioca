package mandioca.bitcoin.transaction;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.ecc.Secp256k1PrivateKey;
import mandioca.bitcoin.util.Tuple;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static mandioca.bitcoin.function.CurrencyFunctions.btcToSatoshis;
import static mandioca.bitcoin.script.ScriptType.P2PKH;
import static mandioca.bitcoin.script.processing.OpCodeFunction.disableStackDebug;
import static mandioca.bitcoin.script.processing.OpCodeFunction.enableStackDebug;
import static mandioca.bitcoin.transaction.TransactionFactory.TxInFactory.createInputs;
import static mandioca.bitcoin.transaction.TransactionFactory.createTransaction;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// TODO see https://bitco.in/en/developer-guide#term-consensus-rules
// TODO see https://github.com/bitcoin/bitcoin/blob/46898e7e942b4e04021aac3724eb4f2ec4cf567b/qa/rpc-tests/bip68-sequence.py
// TODO see https://www.codeproject.com/Articles/1151054/Create-a-Bitcoin-transaction-by-hand
//      "sign input connection with the private key of the output tx (utxo)"
// TODO https://klmoney.wordpress.com/bitcoin-dissecting-transactions-part-2-building-a-transaction-by-hand/

// See https://live.blockcypher.com/btc-testnet/decodetx

public class CreateNewTxTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(CreateNewTxTest.class);

    @BeforeClass
    public static void setup() {
        enableStackDebug();
    }

    @Test
    public void testCreate1In1OutTx() {
        long availableBalance = btcToSatoshis.apply("0.05009550");
        TxIn[] txIns = createInputs(new String[]{"ffc7416b95ce5340a10b6906e602c7dc25f1e034ed60dd74213ca868252d3a5c"}, new int[]{0});

        long targetAmount = btcToSatoshis.apply("0.05");
        long fee = btcToSatoshis.apply("0.00001");
        String targetAddress = "mvF8HKtE6gJ63gCVXED7Z1o8bCYP491bgo";
        String changeAddress = "mzRY7E4ovv9dMReoCQo4uJaVpHFqHJRLbb";
        final long changeAmount = availableBalance - targetAmount - fee;

        Tx txObject = createTransaction(txIns, targetAddress, targetAmount, changeAddress, changeAmount, fee, P2PKH);

        String wif = "cRRJCXC7kgrN9bg9hxkDyhFaHbrTDni2tmfw2kMECmcKdixKQxtg";
        Secp256k1PrivateKey privateKey = Secp256k1PrivateKey.wifToPrivateKey(wif, true);
        boolean isSigned = txObject.signInputs(new int[]{0}, privateKey);
        assertTrue(isSigned);

        byte[] rawTx = txObject.serialize();
        log.info("tx size {} bytes (for fee calculation above)", rawTx.length);
        String rawHex = HEX.encode(rawTx);
        log.info("successfully signed raw hex {}", rawHex);
        String expectedHex = "02000000015c3a2d2568a83c2174dd60ed34e0f125dcc702e606690ba14053ce956b41c7ff000000006a4730440220552d5cc3103224c2d78b6297eb2962d81a817e6caf4b3b810a93f181853ab1670220437af87ea213ed43a9ef9e7338594427042bf244c4249d4066fc219e16ccaada01210245a34a9b093068a2671fc2672a9b769d8360805010a34f14b1b7c76674b8b12affffffff02404b4c00000000001976a914a18b918798eb17bd9971f617c6c01852d3bddf3a88ac66210000000000001976a914cf641ebbba97d9a2551dc9284dfefa6b763b910488ac00000000";
        assertEquals(expectedHex, rawHex);
        // Decode the raw hex @
        //  https://live.blockcypher.com/btc-testnet/decodetx
        //
        // Successfully broadcast tx with Id:  57d770985d29c09cd42a39b6f854c91328a4ed46c22f94a0b51e20ea83f00aba
        //   on  2020-02-13
        // https://www.blockstream.info/testnet/tx/57d770985d29c09cd42a39b6f854c91328a4ed46c22f94a0b51e20ea83f00aba
        //
        //  Warnings:
        //      0.00002 tBTC (8.8 sat/vB) ⓘ overpaying by 785%
        //      This transaction could save 36% on fees by upgrading to native SegWit-Bech32 or 26% by upgrading to SegWit-P2SH
        //
        //  Privacy Warning:
        //      Round numbers  [ See https://en.bitcoin.it/wiki/Privacy#Round_numbers ]
        //  Many payment amounts are round numbers, for example 1 BTC or 0.1 BTC. The leftover change amount would then
        //  be a non-round number (e.g. 1.78213974 BTC). This potentially useful for finding the change address.
        //  The amount may be a round number in another currency.
        //  The amount 2.24159873 BTC isn't round in bitcoin but when converted to USD it may be close to $100.
    }

    @Test
    public void testCreate2In2OutTxObject() {

        long availableBalance = btcToSatoshis.apply("0.39103878");

        TxIn[] txIns = createInputs(
                new Tuple<>("57d770985d29c09cd42a39b6f854c91328a4ed46c22f94a0b51e20ea83f00aba", 0),
                new Tuple<>("6c1c04dee88543a408fcaf3a0eaf3b37ae1e3eddf3c51326d44b69603b4e5050", 0));
        //  use either factory method to get TxIn[] txIns
        // TxIn[] txIns = createInputs(
        //         new String[]{"57d770985d29c09cd42a39b6f854c91328a4ed46c22f94a0b51e20ea83f00aba", "6c1c04dee88543a408fcaf3a0eaf3b37ae1e3eddf3c51326d44b69603b4e5050"},
        //        new int[]{0, 0});

        String targetAddress = "mk6X32Faw9dyuFRtDdC2YzAo8E4rRqqh9t";
        long targetAmount = btcToSatoshis.apply("0.3910317");

        long fee = btcToSatoshis.apply("0.00000374"); // 374 sats fee (confirmed in raw decoder site)

        String changeAddress = "mmb9rjVgSovjf24rjwG6qJ7xHeigWaKvko";
        final long changeAmount = availableBalance - targetAmount - fee;  // change amt will be  334 sats
        assertEquals((availableBalance - changeAmount), (targetAmount + fee));

        Tx txObject = createTransaction(txIns, targetAddress, targetAmount, changeAddress, changeAmount, fee, P2PKH);

        // sign input 1
        String wif1 = "cQuXL1T9AcHKsy8jr4ASzWjGeSuQPrZr2GMv3fBDiremfvAHek4E";  // bitcoin-cli dumpprivkey "mvF8HKtE6gJ63gCVXED7Z1o8bCYP491bgo"
        Secp256k1PrivateKey privateKey1 = Secp256k1PrivateKey.wifToPrivateKey(wif1, true);
        boolean isSigned = txObject.signInputs(new int[]{0}, privateKey1);
        assertTrue(isSigned);

        // sign input 2
        String wif2 = "cUF3JTYBxfGpmFBurEYYQaMYkqyE29MFyeec7EkG9A3Ydyd3NYv4";  // bitcoin-cli dumpprivkey "n3bYWKwSwKFwB59pLJW1L1VhDkMxVwCzZt"
        Secp256k1PrivateKey privateKey2 = Secp256k1PrivateKey.wifToPrivateKey(wif2, true);
        isSigned = txObject.signInputs(new int[]{1}, privateKey2);
        assertTrue(isSigned);

        byte[] rawTx = txObject.serialize();
        log.info("tx size {} bytes (for fee calculation above)", rawTx.length);  // 372 bytes
        String rawHex = HEX.encode(rawTx);
        log.info("successfully signed raw hex {}", rawHex);
        String expectedHex = "0200000002ba0af083ea201eb5a0942fc246eda42813c954f8b6392ad49cc0295d9870d757000000006a47304402207e2d359ee6ead77fa51edb545566af636377d10813f5ceb327e77eb699c40bf4022027452e1ab99eb1aba9376efe24242e6e44daa8c1b446fbfcdce9d99e23663b41012103eae76d178b490a68137d1a58fc24ec7e6244882da4937a391aade0ff811b3e6dffffffff50504e3b60694bd42613c5f3dd3e1eae373baf0e3aaffc08a44385e8de041c6c000000006a473044022037b687a2ba75c256af5bcff25b99cb0ba89bed919d0d55b6ad438e122ed91d7d0220166521c4fc46ef86dea600b92fe03f5309c5dfc7278ee244bf3b758d2eb6d450012103a41c3452000f36e1b77f7a2282e88e2fc589706d49175d758fe11e7e7d6463feffffffff02c2aa5402000000001976a91432398f7ff1c0c87078ddc76a0db8fc48feef317c88ac4e010000000000001976a914429c123ba42b66c1ac2e76951ac1d27afdfbbe9f88ac00000000";
        assertEquals(expectedHex, rawHex);
    }

    @AfterClass
    public static void teardown() {
        disableStackDebug();
    }

}
