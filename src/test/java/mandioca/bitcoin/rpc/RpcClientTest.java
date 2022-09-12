package mandioca.bitcoin.rpc;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.rpc.response.*;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.TestCase.assertTrue;
import static mandioca.bitcoin.rpc.RpcCommand.*;
import static mandioca.bitcoin.rpc.error.ErrorCode.isValidErrorCode;
import static org.junit.Assert.*;

// Depends on running bitcoind -deprecatedrpc=generate -testnet -daemon on localhost

public class RpcClientTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(RpcClientTest.class);

    /*
     if testnet:
            return 'https://blockstream.info/testnet/api/'
        else:
            return 'https://blockstream.info/api/'
     */

    // See https://github.com/bitcoin/bitcoin/blob/master/doc/JSON-RPC-interface.md
    // See https://github.com/bitcoin/bitcoin/blob/master/doc/REST-interface.md
    // See https://en.bitcoin.it/wiki/API_reference_%28JSON-RPC%29#Java

    // These curl cmds work:  (-d is POST request data)
    // curl -v --basic -u me:password  127.0.0.1:5000/ -d "{\"jsonrpc\":\"2.0\",\"id\":\"0\",\"method\":\"getmemoryinfo\"}" -H 'Content-Type:application/json';
    // curl -v --basic -u me:password  127.0.0.1:5000/ -d "{\"jsonrpc\":\"2.0\",\"id\":\"0\",\"method\":\"getwalletinfo\"}" -H 'Content-Type:application/json';
    // curl -v --basic -u me:password  127.0.0.1:5000/ -d "{\"jsonrpc\":\"2.0\",\"id\":\"0\",\"method\":\"getnettotals\"}" -H 'Content-Type:application/json';
    // curl -v --basic -u me:password  127.0.0.1:5000/ -d "{\"jsonrpc\":\"2.0\",\"id\":\"0\",\"method\":\"gettransaction\", \"params\":[\"32a39a60143e6cd027247925658e81914e2a57fdd320829c8cb38bd7c9981d69\"]}" -H 'Content-Type:application/json'
    // Note:  Use -txindex to enable blockchain transaction queries.
    // curl -v --basic -u me:password  127.0.0.1:5000/ -d "{\"jsonrpc\":\"2.0\",\"id\":\"0\",\"method\":\"getrawtransaction\", \"params\":[\"5b4b5ff628af229de84d04fc7adafd2b2950e9d9b5dc50312ffadf88f6be5fec\", \"true\"]}" -H 'Content-Type:application/json'

    private final RpcClient rpcClient = new RpcClient(true);
    private final RpcCommand rpcCommand = new RpcCommand();

    @Test
    public void testGetBlockCount() {
        rpcCommand.configure.apply(GET_BLOCKCOUNT[0], GET_BLOCKCOUNT[1]);
        GetBlockCountResponse response = (GetBlockCountResponse) rpcClient.runCommand(rpcCommand);
        assertNull(response.getRpcErrorResponse());
        //log.info("GetBlockCountResponse response:\n{}", response);
    }

    @Test
    public void testGetBlockHash() {
        rpcCommand.configureWithParams.apply(GET_BLOCKHASH[0], new Object[]{100_000}, GET_BLOCKHASH[1]);
        GetBlockHashResponse response = (GetBlockHashResponse) rpcClient.runCommand(rpcCommand);
        assertNull(response.getRpcErrorResponse());
        //log.info("GetBlockHashResponse response:\n{}", response);
    }

    @Test
    public void testGetNonWalletTransaction1() {
        // $BITCOIN_HOME/bitcoin-cli -testnet gettransaction "3a9837137373cc9fbe7e35951caa91444654eb88e5b152556ff32772e26f5901"
        // error code: -5
        // error message:
        // Invalid or non-wallet transaction id
        String txId = "3a9837137373cc9fbe7e35951caa91444654eb88e5b152556ff32772e26f5901";
        rpcCommand.configureWithParams.apply(GET_TRANSACTION[0], new String[]{txId}, GET_TRANSACTION[1]);
        // exception.expect(RuntimeException.class);
        // exception.expectMessage("RpcErrorResponse{code=-5, message='Invalid or non-wallet transaction id'}");
        GetTransactionResponse response = (GetTransactionResponse) rpcClient.runCommand(rpcCommand);
        assertNotNull(response.getRpcErrorResponse());
        // log.error(response.getRpcErrorResponse().toString());
        // TODO build out RcpServerErrorException(with response), and figure out the best place to throw it
    }

    @Test
    public void testBlockChainInfo() {
        rpcCommand.configure.apply(GET_BLOCKCHAIN_INFO[0], GET_BLOCKCHAIN_INFO[1]);
        GetBlockChainInfoResponse response = (GetBlockChainInfoResponse) rpcClient.runCommand(rpcCommand);
        // log.info("testBlockChainInfo response:\n{}", response);
        assertEquals("test", response.getChain());
        assertEquals("Warning: unknown new rules activated (versionbit 28)", response.getWarnings());
    }

    @Test
    public void testGetWalletInfo() {
        rpcCommand.configure.apply(GET_WALLET_INFO[0], GET_WALLET_INFO[1]);
        GetWalletInfoResponse response = (GetWalletInfoResponse) rpcClient.runCommand(rpcCommand);
        //log.info("testGetWalletInfo response:\n{}", response);
        assertNull(response.getRpcErrorResponse());
    }


    @Test
    public void testGetTransaction1() {
        // txid of 1st testnet faucet withdrawal
        // https://coinfaucet.eu/en/btc-testnet
        // https://live.blockcypher.com/btc-testnet/tx/32a39a60143e6cd027247925658e81914e2a57fdd320829c8cb38bd7c9981d69
        // https://live.blockcypher.com/btc-testnet/address/2NCbak49iCGk9kZ4Ehrwf34YtUpmp9b47Dc
        String txId = "32a39a60143e6cd027247925658e81914e2a57fdd320829c8cb38bd7c9981d69";
        rpcCommand.configureWithParams.apply(GET_TRANSACTION[0], new String[]{txId}, GET_TRANSACTION[1]);
        GetTransactionResponse response = (GetTransactionResponse) rpcClient.runCommand(rpcCommand);
        //log.info("testGetTransaction1 response:\n{}", response);
        RpcErrorResponse error = response.getRpcErrorResponse();
        assertEquals(-5, error.getCode());
        assertEquals("Invalid or non-wallet transaction id", error.getMessage());

        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION_VERBOSE[0], new Object[]{txId, true}, GET_RAWTRANSACTION_VERBOSE[1]);
        GetRawTransactionVerboseResponse rawTxResponseVerbose = (GetRawTransactionVerboseResponse) rpcClient.runCommand(rpcCommand);
        // log.info("testGetRawTransactionVerbose response:\n{}", rawTxResponseVerbose);
        String expectedHex = "020000000001014b015954d4f757156e75ae56b735ce63646b88c0ea16c52afa7f798ccaa13634000000001716001464dbdc61195cccd6ea422ec55b2f2a69fe401aa2feffffff0213e9ea650000000017a914e74be3850639ce8c29654c94d0105660a310843887b0462a000000000017a914d44408c52495c5fe29d7d4e54fd4500bae632773870247304402204912a63435de48964b569416b4ed672b5c86e11b62a07429c2962dfc5fcde7b6022004c120c36aff6fb9af412d0811538685dc230289ee367533574852d9b77634610121038b869ef15205f2da1300d14a88a7dc837f930b094b5f08cad80b88f301ed5d17628b1800";
        assertEquals(expectedHex, rawTxResponseVerbose.getHex());
        assertEquals(2, rawTxResponseVerbose.getVersion());
        assertEquals("32a39a60143e6cd027247925658e81914e2a57fdd320829c8cb38bd7c9981d69", rawTxResponseVerbose.getTxid());
        assertEquals("013babb9b013c1efb63fef5b3e089da95cf87a4dfb85f1fa5cca300194eaa536", rawTxResponseVerbose.getHash());
        assertEquals("000000000000021399d4f9d14a288ead96bb295b4e786370460523bbc57e05cf", rawTxResponseVerbose.getBlockhash());
        assertEquals(1573856407, rawTxResponseVerbose.getTime());
        assertEquals(1608546, rawTxResponseVerbose.getLocktime());
        assertEquals(1, rawTxResponseVerbose.getVin().length);
        assertEquals(2, rawTxResponseVerbose.getVout().length);
    }

    @Test
    public void testGetTransaction2() {
        // txid of 2nd testnet faucet withdrawal
        // https://coinfaucet.eu/en/btc-testnet
        // https://live.blockcypher.com/btc-testnet/tx/ddd90b0ffb360eb8a25293d049edec5b556cdf29bcdcbc3caa4f57099b09575b9
        // https://live.blockcypher.com/btc-testnet/address/2NCbak49iCGk9kZ4Ehrwf34YtUpmp9b47Dc
        String txId = "dd90b0ffb360eb8a25293d049edec5b556cdf29bcdcbc3caa4f57099b09575b9";
        rpcCommand.configureWithParams.apply(GET_TRANSACTION[0], new String[]{txId}, GET_TRANSACTION[1]);
        GetTransactionResponse response = (GetTransactionResponse) rpcClient.runCommand(rpcCommand);
        //log.info("testGetTransaction2 response:\n{}", response);
        RpcErrorResponse error = response.getRpcErrorResponse();
        assertEquals(-5, error.getCode());
        assertEquals("Invalid or non-wallet transaction id", error.getMessage());

        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION_VERBOSE[0], new Object[]{txId, true}, GET_RAWTRANSACTION_VERBOSE[1]);
        GetRawTransactionVerboseResponse rawTxResponseVerbose = (GetRawTransactionVerboseResponse) rpcClient.runCommand(rpcCommand);
        //log.info("testGetRawTransactionVerbose response:\n{}", rawTxResponseVerbose);
        String expectedHex = "02000000000101cce7c993ffb64386d5900deffccdbf9e73fec28fee4af5cc0e0052bab53bbbc0000000001716001452d70a7c72481742d135c63112b77360ddd13b60feffffff02a31415270000000017a914b8cb9b60243a4e6ef6247815400f8fcfbc160b1687e84037000000000017a914d44408c52495c5fe29d7d4e54fd4500bae632773870247304402201dd7de40b5ec6d24c6e9269e87824617c6708d9d32d539d4d2c366ed03ce977e0220636ec2ee1cdcb13b5b7f625f2f630da12cec77518bf6ee81c6c55b3b2727b496012102156ca8c5748a8f33f91d6bda4716ec2aab493f1cd854cff92ea0ae51e44e1488048c1800";
        assertEquals(expectedHex, rawTxResponseVerbose.getHex());
        assertEquals(2, rawTxResponseVerbose.getVersion());
        assertEquals("dd90b0ffb360eb8a25293d049edec5b556cdf29bcdcbc3caa4f57099b09575b9", rawTxResponseVerbose.getTxid());
        assertEquals("8eb2a20928cfcfd1da5234bb15985b3d1dcd15e0ac4842a2b23e05d21738366e", rawTxResponseVerbose.getHash());
        assertEquals("00000000000003bef4b6c86ee461ee8b7f99838fab8eb7e8621152b3fa1a635a", rawTxResponseVerbose.getBlockhash());
        assertEquals(1573931400, rawTxResponseVerbose.getTime());
        assertEquals(1608708, rawTxResponseVerbose.getLocktime());
        assertEquals(1, rawTxResponseVerbose.getVin().length);
        assertEquals(2, rawTxResponseVerbose.getVout().length);
    }

    @Test
    public void testGetTransaction3() {
        // txId of 3rd testnet faucet withdrawal
        // https://coinfaucet.eu/en/btc-testnet
        // https://live.blockcypher.com/btc-testnet/tx/5b4b5ff628af229de84d04fc7adafd2b2950e9d9b5dc50312ffadf88f6be5fec
        // https://live.blockcypher.com/btc-testnet/address/2NCbak49iCGk9kZ4Ehrwf34YtUpmp9b47Dc
        String txId = "5b4b5ff628af229de84d04fc7adafd2b2950e9d9b5dc50312ffadf88f6be5fec";
        rpcCommand.configureWithParams.apply(GET_TRANSACTION[0], new String[]{txId}, GET_TRANSACTION[1]);
        GetTransactionResponse response = (GetTransactionResponse) rpcClient.runCommand(rpcCommand);
        // log.info("testGetTransaction3 response:\n{}", response);
        RpcErrorResponse error = response.getRpcErrorResponse();
        assertEquals(-5, error.getCode());
        assertEquals("Invalid or non-wallet transaction id", error.getMessage());

        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION_VERBOSE[0], new Object[]{txId, true}, GET_RAWTRANSACTION_VERBOSE[1]);
        GetRawTransactionVerboseResponse rawTxResponseVerbose = (GetRawTransactionVerboseResponse) rpcClient.runCommand(rpcCommand);
        // log.info("testGetRawTransactionVerbose response:\n{}", rawTxResponseVerbose);
        String expectedHex = "020000000001010314fc82931028a75447ac27cbd5da2fffaf8865557fae3be057728e481bb277010000001716001457b0f92e0ee55b8fd90890a703335ee03dc676d8feffffff02b68f3a000000000017a914d44408c52495c5fe29d7d4e54fd4500bae632773878d336e4a0200000017a914d7962d872341a6d39cb5ee813497ba6077bc4d448702473044022012324f9e7b4747129499e6ef5f8c8df61cf9b74c31bf4dd5bc0101dcd43d048a022052bf38831a444c74377df3ec457e381047860154021557d36dd09f7d2a6d5be701210396843f02b519de933f9279dea876b0f41df7537a14908556e1b8d71b3ef3ad117b8e1800";
        assertEquals(expectedHex, rawTxResponseVerbose.getHex());
        assertEquals(2, rawTxResponseVerbose.getVersion());
        assertEquals("5b4b5ff628af229de84d04fc7adafd2b2950e9d9b5dc50312ffadf88f6be5fec", rawTxResponseVerbose.getTxid());
        assertEquals("b322e49c98ad58db1343fadf7355e9133596cca4613034379bfddde8244d3f33", rawTxResponseVerbose.getHash());
        assertEquals("000000000026104d05ae4c0448bf9e6d6e7abc8349eea3c997e593f3fab4f2c0", rawTxResponseVerbose.getBlockhash());
        assertEquals(1574384996, rawTxResponseVerbose.getTime());
        assertEquals(1609339, rawTxResponseVerbose.getLocktime());
        assertEquals(1, rawTxResponseVerbose.getVin().length);
        assertEquals(2, rawTxResponseVerbose.getVout().length);
    }

    @Test
    public void testGetRawTransaction() {
        // txId of 3rd testnet faucet withdrawal
        // https://coinfaucet.eu/en/btc-testnet
        // https://live.blockcypher.com/btc-testnet/tx/5b4b5ff628af229de84d04fc7adafd2b2950e9d9b5dc50312ffadf88f6be5fec
        // https://live.blockcypher.com/btc-testnet/address/2NCbak49iCGk9kZ4Ehrwf34YtUpmp9b47Dc
        String txId = "5b4b5ff628af229de84d04fc7adafd2b2950e9d9b5dc50312ffadf88f6be5fec";
        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION[0], new String[]{txId}, GET_RAWTRANSACTION[1]);
        GetRawTransactionResponse response = (GetRawTransactionResponse) rpcClient.runCommand(rpcCommand);
        // log.info("testGetRawTransaction response:\n{}", response);
        String expectedData = "020000000001010314fc82931028a75447ac27cbd5da2fffaf8865557fae3be057728e481bb277010000001716001457b0f92e0ee55b8fd90890a703335ee03dc676d8feffffff02b68f3a000000000017a914d44408c52495c5fe29d7d4e54fd4500bae632773878d336e4a0200000017a914d7962d872341a6d39cb5ee813497ba6077bc4d448702473044022012324f9e7b4747129499e6ef5f8c8df61cf9b74c31bf4dd5bc0101dcd43d048a022052bf38831a444c74377df3ec457e381047860154021557d36dd09f7d2a6d5be701210396843f02b519de933f9279dea876b0f41df7537a14908556e1b8d71b3ef3ad117b8e1800";
        assertEquals(expectedData, response.getData());
    }

    @Test
    public void testGetRawTransactionVerbose() {
        // txId of 3rd testnet faucet withdrawal
        // https://coinfaucet.eu/en/btc-testnet
        // https://live.blockcypher.com/btc-testnet/tx/5b4b5ff628af229de84d04fc7adafd2b2950e9d9b5dc50312ffadf88f6be5fec
        // https://live.blockcypher.com/btc-testnet/address/2NCbak49iCGk9kZ4Ehrwf34YtUpmp9b47Dc
        String txId = "5b4b5ff628af229de84d04fc7adafd2b2950e9d9b5dc50312ffadf88f6be5fec";
        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION_VERBOSE[0], new Object[]{txId, true}, GET_RAWTRANSACTION_VERBOSE[1]);
        GetRawTransactionVerboseResponse response = (GetRawTransactionVerboseResponse) rpcClient.runCommand(rpcCommand);
        // log.info("testGetRawTransactionVerbose response:\n{}", response);
        String expectedHex = "020000000001010314fc82931028a75447ac27cbd5da2fffaf8865557fae3be057728e481bb277010000001716001457b0f92e0ee55b8fd90890a703335ee03dc676d8feffffff02b68f3a000000000017a914d44408c52495c5fe29d7d4e54fd4500bae632773878d336e4a0200000017a914d7962d872341a6d39cb5ee813497ba6077bc4d448702473044022012324f9e7b4747129499e6ef5f8c8df61cf9b74c31bf4dd5bc0101dcd43d048a022052bf38831a444c74377df3ec457e381047860154021557d36dd09f7d2a6d5be701210396843f02b519de933f9279dea876b0f41df7537a14908556e1b8d71b3ef3ad117b8e1800";
        assertEquals(expectedHex, response.getHex());
        assertEquals("5b4b5ff628af229de84d04fc7adafd2b2950e9d9b5dc50312ffadf88f6be5fec", response.getTxid());
        assertEquals("b322e49c98ad58db1343fadf7355e9133596cca4613034379bfddde8244d3f33", response.getHash());
    }

    @Ignore
    @Test
    public void testDumpPrivKey() {
        testDumpPrivKeyBeforeEnteringPassphrase();
        // testDumpPrivKeyBeforeAfterPassphrase();
    }


    private void testDumpPrivKeyBeforeEnteringPassphrase() {
        String address = "n3bYWKwSwKFwB59pLJW1L1VhDkMxVwCzZt";
        rpcCommand.configureWithParams.apply(DUMPPRIVKEY[0], new Object[]{address}, DUMPPRIVKEY[1]);
        DumpPrivKeyResponse response = (DumpPrivKeyResponse) rpcClient.runCommand(rpcCommand);
        // log.info("testDumpPrivKey response:\n{}", response);
        RpcErrorResponse error = response.getRpcErrorResponse();
        log.info("      error:  {}", error);
        assertTrue(isValidErrorCode.apply(error.getCode()));
        assertEquals(-13, error.getCode());
    }

    private void testDumpPrivKeyBeforeAfterPassphrase() {
        String address = "n3bYWKwSwKFwB59pLJW1L1VhDkMxVwCzZt";
        rpcCommand.configureWithParams.apply(DUMPPRIVKEY[0], new Object[]{address}, DUMPPRIVKEY[1]);
        DumpPrivKeyResponse response = (DumpPrivKeyResponse) rpcClient.runCommand(rpcCommand);
        log.info("testDumpPrivKey response:\n{}", response);
        assertEquals("cUF3JTYBxfGpmFBurEYYQaMYkqyE29MFyeec7EkG9A3Ydyd3NYv4", response.getKey());

    }

}
