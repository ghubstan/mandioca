package mandioca;

import mandioca.bitcoin.address.AddressFactoryTest;
import mandioca.bitcoin.ecc.*;
import mandioca.bitcoin.function.EndianFunctionsTest;
import mandioca.bitcoin.network.message.EmptyPayloadChecksumTest;
import mandioca.bitcoin.network.message.GetDataMessageTest;
import mandioca.bitcoin.network.message.MerkleBlockMessageTest;
import mandioca.bitcoin.network.message.VersionMessageTest;
import mandioca.bitcoin.parser.ParserTest;
import mandioca.bitcoin.rpc.RpcClientTest;
import mandioca.bitcoin.script.CombineScriptsTest;
import mandioca.bitcoin.script.ScriptAddressEncodingTest;
import mandioca.bitcoin.script.ScriptVersion1ParseAndSerializeTest;
import mandioca.bitcoin.script.processing.*;
import mandioca.bitcoin.transaction.*;
import mandioca.bitcoin.util.*;
import mandioca.ioc.DiFrameworkServiceInjectionTest;
import mandioca.ioc.DiFrameworkSimpleFieldInjectionTest;
import mandioca.real.RealNumberPointTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AddressFactoryTest.class,

        // ecc pkg
        EllipticCurveFactoryTest.class,
        EllipticCurvePointTest.class,
        FieldElementTest.class,
        Rfc6979Test.class,
        Secp256k1DERTest.class,
        Secp256K1PointTest.class,
        Secp256k1SECTest.class,
        Secp256k1SignatureTest.class,
        Secp256k1WIFTest.class,

        // functions pkg
        EndianFunctionsTest.class,

        /*
        // network pkg
        BlockHeaderTest.class,
        BlockTest.class,
        BloomFilterTest.class,
        BloomFilterExamplesTest.class,
        MainnetGenesisBlockTest.class,
        MerkleExamplesTest.class,
        MerkleTreeTest.class,
        Murmur3Test.class,
        RegtestGenesisBlockTest.class,
        TestnetGenesisBlockTest.class,
         */

        // message pkg
        EmptyPayloadChecksumTest.class,
        GetDataMessageTest.class,
        MerkleBlockMessageTest.class,
        VersionMessageTest.class,

        /*
        // node pkg
        BitcoindGetHeadersTest.class,
        BitcoindHandshakeTest.class,
        EchoClientLongRunningTest.class,
        HandshakeResponsesSerializationTest.class,
        MultiPeerHandshakeTest.class,
        NetworkEnvelopeTest.class,
        SimpleHandshakeClientTest.class,
        SinglePeerHandshakeTest.class,
        SinglePeerPingTest.class,
        StartMultipleServersTest.class,
         */

        // parser pkg
        ParserTest.class,

        // rpc pkg
        RpcClientTest.class,

        // script.processing pkg
        ArithmeticFunctionsTest.class,
        BitwiseLogicFunctionsTest.class,
        ConstantOpFunctionsTest.class,
        CryptoOpFunctionsTest.class,
        ElementEncoderDecoderTest.class,
        FlowControlOpFunctionsTest.class,
        LocktimeOpFunctionsTest.class,
        OpCodeTest.class,
        PseudoWordOpFunctionsTest.class,
        ReservedWordOpFunctionsTest.class,
        ScriptEvaluationTest.class,
        ScriptStackTest.class,
        SpliceOpFunctionsTest.class,
        StackOpFunctionsTest.class,

        // script pkg
        CombineScriptsTest.class,
        ScriptAddressEncodingTest.class,
        ScriptVersion1ParseAndSerializeTest.class,

        // transaction pkg (todo clean this up and fix bugs)
        BookChapter7Example5And6TxParseAndSerializeTest.class,
        BookChapter7Example5CreateTxTest.class,
        BookChapter7Exercise4CreateAndSign1In1OutTx.class,
        BookChapter7Exercise5CreateAndSign2In1OutTx.class,
        BookChapter7Test.class,
        BookChapter7TransactionTest.class,
        BookChapter8Test.class,
        BookLoadSerializedTxCacheTest.class,
        CreateNewTxTest.class,
        DecodeRawTestnetTransactionTest.class,
        FixParserTest.class,
        ParseAndSerializeTransactionTest.class,
        ParseAndVerifyTestnetSegwitTransactionTest.class,
        TxVersion1FetcherTest.class,
        TxVersion2FetcherTest.class,
        UTXOUtilsTest.class,
        UTXOGroupTest.class,
        TransactionFactoryTest.class,

        // util pkg
        Base58Test.class,
        Bech32Test.class,
        Ripemd160Test.class,
        TupleTest.class,
        VarintUtilsTest.class,

        // ioc pkg
        DiFrameworkServiceInjectionTest.class,
        DiFrameworkSimpleFieldInjectionTest.class,

        // real pkg
        RealNumberPointTest.class
})
public class QuickTestSuite {
    /* placeholder, use this to contain all integration tests in one spot * */
}
