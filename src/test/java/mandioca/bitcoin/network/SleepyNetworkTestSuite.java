package mandioca.bitcoin.network;

import mandioca.SleepySuite;
import mandioca.bitcoin.network.block.*;
import mandioca.bitcoin.network.message.EmptyPayloadChecksumTest;
import mandioca.bitcoin.network.message.GetDataMessageTest;
import mandioca.bitcoin.network.message.MerkleBlockMessageTest;
import mandioca.bitcoin.network.message.VersionMessageTest;
import mandioca.bitcoin.network.node.*;
import mandioca.bitcoin.transaction.CreateNewTxUsingBloomFilter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(SleepySuite.class)
@Suite.SuiteClasses({

        // Tests that might saturate sockets, needing some rest time between each one

        // network.node pkg
        BitcoindGetHeadersTest.class,
        BitcoindHandshakeTest.class,

        // This hangs, or crashes with a BufferOverflow exception when running in a suite (TODO fix)
        // EchoClientLongRunningTest.class,

        HandshakeResponsesSerializationTest.class,
        MultiPeerHandshakeTest.class,
        NetworkEnvelopeTest.class,
        SimpleHandshakeClientTest.class,
        SinglePeerHandshakeTest.class,
        SinglePeerPingTest.class,
        StartMultipleServersTest.class,

        // network.block pkg
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

        // network.message pkg
        EmptyPayloadChecksumTest.class,
        GetDataMessageTest.class,
        MerkleBlockMessageTest.class,
        VersionMessageTest.class,

        // transaction pkg
        CreateNewTxUsingBloomFilter.class

})

@SleepySuite.SleepSec(1)
public class SleepyNetworkTestSuite {
}
