package mandioca.bitcoin.transaction;

import mandioca.bitcoin.ecc.Secp256k1PrivateKey;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static mandioca.bitcoin.function.ByteArrayFunctions.bytesToInt;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.script.ScriptType.P2PKH;
import static mandioca.bitcoin.transaction.TransactionFactory.TxInFactory.createInputs;
import static mandioca.bitcoin.transaction.TransactionFactory.printTxObject;
import static mandioca.bitcoin.transaction.TxFetcher.fetchRawTx;
import static mandioca.bitcoin.transaction.UTXOUtils.*;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.*;

///////////////////////////////////////////////////////////////////////////////////////////////////
// TODO get wifs for new unused testnet addresses w/ coin:
//
//              mgHkxULXeY7PVgxj5Dsez9qz5CSw4iYdmb  sent 0.05010000 tBTC on 2020-02-16T18:22:18-03:00
//              mzV2GFHApNauY5y8QxsHdBNMiCPLqKTHTy  sent 0.08334065 tBTC on 2020-02-17T18:32:18-03:00
//
///////////////////////////////////////////////////////////////////////////////////////////////////

public class TransactionFactoryTest {

    private static final Logger log = LoggerFactory.getLogger(TransactionFactoryTest.class);

    private final String addressWithUTXOs = "mnyFuNjUcaXKQ7s6mgMk261L5Jc9zGrSEg";
    // TODO get wif -> tn-cli dumpprivkey "mnyFuNjUcaXKQ7s6mgMk261L5Jc9zGrSEg"
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
    public void testCreate4In1OutTx() {
        // This test case created new tx "fc2613d17b9bf2e3169e981f4673f057a6a4592a47fb10d190fe8d08ea4d55d9"
        // broadcast on 2020-02-16T12:31:29-03:00
        //  https://www.blockstream.info/testnet/tx/fc2613d17b9bf2e3169e981f4673f057a6a4592a47fb10d190fe8d08ea4d55d9
        // Warning "possibly self transfer"
        //  see https://en.bitcoin.it/wiki/Privacy#Exact_payment_amounts_.28no_change.29
        TxOut[] fundingTxOutputs = utxoGroup.txOutputs();
        long availableBalance = calcAvailableBalance.apply(fundingTxOutputs);
        long fee = calcLowestFee.apply(635L);
        long targetAmount = availableBalance - fee;
        assertFalse(isDoubleSpend.apply(fundingTxOutputs, targetAmount, fee));
        assertFalse(leavesChange.apply(fundingTxOutputs, targetAmount, fee));
        TxIn[] txIns = createInputs(utxoGroup.previousTxIdAndIndexPairs());
        assertEquals(4, txIns.length);
        String targetAddress = "mkEt3FRZ79rfLvtrEe2PcKs6LvxcL9SFDP";
        Tx txObject = TransactionFactory.createTransaction(txIns, targetAddress, targetAmount, fee, P2PKH);
        Secp256k1PrivateKey privateKey = Secp256k1PrivateKey.wifToPrivateKey(WIF, true);
        boolean isSigned = txObject.signAllInputs(privateKey);
        assertTrue(isSigned);
        String expectedHex = "02000000048d785297c701bc8fa4a02e56bd8ffd81dab927b799746294e97dda39b02940b0010000006a47304402201aa5166b3baa2f7b5ced74c6dc0d9926d5d713f16de1362ba2cb61e42de74ff3022022f765017301d37ca003c67b2a08b23ee40511accf631172141d3d9d93f381780121032964ffad6bc2416c73fc660cd2decda5172b6587c091067767a3ef3ef7b68293ffffffff8823d2431df2563625d272ce314623ba97084440253efd959a56cce6b761c615010000006b483045022100fb086341146fd54254b0efcf73275aa59cc8ff9a60bb577efaedc28e7d437b8e02200c70460048f51fd10d12bfe4c66b67e66ad1739aeea9035ded556278f6dc0c060121032964ffad6bc2416c73fc660cd2decda5172b6587c091067767a3ef3ef7b68293ffffffff562f8e1a19eaa67b0dde7ba625cf0e316ca8ac2b6c4d58bf681e8431427a447c000000006b483045022100b6a399653dbedec0a0f2a263affcaa344ce0587cd6b7c8fb7ecc89308707126b02202e8eb695178b5c76d3575f23b6d3f33a89d56bb95922c66a3edfc40dbad45b8b0121032964ffad6bc2416c73fc660cd2decda5172b6587c091067767a3ef3ef7b68293ffffffff292fae5d23d96c29419631ec41f83a0a9fcaaf7afc4dd94b4de00731cfc461ed010000006a473044022006c9980a687673f6a87bdc800fb30be28ef79194b5eaec696250829654283e9c02201b311790508e19003fe9b300d0b3c475dcb7413527b92af30b4f889473e691320121032964ffad6bc2416c73fc660cd2decda5172b6587c091067767a3ef3ef7b68293ffffffff0190765d00000000001976a91433ce6be2942422065bc3d66a89b1e974b6928b0f88ac00000000";
        assertEquals(expectedHex, HEX.encode(txObject.serialize()));
        printTxObject(txObject, targetAmount, fee);
        compareNewInputsWithOldOutputs(txObject, targetAmount, fee);
    }

    private void compareNewInputsWithOldOutputs(Tx txObject, long targetAmount, long fee) {
        TxIn[] txIns = txObject.getDeserializedInputs();
        long availableBalance = 0;
        List<UTXO> utxos = utxoGroup.getUTXOs();
        for (int i = 0; i < utxos.size(); i++) {
            UTXO utxo = utxos.get(i);
            Integer prevIdx = utxos.get(i).getPrevIndex();
            assertEquals(prevIdx, bytesToInt.apply(txIns[i].previousTransactionIndex));
            availableBalance += utxo.getAmount();
        }
        assertEquals(totalUTXOBalance, availableBalance);
        assertEquals(totalUTXOBalance, (targetAmount + fee));
    }
}












