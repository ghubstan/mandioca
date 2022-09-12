package mandioca.bitcoin.transaction;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.script.Script;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static mandioca.bitcoin.function.ByteArrayFunctions.bytesToLong;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.transaction.TransactionSerializer.LOCKTIME_0;
import static mandioca.bitcoin.transaction.TransactionSerializer.VERSION_1;
import static mandioca.bitcoin.transaction.TxFetcher.fetch;
import static mandioca.bitcoin.transaction.TxFetcher.fetchRawTx;
import static mandioca.bitcoin.transaction.TxIn.SEQUENCE_0xFFFFFFFF;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TxVersion1FetcherTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(TxVersion1FetcherTest.class);

    @Test
    public void testFetchNonWalletTxId_eb5712033701da8efd28737fa609bb74398e37d606ad81d429f2869fe01755ed() {
        // TxId eb5712033701da8efd28737fa609bb74398e37d606ad81d429f2869fe01755ed is Version 1 Tx
        // https://testnet.smartbit.com.au/tx/eb5712033701da8efd28737fa609bb74398e37d606ad81d429f2869fe01755ed
        // https://blockstream.info/testnet/api/tx/eb5712033701da8efd28737fa609bb74398e37d606ad81d429f2869fe01755ed
        // $BITCOIN_HOME/bitcoin-cli -testnet gettransaction "eb5712033701da8efd28737fa609bb74398e37d606ad81d429f2869fe01755ed"
        String txId = "eb5712033701da8efd28737fa609bb74398e37d606ad81d429f2869fe01755ed";
        exception.expect(RuntimeException.class);
        exception.expectMessage("Rpc Error:  -5, Invalid or non-wallet transaction id");
        // TODO build out RcpServerErrorException(with response), and figure out the best place to catch it
        fetch(txId, true, TESTNET3);
    }

    @Test
    public void testFetchRawTxId_687f70bd4b071ca2733667f68c1b74c5561b6cf681406ed60a8d4c8effa0a9a7() {
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "687f70bd4b071ca2733667f68c1b74c5561b6cf681406ed60a8d4c8effa0a9a7" true
        // Hex:  0100000001eafb99c6b24e9285cf0ed457a028d723e61c80c443e370c84cf96507958dd610010000008b483045022100b95d83b8b81a0491cc24b7f91dc7b8f961a050b0ee5371eaa55e1d94bb89a7b902204524c390855cc384afc6d72e0b4f2ea72c9bad6a3a54f2ad5501b998b442fd750141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff02287d0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388acb34caf0b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000
        // TxId:        687f70bd4b071ca2733667f68c1b74c5561b6cf681406ed60a8d4c8effa0a9a7
        // vinTxId:     10d68d950765f94cc870e343c4801ce623d728a057d40ecf85924eb2c699fbea
        String txId = "687f70bd4b071ca2733667f68c1b74c5561b6cf681406ed60a8d4c8effa0a9a7";
        Tx tx = fetchRawTx(txId, true, TESTNET3);
        assertArrayEquals(VERSION_1, tx.version);
        String expectedHex = "0100000001eafb99c6b24e9285cf0ed457a028d723e61c80c443e370c84cf96507958dd610010000008b483045022100b95d83b8b81a0491cc24b7f91dc7b8f961a050b0ee5371eaa55e1d94bb89a7b902204524c390855cc384afc6d72e0b4f2ea72c9bad6a3a54f2ad5501b998b442fd750141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff02287d0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388acb34caf0b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000";
        String actualHex = HEX.encode(tx.serialize());
        assertEquals(expectedHex, actualHex);
        assertEquals(tx.id(), txId);
        assertArrayEquals(LOCKTIME_0, tx.locktime);

        assertEquals(1, tx.getDeserializedInputs().length);
        assertArrayEquals(SEQUENCE_0xFFFFFFFF, reverse.apply(tx.getDeserializedInputs()[0].sequence));

        // There is only 1 input for txId = "687f70bd4b071ca2733667f68c1b74c5561b6cf681406ed60a8d4c8effa0a9a7"
        TxIn txIn = tx.getDeserializedInputs()[0];
        assertEquals("10d68d950765f94cc870e343c4801ce623d728a057d40ecf85924eb2c699fbea", HEX.encode(txIn.previousTransactionId));
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "10d68d950765f94cc870e343c4801ce623d728a057d40ecf85924eb2c699fbea" true
        byte[] txInValue = txIn.value(TESTNET3);
        long expectedValue = 196220923L;
        long actualValue = bytesToLong.apply(txInValue);
        assertEquals(expectedValue, actualValue);

        Script scriptPubKey = txIn.scriptPubKey(TESTNET3);
        assertEquals("OP_DUP OP_HASH160 36a5ee46338acf885538ebd709a810b361c93a43 OP_EQUALVERIFY OP_CHECKSIG", scriptPubKey.asm());

        assertEquals(2, tx.getDeserializedOutputs().length);
        assertEquals(163112L, tx.getDeserializedOutputs()[0].getAmountAsLong());
        assertEquals(196037811L, tx.getDeserializedOutputs()[1].getAmountAsLong());
    }

    @Test
    public void testFetchRawTxId_3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86() {
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86" true
        // Hex:  01000000018fe1ada6f06c2fd2080f0ae73fbbbd14061ca572eeaa6342a66191d6777c7c70010000008b483045022100c4558ec0232c4aaafad956b777d73637adb6b5eaf9ba1c8de86607043bc96bd6022075715ea626bd2ea281e531ac34816132b80995a7541ad2a33e233eea78572a4c0141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff02d8e3b40b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388acce7b0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000
        // TxId:        3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86
        // vinTxId:     707c7c77d69161a64263aaee72a51c0614bdbb3fe70a0f08d22f6cf0a6ade18f
        String txId = "3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86";
        Tx tx = fetchRawTx(txId, true, TESTNET3);
        assertArrayEquals(VERSION_1, tx.version);
        String expectedHex = "01000000018fe1ada6f06c2fd2080f0ae73fbbbd14061ca572eeaa6342a66191d6777c7c70010000008b483045022100c4558ec0232c4aaafad956b777d73637adb6b5eaf9ba1c8de86607043bc96bd6022075715ea626bd2ea281e531ac34816132b80995a7541ad2a33e233eea78572a4c0141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff02d8e3b40b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388acce7b0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000";
        String actualHex = HEX.encode(tx.serialize());
        assertEquals(expectedHex, actualHex);
        assertEquals(tx.id(), txId);
        assertArrayEquals(LOCKTIME_0, tx.locktime);

        // There is only 1 input for txId = "3f398a03e07e20f256426b423c9a6412023c2af5c785c6a0b63e05697f310f86"
        TxIn txIn = tx.getDeserializedInputs()[0];
        assertEquals("707c7c77d69161a64263aaee72a51c0614bdbb3fe70a0f08d22f6cf0a6ade18f", HEX.encode(txIn.previousTransactionId));
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "707c7c77d69161a64263aaee72a51c0614bdbb3fe70a0f08d22f6cf0a6ade18f" true
        byte[] txInValue = txIn.value(TESTNET3);
        long expectedValue = 196586950L;
        long actualValue = bytesToLong.apply(txInValue);
        assertEquals(expectedValue, actualValue);

        Script scriptPubKey = txIn.scriptPubKey(TESTNET3);
        assertEquals("OP_DUP OP_HASH160 36a5ee46338acf885538ebd709a810b361c93a43 OP_EQUALVERIFY OP_CHECKSIG", scriptPubKey.asm());

        assertEquals(2, tx.getDeserializedOutputs().length);
        assertEquals(196404184L, tx.getDeserializedOutputs()[0].getAmountAsLong());
        assertEquals(162766L, tx.getDeserializedOutputs()[1].getAmountAsLong());
    }

    @Test
    public void testFetchRawTxId_4dfd2fcda2812af3c5ab582a1ce3835bfadfd0c6979dfc0e0d379c506db36741() {
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "4dfd2fcda2812af3c5ab582a1ce3835bfadfd0c6979dfc0e0d379c506db36741" true
        // Hex:  0100000001beb69712e1758c06412fded8459759dc09f3bb2ff062d970f3f7e496eb52d9b8000000008a473044022013c7b9895e185930a35f558bcef646ea79fb0e6dfd43061adf9fc2f6b70de4c50220417a82211424d9ef3b79e94c53bb9ad33381ca326980e2875217d80e1c1156d20141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff020c7bba0b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac8e7e0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000
        // TxId:        4dfd2fcda2812af3c5ab582a1ce3835bfadfd0c6979dfc0e0d379c506db36741
        // vinTxId:     b8d952eb96e4f7f370d962f02fbbf309dc599745d8de2f41068c75e11297b6be
        String txId = "4dfd2fcda2812af3c5ab582a1ce3835bfadfd0c6979dfc0e0d379c506db36741";
        Tx tx = fetchRawTx(txId, true, TESTNET3);
        assertArrayEquals(VERSION_1, tx.version);
        String expectedHex = "0100000001beb69712e1758c06412fded8459759dc09f3bb2ff062d970f3f7e496eb52d9b8000000008a473044022013c7b9895e185930a35f558bcef646ea79fb0e6dfd43061adf9fc2f6b70de4c50220417a82211424d9ef3b79e94c53bb9ad33381ca326980e2875217d80e1c1156d20141048aa0d470b7a9328889c84ef0291ed30346986e22558e80c3ae06199391eae21308a00cdcfb34febc0ea9c80dfd16b01f26c7ec67593cb8ab474aca8fa1d7029dffffffff020c7bba0b000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac8e7e0200000000001976a91436a5ee46338acf885538ebd709a810b361c93a4388ac00000000";
        String actualHex = HEX.encode(tx.serialize());
        assertEquals(expectedHex, actualHex);
        assertEquals(tx.id(), txId);
        assertArrayEquals(LOCKTIME_0, tx.locktime);

        // There is only 1 input for txId = "4dfd2fcda2812af3c5ab582a1ce3835bfadfd0c6979dfc0e0d379c506db36741"
        TxIn txIn = tx.getDeserializedInputs()[0];
        assertEquals("b8d952eb96e4f7f370d962f02fbbf309dc599745d8de2f41068c75e11297b6be", HEX.encode(txIn.previousTransactionId));
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "b8d952eb96e4f7f370d962f02fbbf309dc599745d8de2f41068c75e11297b6be" true
        byte[] txInValue = txIn.value(TESTNET3);
        long expectedValue = 196954042L;
        long actualValue = bytesToLong.apply(txInValue);
        assertEquals(expectedValue, actualValue);

        Script scriptPubKey = txIn.scriptPubKey(TESTNET3);
        assertEquals("OP_DUP OP_HASH160 36a5ee46338acf885538ebd709a810b361c93a43 OP_EQUALVERIFY OP_CHECKSIG", scriptPubKey.asm());

        assertEquals(2, tx.getDeserializedOutputs().length);
        assertEquals(196770572L, tx.getDeserializedOutputs()[0].getAmountAsLong());
        assertEquals(163470L, tx.getDeserializedOutputs()[1].getAmountAsLong());
    }
}
