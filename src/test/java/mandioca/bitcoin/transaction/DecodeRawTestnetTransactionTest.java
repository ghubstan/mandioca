package mandioca.bitcoin.transaction;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.rpc.RpcClient;
import mandioca.bitcoin.rpc.RpcCommand;
import mandioca.bitcoin.rpc.response.GetRawTransactionVerboseResponse;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static mandioca.bitcoin.network.NetworkType.TESTNET3;
import static mandioca.bitcoin.rpc.RpcCommand.GET_RAWTRANSACTION_VERBOSE;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

public class DecodeRawTestnetTransactionTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(DecodeRawTestnetTransactionTest.class);

    private final RpcClient rpcClient = new RpcClient(true);
    private final RpcCommand rpcCommand = new RpcCommand();

    @Test
    public void testDecode_8b57a496849803a1ecee6dccf04eb68456c65b24956e6b916ea5a7f4a4c0d5c1() {
        //  tn-cli decoderawtransaction "<hex>" ( iswitness )
        String rawHex = "0100000015d8994bbfffd444b36561b2adf286d7cbbce58458112df4465cfd4a6c700ebce7010000006b483045022100e79fc8331ef5b237ce79f4d88954bebfbe3bbe9be33bebd05816816ef234f61402205413513a2c9035d9d1cc28b4a10d1bff1972e311c949753cd72480ed0093f6f7012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff0805888255ab6f3dd7a353664ba142c31849a55aa4a108ced08d7415d036be3c010000006a47304402200bfa5b07478b715a2743ef7c8527cdc7c005832dc0763b3476c9711fedc0801e022075c33548b89d8fc9807674032effb83adda327d24f3fdff9543176a7ac038af3012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff4073e8c15b453e53ba570a82790f6ef789654d9b4fb727f3bba5d5aced65994a010000006a47304402206f090f636ed5694f864af2f597c7350c85f5ebc8c079919bfe05beee68538946022044fd6e8c7bac0ab7e281553e232798910fc6016c846ce14197db2c48c080b1c0012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff985b2326617b44972c1dcdcf5f0e8983b2518010fc4d1a15ee74108b9d73c833020000006a47304402206655a7f14a03680762345db2d3318e822b2b407795d4d24376a24e05aed11a000220321bb74c5f014dfea251c185305e6eb702d09e4ce6cc3b4247bf5fcf43e1df1b012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff95e1e823b55c6f711f04d11411787ad0cfe375a360fa778e712f5f52f7de48fa020000006b48304502210095518ef283a67da79d03f635a415dbdcc870485a4078afd96543d775157e2fcd022058628ab66cb24e4db676d4a98b4c9d09b3330a8c4c1091f4c456422282cd9bbe012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff7e9881f3af43124dbf9f78a9f72b2af1b8f997baa06b04de0e5891374212ecb8010000006b483045022100efe1a691f11f3dff1ba69769db2cf6e33566b048aff6d6d8ef8b139e4e4b8855022030296c689c4b74727777b3908ab24a9101f2a487f04eafc2fb69576c66180958012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff4aa07d2daaf82cbb415fb51c03668785550197df88e89b8ecba5b51686c41d2b010000006a47304402202d5ee24dec2e39520e3f94ce924cbacf38d9938097b5682cbe5a8864c489536a02204f665dc3f8686441284076cf26523a3cb5110e86e05299f647a831a5b1de5882012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffffb6b78fc38c2431529d06265acb71877e879075d54efbd33855da281a3d3ec1c1020000006a4730440220112307d8cac69ae1ee9daa20d8d4e1105cc2bd8a56bae965cb69cf1b43b2111f02203c57889fa3c95b6cfe462db3db40488849d669b3a5bf8fcc7f835b167d428755012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff5853e888df62e6037b727e62952dfdfe0f455df69a0df481a9e395c14bba614b020000006b48304502210097c9affd004f79abe67769b7bdd1b8b46671d5e27760b3f50081f8fd4edab49a0220139f0dafee2bd7513b70ce6419992187d8aee64fcfb4de49874e543216ddde9e012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffffd7c9a00044a99f57dfeb98102df977acf6fbe9f70346ea8dedbc351fa33896af020000006b483045022100a6ebf67b9741807fbbd4b2ae45b739b4cb983c4df5aa81d97862dbc7fed22fcb02204c3375b8530c3c99db28ddb425a6363417e53acc69a0fa280786bc2908ca8a68012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff4dbd6c3b9ba3526ba16b0a1c36a5d3cd6dcd2f69e3816dbc3dbbb4bc47ff7386020000006b483045022100cffbac4bb4321c5cd895b828abc5f67887afd20e19b9c6bb65fe584c6b9911b602205550d406beeda97c8d5bf279e4000dcb039e7e1bbad685fb7273613f73fd21f7012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff6609033fb83afb4b32d47fe9935c1ea405ac4bf593696bf6b8f657cfda01d3ee010000006a47304402205e2ce57e23114cd9b1fa4d79cbe1ff212a1e17ce27db4e1e6c8b2c1c94883b3a02207d066d62f6982d700f9e75e75c55fb49e11299a757decafd20fedf77a3e368ae012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff4c3666b5d43d6c60e6a4df2673d72c3886286236c155cc7c8176ea6a905b924e020000006b483045022100823d577016686012453b95b7188cf435d8bc26e7bac991a467ecf9579072fdeb022029b40445f482611c9b71e9cc558701bc3d9c688f9ca37d9bb0d47f552a1d2323012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff0fd444a6cf432b9561b367f348c42f7d55693e1b5d527dd4bd9fa3657f1a2244010000006b483045022100a4583bd984eac16bd2f45e2024ac5286b3fe0766019c8bf98cd7d13f04ba9339022026bfb21648ecd0ce453c39c96f539f16161098d3e2b5877f66cdbcd8d274d377012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff5cd2be0318fee1ca985aeefa550a5463ad95ca71f4046255a6b4b3bc84e382fe020000006a473044022034bc16d00406a6d8f009f49d457a1d04192f2a2d0d24aeafc8dba268145afda502207baa4b26331bba87e5691eeb4a56a8adc0b07327a4fd40e830d53eeca8cae2e0012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff5dbb7bdd3dad421fdc0812deba70357f9166ac32b557e6d806533d3cb16b742f010000006b483045022100e1b99516794368fa87422831bc0b1100a7c281e7921bf00398fdc2fba334a559022078f5d619193364635aea520991aa91c23e6b43f5630342ece9ecbc0eadc1a547012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff4c19e826062f6652ec796c09b997aa4fd8843dfe8917da325daa492def82959b010000006b483045022100fcc915e7b31bd244d5c0e4f082f51c70a47fe8d8b7cf88bb2a449cb65ab3fc1802203083970ef1e569dd594a850e9167eba7635eab121fd7b14d867aad5ba5f7d912012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff5f60a15664d88e6d4c74d31987ab64e3320b388ffe46206ef0a5ac1e7fbbf581010000006b483045022100be6b952c575cf004e2a0fc5d475e2f5e67a6c6e4695069ea06f9b8a1f56476ee02200c4345f7cc87724abadf9d32b1c46c041c69141341693aa52d89b5147aaf195f012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffffd30664f4d846917c60010a51e1af7885b59b0e581bb6f1930d1c165eeaac70be020000006a47304402205692c840318e9d530978a463cb9a3e0dcdbbd9d4cb5480b54a0673c6abee3b5d02203edba492025742113c5b7f609d0ae270b3d74ffee9f0e6ebf5842a3f39d09e46012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffffabfc60d4b080871f520aeb51272a51d12a11bf50156658ca04e5206ea55edaab020000006a47304402206f936e8a11b2438157fe007ec6529456ddeafb09067fbba0fe2cb2fb922d1d4102206a04d099a38e182d10655af018b78172c827c03c0e9258350028977c7907b209012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff8d8363d5cd66b4eee1b54529a398d5b2573c9b06e6dcf73591569fcce591a999020000006a47304402204c17d9ade5b7941f81fefa528f80be449787bd82569aa63f0873480d0d15e7ac022000819f7f38112040727e1df57bd9b01427e214cbe813d2bf133c078aa049b80d012102ed9db0fafd0781f4ef4bc43f07fdb15e1ac3013204a91698197b2cb89865c066feffffff030353c904000000001976a914ba1175214f3208a0addf57e11f50b833add6954388ac0000000000000000166a146f6d6e6900000000000000020000000005f5e10022020000000000001976a91413889d6b33f9a9e56031090796d8920aff85843388acd6961500";
        String txId = "8b57a496849803a1ecee6dccf04eb68456c65b24956e6b916ea5a7f4a4c0d5c1";

        rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION_VERBOSE[0], new Object[]{txId, true}, GET_RAWTRANSACTION_VERBOSE[1]);
        GetRawTransactionVerboseResponse response = (GetRawTransactionVerboseResponse) rpcClient.runCommand(rpcCommand);
        //log.info("GetRawTransactionVerbose response:\n{}", response);
        String expectedHex = rawHex;
        assertEquals(expectedHex, response.getHex());

        assertEquals(txId, response.getTxid());
        assertEquals("00000000d191438197fb8b5891e495f15d7e306391fc715eae7c41d5ab3f1c25", response.getBlockhash());
        assertEquals(1, response.getVersion());
        assertEquals(1538620654, response.getTime());
        assertEquals(1414870, response.getLocktime());

        assertEquals(21, response.getVin().length);
        printInputs(response.getVin());

        assertEquals(3, response.getVout().length);
        printOutputs(response.getVout());

        byte[] rawBytes = HEX.decode(rawHex);
        Tx tx = Tx.parse(stream.apply(rawBytes), TESTNET3);
        assertEquals(txId, tx.id());
        assertEquals(21, tx.getDeserializedInputs().length);
        assertEquals(3, tx.getDeserializedOutputs().length);
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

    // $ tn-cli help decoderawtransaction
    //decoderawtransaction "hexstring" ( iswitness )
    //
    //Return a JSON object representing the serialized, hex-encoded transaction.
    //
    //Arguments:
    //1. hexstring    (string, required) The transaction hex string
    //2. iswitness    (boolean, optional, default=depends on heuristic tests) Whether the transaction hex is a serialized witness transaction.
    //                If iswitness is not present, heuristic tests will be used in decoding.
    //                If true, only witness deserialization will be tried.
    //                If false, only non-witness deserialization will be tried.
    //                This boolean should reflect whether the transaction has inputs
    //                (e.g. fully valid, or on-chain transactions), if known by the caller.
    //
    //Result:
    //{
    //  "txid" : "id",        (string) The transaction id
    //  "hash" : "id",        (string) The transaction hash (differs from txid for witness transactions)
    //  "size" : n,             (numeric) The transaction size
    //  "vsize" : n,            (numeric) The virtual transaction size (differs from size for witness transactions)
    //  "weight" : n,           (numeric) The transaction's weight (between vsize*4 - 3 and vsize*4)
    //  "version" : n,          (numeric) The version
    //  "locktime" : ttt,       (numeric) The lock time
    //  "vin" : [               (array of json objects)
    //     {
    //       "txid": "id",    (string) The transaction id
    //       "vout": n,         (numeric) The output number
    //       "scriptSig": {     (json object) The script
    //         "asm": "asm",  (string) asm
    //         "hex": "hex"   (string) hex
    //       },
    //       "txinwitness": ["hex", ...] (array of string) hex-encoded witness data (if any)
    //       "sequence": n     (numeric) The script sequence number
    //     }
    //     ,...
    //  ],
    //  "vout" : [             (array of json objects)
    //     {
    //       "value" : x.xxx,            (numeric) The value in BTC
    //       "n" : n,                    (numeric) index
    //       "scriptPubKey" : {          (json object)
    //         "asm" : "asm",          (string) the asm
    //         "hex" : "hex",          (string) the hex
    //         "reqSigs" : n,            (numeric) The required sigs
    //         "type" : "pubkeyhash",  (string) The type, eg 'pubkeyhash'
    //         "addresses" : [           (json array of string)
    //           "12tvKAXCxZjSmdNbao16dKXC8tRWfcF5oc"   (string) bitcoin address
    //           ,...
    //         ]
    //       }
    //     }
    //     ,...
    //  ],
    //}
    //
    //Examples:
    //> bitcoin-cli decoderawtransaction "hexstring"
    //> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "decoderawtransaction", "params": ["hexstring"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
}
