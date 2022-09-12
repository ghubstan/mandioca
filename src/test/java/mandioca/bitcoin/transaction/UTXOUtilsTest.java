package mandioca.bitcoin.transaction;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.transaction.TxFetcher.fetchRawTx;
import static mandioca.bitcoin.transaction.UTXOUtils.*;
import static org.junit.Assert.*;

public class UTXOUtilsTest {

    private static final Logger log = LoggerFactory.getLogger(UTXOUtilsTest.class);

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
    public void testCalcAvailableBalance() {
        TxOut[] txOuts = utxoGroup.txOutputs();
        long availableBalance = calcAvailableBalance.apply(txOuts);
        assertEquals(totalUTXOBalance, availableBalance);
    }

    @Test
    public void testIsDoubleSpend() {
        TxOut[] txOuts = utxoGroup.txOutputs();
        long targetAmount = this.totalUTXOBalance - 5000L;

        long fee = 5001L; // over by 1 sat
        assertTrue(isDoubleSpend.apply(txOuts, targetAmount, fee));

        fee = 5000L; // zero change
        assertFalse(isDoubleSpend.apply(txOuts, targetAmount, fee));
    }

    @Test
    public void testLeavesChange() {
        TxOut[] txOuts = utxoGroup.txOutputs();
        long targetAmount = this.totalUTXOBalance - 5000L;

        long fee = 4999L; // 1 sat change
        assertTrue(leavesChange.apply(txOuts, targetAmount, fee));

        fee = 5000L; // 1 sat change
        assertFalse(leavesChange.apply(txOuts, targetAmount, fee));
    }

    @Test
    public void testCalcLowestFee() {
        long fee = calcLowestFee.apply(1024L);
        log.info("1 kB tx's lowest fee = {}", fee);

        fee = calcLowestFee.apply(512L);
        log.info("0.5 kB tx's lowest fee = {}", fee);
        // TODO asserts
    }

}
