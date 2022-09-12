package mandioca.bitcoin.transaction;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.rpc.RpcClient;
import mandioca.bitcoin.rpc.RpcCommand;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.transaction.TxFetcher.fetchRawTx;
import static mandioca.bitcoin.transaction.TxFetcher.fetchRawVerboseTx;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;


@Ignore
public class TxVersion2FetcherTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(TxVersion2FetcherTest.class);

    private final RpcClient rpcClient = new RpcClient(true);
    private final RpcCommand rpcCommand = new RpcCommand(null);

    @Test
    public void testTxVersion2Fetch() { // TODO (come back after Chapters 7,8)
        // TxId b67832036646771ecbe90c661b0493257c9c30aaf9fbed83bd81fc4673a6c4fc is Version 2 Tx
        // https://testnet.smartbit.com.au/tx/b67832036646771ecbe90c661b0493257c9c30aaf9fbed83bd81fc4673a6c4fc
        // https://blockstream.info/testnet/api/tx/b67832036646771ecbe90c661b0493257c9c30aaf9fbed83bd81fc4673a6c4fc
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "b67832036646771ecbe90c661b0493257c9c30aaf9fbed83bd81fc4673a6c4fc" true
        String txId = "b67832036646771ecbe90c661b0493257c9c30aaf9fbed83bd81fc4673a6c4fc";
        Tx tx = fetchRawTx(txId, true, TESTNET3);
        String expectedHex = "0200000000010143110754b1e92f4ff3313769424d23f8c896016ebab264d72bb887b3b84239c2000000001716001498d6753b92ba26c45e06e41dd76a33123ee92034feffffff021a8ac5010000000017a914166e8d3f4a844723c062051ca2c7122107cc4a8b87f04902000000000017a914d44408c52495c5fe29d7d4e54fd4500bae632773870247304402207d47afec711320a9140f646bc6dfc3deb4f45bbad31eb98145344d09531c903602201eb3aa67ef58f60749b7ecf1d1350ce0a5e14a7214aca26fe6620db3f7948352012103acaa2cd5248808955bd7ca0b43be0f8c09715bfe09ea2a7ef1d773eac4d439296e941800";
        String actualHex = HEX.encode(tx.serialize());
        assertEquals(expectedHex, actualHex);
        assertEquals(tx.id(), txId);
    }

    @Test
    public void testRawVerboseTxVersion2Fetch() { // TODO (come back after Chapters 7,8)
        // https://testnet.smartbit.com.au/tx/b67832036646771ecbe90c661b0493257c9c30aaf9fbed83bd81fc4673a6c4fc
        // $BITCOIN_HOME/bitcoin-cli -testnet getrawtransaction "b67832036646771ecbe90c661b0493257c9c30aaf9fbed83bd81fc4673a6c4fc" true
        String txId = "b67832036646771ecbe90c661b0493257c9c30aaf9fbed83bd81fc4673a6c4fc";
        Tx tx = fetchRawVerboseTx(txId, true, TESTNET3);
        String expectedHex = "0200000000010143110754b1e92f4ff3313769424d23f8c896016ebab264d72bb887b3b84239c2000000001716001498d6753b92ba26c45e06e41dd76a33123ee92034feffffff021a8ac5010000000017a914166e8d3f4a844723c062051ca2c7122107cc4a8b87f04902000000000017a914d44408c52495c5fe29d7d4e54fd4500bae632773870247304402207d47afec711320a9140f646bc6dfc3deb4f45bbad31eb98145344d09531c903602201eb3aa67ef58f60749b7ecf1d1350ce0a5e14a7214aca26fe6620db3f7948352012103acaa2cd5248808955bd7ca0b43be0f8c09715bfe09ea2a7ef1d773eac4d439296e941800";
        String actualHex = HEX.encode(tx.serialize());
        assertEquals(expectedHex, actualHex);
        assertEquals(tx.id(), txId);
    }
}
