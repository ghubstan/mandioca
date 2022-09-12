package mandioca.bitcoin.transaction;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.rpc.RpcClient;
import mandioca.bitcoin.rpc.RpcCommand;
import mandioca.bitcoin.rpc.response.GetRawTransactionResponse;
import mandioca.bitcoin.rpc.response.GetRawTransactionVerboseResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.rpc.RpcCommand.GET_RAWTRANSACTION;
import static mandioca.bitcoin.rpc.RpcCommand.GET_RAWTRANSACTION_VERBOSE;
import static mandioca.bitcoin.script.processing.OpCodeFunction.disableStackDebug;
import static mandioca.bitcoin.script.processing.OpCodeFunction.enableStackDebug;
import static mandioca.bitcoin.transaction.TxFetcher.fetchRawTx;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParseAndVerifyTestnetSegwitTransactionTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(ParseAndVerifyTestnetSegwitTransactionTest.class);

    private final RpcClient rpcClient = new RpcClient(true);
    private final RpcCommand rpcCommand = new RpcCommand();

    @Before
    public void setup() {
        enableStackDebug();
    }


    @Ignore
    @Test
    public void testVerifyRandom() {
        // For testing and fixing tx.parse because I don't work with all segwit script protocols yet (and fix other broken stuff too).
        String txId = "7e683b34c422b799492ffd39a854017c9d3c0c2ce844777f519e033ee4fb92b0";
        Tx tx = fetchRawTx(txId, true, TESTNET3);
        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION_VERBOSE[0], new Object[]{txId, true}, GET_RAWTRANSACTION_VERBOSE[1]);
        GetRawTransactionVerboseResponse verboseResponse = (GetRawTransactionVerboseResponse) rpcClient.runCommand(rpcCommand);
        log.info("GetRawTransactionVerbose response:\n{}", verboseResponse);
        assertEquals(txId, tx.id());
        assertTrue(tx.verify());
    }

    @Test
    public void testVerifySegwitP2sh_P2wsh_1fb870ee3a0d250df1385d3ea17223989d1fb7277ceb68ac6880366eb063f6f7() {
        String txId = "1fb870ee3a0d250df1385d3ea17223989d1fb7277ceb68ac6880366eb063f6f7";
        Tx tx = fetchRawTx(txId, true, TESTNET3);
        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION_VERBOSE[0], new Object[]{txId, true}, GET_RAWTRANSACTION_VERBOSE[1]);
        GetRawTransactionVerboseResponse verboseResponse = (GetRawTransactionVerboseResponse) rpcClient.runCommand(rpcCommand);
        log.info("GetRawTransactionVerbose response:\n{}", verboseResponse);
        final String rawHex = "020000000001011991ff3cc3353a9d3fb51de8b1d255f7833d1e06a8bfbc7fada6cc31c4dadc380100000017160014285152691d5d81e78c65b5f65d0d70988c326370ffffffff0240420f000000000017a914d1c1fc5a4652891e419c5a023498ac85c6604f9487d02b1c000000000017a9147c42c6c39eec91fde23ebe48f389957b2096834c8702483045022100fbb805908ab72064b5a3810829b70751ad0c428efd0bc3b3ddd78c77e2321695022007b2bda12e75d55471ead6d94f98479d1532cafdb55e8d380e22ca860fd4c1af012103aa2927336b2388d0b34f9d5d1c09d3802270e92b26b9d3f7adb00203bf53c48f00000000";
        assertEquals(rawHex, verboseResponse.getHex());
        assertEquals(txId, tx.id());
        assertTrue(tx.verify());
    }

    @Test
    public void testVerifySegwitP2sh_P2wsh_38dcdac431cca6ad7fbcbfa8061e3d83f755d2b1e81db53f9d3a35c33cff9119() {
        // https://live.blockcypher.com/btc-testnet/tx/38dcdac431cca6ad7fbcbfa8061e3d83f755d2b1e81db53f9d3a35c33cff9119
        String txId = "38dcdac431cca6ad7fbcbfa8061e3d83f755d2b1e81db53f9d3a35c33cff9119";
        Tx tx = fetchRawTx(txId, true, TESTNET3);
        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION_VERBOSE[0], new Object[]{txId, true}, GET_RAWTRANSACTION_VERBOSE[1]);
        GetRawTransactionVerboseResponse verboseResponse = (GetRawTransactionVerboseResponse) rpcClient.runCommand(rpcCommand);
        log.info("GetRawTransactionVerbose response:\n{}", verboseResponse);
        final String rawHex = "02000000000101b26fbb717b26549a6e0446ea5dd7f69b9b7054669ea38cf0760941f8f40a9bf20100000017160014ba72626c6c4b2673192749e3f7b165ce5f97f0c0ffffffff0280841e00000000001976a9143172b5654f6683c8fb146959d347ce303cae4ca788acb4702b000000000017a91488ae50201a2a698ccc975bb1b02cd68035bcf8408702483045022100c9d7d3f898c17cd29e7ac8c5c24afa801332e32f69b5a81ff5810e6baf5c478c02206531c2b48304394314a8decf21eef5cc51d2456ba69de5c926baaab4b1a9a9bd0121029f902b3a3a506108b4ce3060c5c514aeb892481b7e427720bddb39d45871254a00000000";
        assertEquals(rawHex, verboseResponse.getHex());
        assertEquals(txId, tx.id());
        assertTrue(tx.verify());
    }

    @Test
    public void testVerifySegwitP2sh_P2wsh_954f43dbb30ad8024981c07d1f5eb6c9fd461e2cf1760dd1283f052af746fc88() {
        // def test_verify_p2sh_p2wsh(self):
        //        tx = TxFetcher.fetch('954f43dbb30ad8024981c07d1f5eb6c9fd461e2cf1760dd1283f052af746fc88', testnet=True)
        //        self.assertTrue(tx.verify())
        // https://live.blockcypher.com/btc-testnet/tx/954f43dbb30ad8024981c07d1f5eb6c9fd461e2cf1760dd1283f052af746fc88
        //  tn-cli decoderawtransaction "<hex>" ( iswitness )
        final String txId = "954f43dbb30ad8024981c07d1f5eb6c9fd461e2cf1760dd1283f052af746fc88";
        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION[0], new String[]{txId}, GET_RAWTRANSACTION[1]);
        GetRawTransactionResponse response = (GetRawTransactionResponse) rpcClient.runCommand(rpcCommand);
        //log.info("GetRawTransaction response:\n{}", response);
        // GET_RAWTRANSACTION give us the rawHex, declared here:
        final String rawHex = "0100000000010115e180dc28a2327e687facc33f10f2a20da717e5548406f7ae8b4c811072f856040000002322002001d5d92effa6ffba3efa379f9830d0f75618b13393827152d26e4309000e88b1ffffffff0188b3f505000000001976a9141d7cd6c75c2e86f4cbf98eaed221b30bd9a0b92888ac02473044022038421164c6468c63dc7bf724aa9d48d8e5abe3935564d38182addf733ad4cd81022076362326b22dd7bfaf211d5b17220723659e4fe3359740ced5762d0e497b7dcc012321038262a6c6cec93c2d3ecd6c6072efea86d02ff8e3328bbd0242b20af3425990acac00000000";
        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION_VERBOSE[0], new Object[]{txId, true}, GET_RAWTRANSACTION_VERBOSE[1]);
        GetRawTransactionVerboseResponse verboseResponse = (GetRawTransactionVerboseResponse) rpcClient.runCommand(rpcCommand);
        //log.info("GetRawTransactionVerbose response:\n{}", verboseResponse);
        assertEquals(rawHex, verboseResponse.getHex());
        assertEquals(txId, verboseResponse.getTxid());
        assertEquals("0000000000004d18983eb16fb40d01feb7d0d68d8c7910ff87a28112aeca6fd3", verboseResponse.getBlockhash());
        assertEquals(1, verboseResponse.getVersion());
        assertEquals(1476449817, verboseResponse.getTime());
        assertEquals(0, verboseResponse.getLocktime());
        assertEquals(1, verboseResponse.getVin().length);
        //printInputs(verboseResponse.getVin());
        assertEquals(1, verboseResponse.getVout().length);
        //printOutputs(verboseResponse.getVout());

        byte[] rawBytes = HEX.decode(rawHex);
        // parse tx with txinwitness=[3044022038421164c6468c63dc7bf724aa9d48d8e5abe3935564d38182addf733ad4cd81022076362326b22dd7bfaf211d5b17220723659e4fe3359740ced5762d0e497b7dcc01, 21038262a6c6cec93c2d3ecd6c6072efea86d02ff8e3328bbd0242b20af3425990acac]
        Tx tx = Tx.parse(stream.apply(rawBytes), TESTNET3);
        assertEquals(txId, tx.id());
        assertEquals(1, tx.getDeserializedInputs().length);
        assertEquals(1, tx.getDeserializedOutputs().length);

        byte[] serializedTx = tx.serialize();
        String testHex = HEX.encode(serializedTx);
        assertEquals(rawHex, testHex);

        TxIn txIn = tx.getDeserializedInputs()[0];
        String txInWitness1 = HEX.encode(txIn.witness[0]);
        String txInWitness2 = HEX.encode(txIn.witness[1]);
        assertEquals("3044022038421164c6468c63dc7bf724aa9d48d8e5abe3935564d38182addf733ad4cd81022076362326b22dd7bfaf211d5b17220723659e4fe3359740ced5762d0e497b7dcc01", txInWitness1);
        assertEquals("21038262a6c6cec93c2d3ecd6c6072efea86d02ff8e3328bbd0242b20af3425990acac", txInWitness2);

        assertTrue(tx.verify());
    }


    @Test
    public void testVerifySegwitP2wsh_78457666f82c28aa37b74b506745a7c7684dc7842a52a457b09f09446721e11c() {
        // def test_verify_p2wsh(self):
        //        tx = TxFetcher.fetch('78457666f82c28aa37b74b506745a7c7684dc7842a52a457b09f09446721e11c', testnet=True)
        //        self.assertTrue(tx.verify())
        // https://live.blockcypher.com/btc-testnet/tx/78457666f82c28aa37b74b506745a7c7684dc7842a52a457b09f09446721e11c
        //  tn-cli decoderawtransaction "<hex>" ( iswitness )
        final String txId = "78457666f82c28aa37b74b506745a7c7684dc7842a52a457b09f09446721e11c";
        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION[0], new String[]{txId}, GET_RAWTRANSACTION[1]);
        GetRawTransactionResponse response = (GetRawTransactionResponse) rpcClient.runCommand(rpcCommand);
        //log.info("GetRawTransaction response:\n{}", response);
        // GET_RAWTRANSACTION give us the rawHex, declared here:
        final String rawHex = "0100000000010115e180dc28a2327e687facc33f10f2a20da717e5548406f7ae8b4c811072f8560200000000ffffffff0188b3f505000000001976a9141d7cd6c75c2e86f4cbf98eaed221b30bd9a0b92888ac02483045022100f9d3fe35f5ec8ceb07d3db95adcedac446f3b19a8f3174e7e8f904b1594d5b43022074d995d89a278bd874d45d0aea835d3936140397392698b7b5bbcdef8d08f2fd012321038262a6c6cec93c2d3ecd6c6072efea86d02ff8e3328bbd0242b20af3425990acac00000000";
        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION_VERBOSE[0], new Object[]{txId, true}, GET_RAWTRANSACTION_VERBOSE[1]);
        GetRawTransactionVerboseResponse verboseResponse = (GetRawTransactionVerboseResponse) rpcClient.runCommand(rpcCommand);
        //log.info("GetRawTransactionVerbose response:\n{}", verboseResponse);
        assertEquals(rawHex, verboseResponse.getHex());
        assertEquals(txId, verboseResponse.getTxid());
        assertEquals("0000000000004d18983eb16fb40d01feb7d0d68d8c7910ff87a28112aeca6fd3", verboseResponse.getBlockhash());
        assertEquals(1, verboseResponse.getVersion());
        assertEquals(1476449817, verboseResponse.getTime());
        assertEquals(0, verboseResponse.getLocktime());
        assertEquals(1, verboseResponse.getVin().length);
        //printInputs(verboseResponse.getVin());
        assertEquals(1, verboseResponse.getVout().length);
        //printOutputs(verboseResponse.getVout());

        byte[] rawBytes = HEX.decode(rawHex);
        // parse tx with txinwitness=[3045022100f9d3fe35f5ec8ceb07d3db95adcedac446f3b19a8f3174e7e8f904b1594d5b43022074d995d89a278bd874d45d0aea835d3936140397392698b7b5bbcdef8d08f2fd01, 21038262a6c6cec93c2d3ecd6c6072efea86d02ff8e3328bbd0242b20af3425990acac]
        Tx tx = Tx.parse(stream.apply(rawBytes), TESTNET3);
        assertEquals(txId, tx.id());
        assertEquals(1, tx.getDeserializedInputs().length);
        assertEquals(1, tx.getDeserializedOutputs().length);

        byte[] serializedTx = tx.serialize();
        String testHex = HEX.encode(serializedTx);
        assertEquals(rawHex, testHex);

        TxIn txIn = tx.getDeserializedInputs()[0];
        String txInWitness1 = HEX.encode(txIn.witness[0]);
        String txInWitness2 = HEX.encode(txIn.witness[1]);
        assertEquals("3045022100f9d3fe35f5ec8ceb07d3db95adcedac446f3b19a8f3174e7e8f904b1594d5b43022074d995d89a278bd874d45d0aea835d3936140397392698b7b5bbcdef8d08f2fd01", txInWitness1);
        assertEquals("21038262a6c6cec93c2d3ecd6c6072efea86d02ff8e3328bbd0242b20af3425990acac", txInWitness2);

        assertTrue(tx.verify());
    }

    @Test
    public void testVerifySegwitP2wpkh_d869f854e1f8788bcff294cc83b280942a8c728de71eb709a2c29d10bfe21b7c() {
        // https://live.blockcypher.com/btc-testnet/tx/d869f854e1f8788bcff294cc83b280942a8c728de71eb709a2c29d10bfe21b7c

        //  tn-cli decoderawtransaction "<hex>" ( iswitness )
        final String txId = "d869f854e1f8788bcff294cc83b280942a8c728de71eb709a2c29d10bfe21b7c";
        final String rawHex = "0100000000010115e180dc28a2327e687facc33f10f2a20da717e5548406f7ae8b4c811072f8560100000000ffffffff0100b4f505000000001976a9141d7cd6c75c2e86f4cbf98eaed221b30bd9a0b92888ac02483045022100df7b7e5cda14ddf91290e02ea10786e03eb11ee36ec02dd862fe9a326bbcb7fd02203f5b4496b667e6e281cc654a2da9e4f08660c620a1051337fa8965f727eb19190121038262a6c6cec93c2d3ecd6c6072efea86d02ff8e3328bbd0242b20af3425990ac00000000";
        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION_VERBOSE[0], new Object[]{txId, true}, GET_RAWTRANSACTION_VERBOSE[1]);
        GetRawTransactionVerboseResponse verboseResponse = (GetRawTransactionVerboseResponse) rpcClient.runCommand(rpcCommand);
        //log.info("GetRawTransactionVerbose response:\n{}", verboseResponse);
        assertEquals(rawHex, verboseResponse.getHex());
        assertEquals(txId, verboseResponse.getTxid());
        assertEquals("0000000000004d18983eb16fb40d01feb7d0d68d8c7910ff87a28112aeca6fd3", verboseResponse.getBlockhash());
        assertEquals(1, verboseResponse.getVersion());
        assertEquals(1476449817, verboseResponse.getTime());
        assertEquals(0, verboseResponse.getLocktime());
        assertEquals(1, verboseResponse.getVin().length);
        // printInputs(verboseResponse.getVin());
        assertEquals(1, verboseResponse.getVout().length);
        // printOutputs(verboseResponse.getVout());

        byte[] rawBytes = HEX.decode(rawHex);
        // parse tx with txinwitness=[3045022100df7b7e5cda14ddf91290e02ea10786e03eb11ee36ec02dd862fe9a326bbcb7fd02203f5b4496b667e6e281cc654a2da9e4f08660c620a1051337fa8965f727eb191901, 038262a6c6cec93c2d3ecd6c6072efea86d02ff8e3328bbd0242b20af3425990ac]
        Tx tx = Tx.parse(stream.apply(rawBytes), TESTNET3);
        assertEquals(txId, tx.id());
        assertEquals(1, tx.getDeserializedInputs().length);
        assertEquals(1, tx.getDeserializedOutputs().length);

        byte[] serializedTx = tx.serialize();
        String testHex = HEX.encode(serializedTx);
        assertEquals(rawHex, testHex);

        TxIn txIn = tx.getDeserializedInputs()[0];
        String txInWitness1 = HEX.encode(txIn.witness[0]);
        String txInWitness2 = HEX.encode(txIn.witness[1]);
        assertEquals("3045022100df7b7e5cda14ddf91290e02ea10786e03eb11ee36ec02dd862fe9a326bbcb7fd02203f5b4496b667e6e281cc654a2da9e4f08660c620a1051337fa8965f727eb191901", txInWitness1);
        assertEquals("038262a6c6cec93c2d3ecd6c6072efea86d02ff8e3328bbd0242b20af3425990ac", txInWitness2);

        // assert p2pkhScript =  OP_DUP OP_HASH160 1d7cd6c75c2e86f4cbf98eaed221b30bd9a0b928 OP_EQUALVERIFY OP_CHECKSIG

        assertTrue(tx.verify());
    }

    private void printInputs(Object[] inputMaps) {
        StringBuilder inputsBuilder = new StringBuilder("Inputs from rpc decoderawtransaction response:\n");
        Arrays.stream(inputMaps).forEachOrdered(inputMap -> {
            inputsBuilder.append("\t")
                    .append(inputMap)
                    .append("\n");
        });
        //log.info(inputsBuilder.toString().trim());
    }

    private void printOutputs(Object[] inputMaps) {
        StringBuilder outputsBuilder = new StringBuilder("Outputs from rpc decoderawtransaction response:\n");
        Arrays.stream(inputMaps).forEachOrdered(inputMap -> {
            outputsBuilder.append("\t")
                    .append(inputMap)
                    .append("\n");
        });
        //log.info(outputsBuilder.toString().trim());
    }

    @After
    public void teardown() {
        disableStackDebug();
    }

}
