package mandioca.bitcoin.transaction;

import mandioca.bitcoin.ecc.Secp256k1PrivateKey;
import mandioca.bitcoin.network.block.BloomFilter;
import mandioca.bitcoin.network.block.BloomFilterQuery;
import mandioca.bitcoin.network.message.MerkleBlockMessage;
import mandioca.bitcoin.util.Tuple;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static mandioca.bitcoin.function.CurrencyFunctions.btcToSatoshis;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.script.ScriptType.P2PKH;
import static mandioca.bitcoin.transaction.TransactionFactory.TxOutFactory.createOutputs;
import static mandioca.bitcoin.transaction.TxFetcher.fetchRawTx;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.*;

// TODO dedup code with BloomFilterExamplesTest (and move to src/main)

public class CreateNewTxUsingBloomFilter {

    private static final Logger log = LoggerFactory.getLogger(CreateNewTxUsingBloomFilter.class);

    // Use this address, created 2020-02-14, and rcvd some tBTC on same day
    // $BITCOIN_HOME/bitcoin-cli -testnet getnewaddress
    //          mkpgeHP5Fxa6sX9zdLGbGLWxqNduRZ5SdR
    //      tn-cli dumpprivkey "mkpgeHP5Fxa6sX9zdLGbGLWxqNduRZ5SdR"
    //              cQY3Jn95j6UwQopcE7NEKEKpqX2LQoUA1XPR1LjFBDbEuN8RvtSs

    @Test
    public void testFindFundingTxWithBloomFilterForNewTx() {
        // The objective is to not risk privacy leak by using a block explorer.
        // TODO code this in src/main

        try {
            String myFundedAddress = "mkpgeHP5Fxa6sX9zdLGbGLWxqNduRZ5SdR";  // this will be the spending address
            String wif = "cQY3Jn95j6UwQopcE7NEKEKpqX2LQoUA1XPR1LjFBDbEuN8RvtSs"; // pk to funds to be spent
            Secp256k1PrivateKey privateKey = Secp256k1PrivateKey.wifToPrivateKey(wif, true);
            String newWif = privateKey.getWif(true, true);  // original was an compressed, testnet WIF
            assertEquals(wif, newWif);

            // this is the tx that sent some tBTC to mkpgeHP5Fxa6sX9zdLGbGLWxqNduRZ5SdR
            String txId = "b2894a9f2a8b3ff04cfc05460189a207982cd68cb21576a655a7ce6788c1276f";
            Tx fundingTx = fetchRawTx(txId, true, TESTNET3);
            log.info("creating bloom filter to find funding tx with id = {}", fundingTx.id());

            // use a bloom filter, (not block explorer) to find utxo for input to new tx
            String[] addresses = new String[]{"mkpgeHP5Fxa6sX9zdLGbGLWxqNduRZ5SdR"};
            BloomFilter bloomFilter = new BloomFilter(30, 6, 3504);
            BloomFilterQuery bloomFilterQuery = new BloomFilterQuery(addresses, bloomFilter);

            // $ tn-cli getblockhash  1664000
            String blockLocator = "000000000000008f916a2f3b39f47a6b524663e356d0f4d38ca98823fc136600";
            boolean gotResults = bloomFilterQuery.runQuery(blockLocator);
            assertTrue(gotResults);

            List<MerkleBlockMessage> merkleBlockMessages = bloomFilterQuery.getMerkleBlocks();
            log.info("downloaded {} merkleblock msgs", merkleBlockMessages.size());
            assertTrue(merkleBlockMessages.size() >= 1875); // as of  2020-02-14T14:26:30-03:00

            List<Tx> transactions = bloomFilterQuery.getAllTransactions();
            int totalTxOutCount = getTotalTxOutCount(transactions);
            log.info("downloaded {} transactions containing a total of {} tx-outputs", transactions.size(), totalTxOutCount);

            List<Tx> matchingTransactions = bloomFilterQuery.getMatchingTransactions();
            int matchingTxCount = matchingTransactions.size();
            log.info("found {} matching transactions", matchingTxCount);

            UTXOGroup utxoGroup = new UTXOGroup();
            utxoGroup.add(matchingTransactions, myFundedAddress);
            TxIn[] txIns = utxoGroup.txInputs();

            // Now I have an available balance from the utxos; create a new tx and broadcast it (less fee, no change)
            long availableBalance = utxoGroup.totalAmount();
            long fee = btcToSatoshis.apply("0.00000550"); // see if this flies in bitcoin-cli sendrawtransaction
            long targetAmount = availableBalance - fee;
            long changeAmount = 0L;
            
            TxOut[] txOuts = createOutputs(P2PKH,
                    new Tuple<>("mmAsKc1jq5oKf4ukHoKNZuxXp99GfBJn9L", targetAmount),
                    new Tuple<>("mknYaB5NHf3MGqKzS3b95YkhiTiten6eH3", changeAmount));

            Tx txObject = new Tx(2, txIns, txOuts, 0, TESTNET3);
            // sign all 3 inputs with same private key for funded address 'mkpgeHP5Fxa6sX9zdLGbGLWxqNduRZ5SdR'
            boolean isSigned = txObject.signAllInputs(privateKey);
            assertTrue(isSigned);

            byte[] rawTx = txObject.serialize();
            log.info("tx size {} bytes (for fee calculation above)", rawTx.length);  // 520 bytes, have to increase fee
            String rawHex = HEX.encode(rawTx);
            log.info("successfully signed raw hex {}", rawHex);
            String expectedHex = "02000000034ffd7e8f4ff182c35b841895dee079fc7740afe3a3a1634204ebe4884405deb4000000006b483045022100c1065ed658fcd2fe84c3814dd74af39eca7bdba7d820a8251af70cd461b877c402206d38bb7c65933e82b894e8cbc838b19ea6b97b1b87f6acb6868dc6478d4dc3a3012102e1bdcf534527b5d1e6daca2767bc36669d8cad6c5690a55e16a50fe818eb8f80ffffffff4ed603e8ae49d777a38c1dee71f77b42729885fbf6027daa051a22c695bbe32c010000006a473044022058ceafd2b4dc25cf379847edc2aa46b4d378b12f3b92b5c871cd005a9937ac3002203e242e872f5f95bc3f2b744f7fc79fcc62823a1aa7163a8a7318833c4cb73652012102e1bdcf534527b5d1e6daca2767bc36669d8cad6c5690a55e16a50fe818eb8f80ffffffff6f27c18867cea755a67615b28cd62c9807a289014605fc4cf03f8b2a9f4a89b2010000006a473044022019fbbd076500a473ebc1efe067c73b87c3edff47ec399757580e5ea6c1b6e48002201ffc924ed4d0df734bad3d5ea238764c3d8c3784d8de70af90019041b8c2bf2f012102e1bdcf534527b5d1e6daca2767bc36669d8cad6c5690a55e16a50fe818eb8f80ffffffff0254cd1400000000001976a9143e0448b3b1e8eb631d8a69a3d768b98b6c8f072a88ac00000000000000001976a91439cb7b6ccc5e114e1f325667b58b8fc1f47d537288ac00000000";
            assertEquals(expectedHex, rawHex);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getCause().getMessage());
        }
        // https://testnet.smartbit.com.au/tx/bc86ca3fd374187e23e26f8652d0becd0024225f9fb03e304d31b1192a9be2ad
        // https://live.blockcypher.com/btc-testnet/tx/bc86ca3fd374187e23e26f8652d0becd0024225f9fb03e304d31b1192a9be2ad
        // https://www.blockstream.info/testnet/tx/bc86ca3fd374187e23e26f8652d0becd0024225f9fb03e304d31b1192a9be2ad
        // No privacy warnings from blockstream
    }

    @Test
    public void testParseNewTxCreatedWithoutBlockExplorer() {
        // this is the tx that sent some tBTC to mkpgeHP5Fxa6sX9zdLGbGLWxqNduRZ5SdR
        String newTxId = "bc86ca3fd374187e23e26f8652d0becd0024225f9fb03e304d31b1192a9be2ad";
        Tx newTx = fetchRawTx(newTxId, true, TESTNET3);
        log.info("new tx with id = {}\n{}", newTx.id(), newTx);
    }

    private int getTotalTxOutCount(List<Tx> transactions) {
        int count = 0;
        for (Tx tx : transactions) {
            count += tx.getDeserializedOutputs().length;
        }
        return count;
    }

}
