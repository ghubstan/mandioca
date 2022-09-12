package mandioca.bitcoin.network.block;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Supplier;

import static mandioca.bitcoin.function.BigIntegerFunctions.HEX_RADIX;
import static mandioca.bitcoin.function.ByteArrayFunctions.intToBytes;
import static mandioca.bitcoin.network.NetworkConstants.ZERO_HASH;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class GenesisBlockHeader {

    public static final String MAINNET_HASH_HEX = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";
    private static final int mainnetVersionInt = 1;
    private static final String mainnetMerkleRootHex = "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b";
    private static final int mainnetTimestampInt = 1231006505;
    private static final String mainnetBitsHex = "1d00ffff";
    private static final int mainnetNonceInt = 2083236893;

    public static final BlockHeader MAINNET_GENESIS_BLOCK_HEADER = new BlockHeader(
            mainnetVersionInt,
            ZERO_HASH,
            HEX.decode(mainnetMerkleRootHex),
            mainnetTimestampInt,
            HEX.decode(mainnetBitsHex),
            intToBytes.apply(mainnetNonceInt),
            new byte[]{});


    public static final String TESTNET_HASH_HEX = "000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943";
    private static final int testnetVersionInt = 1;
    private static final String testnetMerkleRootHex = "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b";
    private static final int testnetTimestampInt = 1296688602;
    private static final String testnetBitsHex = "1d00ffff";
    private static final int testnetNonceInt = 414098458;

    public static final BlockHeader TESTNET_GENESIS_BLOCK_HEADER = new BlockHeader(
            testnetVersionInt,
            ZERO_HASH,
            HEX.decode(mainnetMerkleRootHex),
            testnetTimestampInt,
            HEX.decode(mainnetBitsHex),
            intToBytes.apply(testnetNonceInt),
            new byte[]{});


    public static final String REGTEST_HASH_HEX = "0f9188f13cb7b2c71f2a335e3a4fc328bf5beb436012afca590b1a11466e2206";
    private static final int regtestVersionInt = 1;
    private static final String regtestMerkleRootHex = "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b";
    private static final int regtestTimestampInt = 1296688602;
    private static final String regtestBitsHex = "207fffff";        // bits for unit testing
    private static final int regtestNonceInt = 2;
    // regtest blocks have difficulties far below 1
    public static final BigDecimal REGTEST_GENESIS_BLK_DIFFICULTY = new BigDecimal("4.656542373906925e-10");

    public static final BlockHeader REGTEST_GENESIS_BLOCK_HEADER = new BlockHeader(
            regtestVersionInt,
            ZERO_HASH,
            HEX.decode(mainnetMerkleRootHex),
            regtestTimestampInt,
            HEX.decode(mainnetBitsHex),
            intToBytes.apply(regtestNonceInt),
            new byte[]{});

    public static final Supplier<BlockHeader> genesisBlockHeader = () -> {
        switch (NETWORK) {
            case MAINNET:
                return MAINNET_GENESIS_BLOCK_HEADER;
            case TESTNET3:
                return TESTNET_GENESIS_BLOCK_HEADER;
            case REGTEST:
                return REGTEST_GENESIS_BLOCK_HEADER;
            default:
                throw new IllegalStateException("cannot supply genesis block header for network " + NETWORK.name());
        }
    };


    // proof of work limit has to start with 00, as otherwise the value will be interpreted as negative
    private static final BigInteger MAINNET_PROOF_OF_WORK_LIMIT = new BigInteger("00000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffff", HEX_RADIX);
    private static final BigInteger TESTNET3_PROOF_OF_WORK_LIMIT = new BigInteger("0000000fffffffffffffffffffffffffffffffffffffffffffffffffffffffff", HEX_RADIX);
    private static final BigInteger REGTEST_PROOF_OF_WORK_LIMIT = new BigInteger("00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", HEX_RADIX);

    public static final Supplier<BigInteger> proofOfWorkLimit = () -> {
        switch (NETWORK) {
            case MAINNET:
                return MAINNET_PROOF_OF_WORK_LIMIT;
            case TESTNET3:
                return TESTNET3_PROOF_OF_WORK_LIMIT;
            case REGTEST:
                return REGTEST_PROOF_OF_WORK_LIMIT;
            default:
                throw new IllegalStateException("cannot supply proof of work limit for network " + NETWORK.name());
        }
    };
}
