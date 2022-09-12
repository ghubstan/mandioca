package mandioca.bitcoin.transaction;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static mandioca.bitcoin.function.ByteArrayFunctions.bytesToInt;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.transaction.TransactionFactory.TxInFactory.createInputs;
import static mandioca.bitcoin.transaction.TxFetcher.fetchRawTx;
import static org.junit.Assert.assertEquals;

public class UTXOGroupTest {

    private static final Logger log = LoggerFactory.getLogger(UTXOGroupTest.class);

    private final String addressWithUTXOs = "mnyFuNjUcaXKQ7s6mgMk261L5Jc9zGrSEg";
    private static final String WIF = "cUPu4XAkgZkysAheCHNd8KpTSt6jR8N7JVmBfCG2TZg5HdXvN5dC";

    // Transactions that funded addressWithUTXOs:
    private final String tx1Hex = "b04029b039da7de994627499b727b9da81fd8fbd562ea0a48fbc01c79752788d";
    private final String tx2Hex = "15c661b7e6cc569a95fd3e2540440897ba234631ce72d2253656f21d43d22388";
    private final String tx3Hex = "7c447a4231841e68bf584d6c2baca86c310ecf25a67bde0d7ba6ea191a8e2f56";
    private final String tx4Hex = "ed61c4cf3107e04d4bd94dfc7aafca9f0a3af841ec319641296cd9235dae2f29";
    private final List<Tx> transactions = new ArrayList<>();

    // maps tx.id to lists of  prevTxIndex, TxOut, & txOut.amountAsLong;  a cache of expected values
    private final UTXOGroup utxoGroup = new UTXOGroup();

    private final long totalUTXOBalance = 6125851L;

    @Before
    public void setup() {
        if (transactions.isEmpty()) {
            transactions.add(fetchRawTx(tx1Hex, true, TESTNET3));
            transactions.add(fetchRawTx(tx2Hex, true, TESTNET3));
            transactions.add(fetchRawTx(tx3Hex, true, TESTNET3));
            transactions.add(fetchRawTx(tx4Hex, true, TESTNET3));
            assertEquals(4, transactions.size());
            utxoGroup.add(transactions, addressWithUTXOs);
            assertEquals(4, utxoGroup.size());
            assertEquals(totalUTXOBalance, utxoGroup.totalAmount());
        }
    }


    @Test
    public void testCreateInputs() {
        String[] inputTxIds = new String[]{tx1Hex, tx2Hex, tx3Hex, tx4Hex};
        int[] inputTxIndexes = utxoGroup.previousTxIndexes();

        TxIn[] txIns = createInputs(inputTxIds, inputTxIndexes);

        List<UTXO> utxos = utxoGroup.getUTXOs();
        for (int i = 0; i < txIns.length; i++) {
            Integer prevIdx = utxos.get(i).getPrevIndex();
            assertEquals(prevIdx, bytesToInt.apply(txIns[i].previousTransactionIndex));
        }
    }

    @Test
    public void testCreateOutputs() {
        TxOut[] txOuts = utxoGroup.txOutputs();
        assertEquals(4, txOuts.length);

        // confirmed by https://www.blockstream.info/testnet/address/mnyFuNjUcaXKQ7s6mgMk261L5Jc9zGrSEg
        assertEquals(5000000L, txOuts[0].getAmountAsLong());
        assertEquals("OP_DUP OP_HASH160 51c2650d96a41be771924b3e8f222980f0ba737f OP_EQUALVERIFY OP_CHECKSIG",
                txOuts[0].getScriptPubKey().asm());

        assertEquals(10000L, txOuts[1].getAmountAsLong());
        assertEquals("OP_DUP OP_HASH160 51c2650d96a41be771924b3e8f222980f0ba737f OP_EQUALVERIFY OP_CHECKSIG",
                txOuts[1].getScriptPubKey().asm());

        assertEquals(50000L, txOuts[2].getAmountAsLong());
        assertEquals("OP_DUP OP_HASH160 51c2650d96a41be771924b3e8f222980f0ba737f OP_EQUALVERIFY OP_CHECKSIG",
                txOuts[2].getScriptPubKey().asm());

        assertEquals(1065851L, txOuts[3].getAmountAsLong());
        assertEquals("OP_DUP OP_HASH160 51c2650d96a41be771924b3e8f222980f0ba737f OP_EQUALVERIFY OP_CHECKSIG",
                txOuts[3].getScriptPubKey().asm());

    }

}
