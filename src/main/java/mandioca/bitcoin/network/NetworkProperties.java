package mandioca.bitcoin.network;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Supplier;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Runtime.getRuntime;
import static mandioca.bitcoin.network.NetworkType.valueOf;

public class NetworkProperties {

    private static final Properties networkProperties = readNetworkProperties();

    // Property Names

    static final String PROPERTY_DATA_DIR = "node.data.dir";

    static final String PROPERTY_MAINNET_PORT = "mainnet.port";
    static final String PROPERTY_REGTEST_PORT = "regtest.port";
    static final String PROPERTY_TESTNET3_PORT = "testnet3.port";
    static final String PROPERTY_NETWORK = "network";
    static final String PROPERTY_NETWORK_PROTOCOL_VERSION = "bitcoind.protocolversion";
    static final String PROPERTY_DEFAULT_USERAGENT = "default.useragent";

    static final String PROPERTY_MAIN_REACTOR_DEFAULT_PORT = "node.mainreactor.default.port";
    static final String PROPERTY_MAIN_REACTOR_SELECT_TIMEOUT = "node.mainreactor.select.timeout";
    static final String PROPERTY_SUB_REACTOR_SELECT_TIMEOUT = "node.subreactor.select.timeout";

    static final String PROPERTY_CLIENT_SOCKET_TIMEOUT = "node.client.socket.timeout";
    static final String PROPERTY_CLIENT_SOCKET_ZEROREAD_LIMIT = "node.client.socket.zeroread.limit";

    static final String PROPERTY_HANDSHAKE_TIME_TO_LIVE = "node.handshake.ttl";
    static final String PROPERTY_HANDSHAKE_COMPLETION_WAIT = "node.handshake.completion.wait";

    static final String PROPERTY_NODE_STREAMING_MAX_CHUNKSIZE = "node.streaming.max.chunksize";

    static final String PROPERTY_MAINNET_GENESIS_HASH = "mainnet.genesis.hash";
    static final String PROPERTY_TESTNET3_GENESIS_HASH = "testnet3.genesis.hash";
    static final String PROPERTY_REGTEST_GENESIS_HASH = "regtest.genesis.hash";
    static final String PROPERTY_REGTEST_GENESIS_LOWEST_BITS = "regtest.genesis.lowest.bits";


    // Property Values

    public static final String DATA_DIR = networkProperties.getProperty(PROPERTY_DATA_DIR);

    public static final int MAINNET_PORT = parseInt(networkProperties.getProperty(PROPERTY_MAINNET_PORT));
    public static final int REGTEST_PORT = parseInt(networkProperties.getProperty(PROPERTY_REGTEST_PORT));
    public static final int TESTNET3_PORT = parseInt(networkProperties.getProperty(PROPERTY_TESTNET3_PORT));
    public static final NetworkType NETWORK = valueOf(networkProperties.getProperty(PROPERTY_NETWORK).toUpperCase());
    public static final Supplier<Integer> BITCOIND_PORT = () -> {
        switch (NETWORK) {
            case MAINNET:
                return MAINNET_PORT;
            case REGTEST:
                return REGTEST_PORT;
            case TESTNET3:
                return TESTNET3_PORT;
            default:
                throw new IllegalStateException("cannot determine bitcoind port from network type " + NETWORK.name());
        }
    };
    public static final int NETWORK_PROTOCOL_VERSION = parseInt(networkProperties.getProperty(PROPERTY_NETWORK_PROTOCOL_VERSION));
    public static final String DEFAULT_USERAGENT = networkProperties.getProperty(PROPERTY_DEFAULT_USERAGENT);

    public static final String LOCALHOST = "localhost";
    public static final int NUM_AVAILABLE_PROCESSORS = getRuntime().availableProcessors();
    public static final int POOL_SIZE = NUM_AVAILABLE_PROCESSORS;

    public static final int MAIN_REACTOR_DEFAULT_PORT = parseInt(networkProperties.getProperty(PROPERTY_MAIN_REACTOR_DEFAULT_PORT));
    public static final long MAIN_REACTOR_SELECT_TIMEOUT = parseLong(networkProperties.getProperty(PROPERTY_MAIN_REACTOR_SELECT_TIMEOUT));
    public static final long SUB_REACTOR_SELECT_TIMEOUT = parseLong(networkProperties.getProperty(PROPERTY_SUB_REACTOR_SELECT_TIMEOUT));

    public static final int CLIENT_SOCKET_TIMEOUT = parseInt(networkProperties.getProperty(PROPERTY_CLIENT_SOCKET_TIMEOUT));
    public static final int CLIENT_SOCKET_ZEROREAD_LIMIT = parseInt(networkProperties.getProperty(PROPERTY_CLIENT_SOCKET_ZEROREAD_LIMIT));

    public static final long HANDSHAKE_TIME_TO_LIVE = 1000 * 60 * parseInt(networkProperties.getProperty(PROPERTY_HANDSHAKE_TIME_TO_LIVE));
    public static final long HANDSHAKE_COMPLETION_WAIT = parseLong(networkProperties.getProperty(PROPERTY_HANDSHAKE_COMPLETION_WAIT));

    public static final int NODE_STREAMING_MAX_CHUNKSIZE = parseInt(networkProperties.getProperty(PROPERTY_NODE_STREAMING_MAX_CHUNKSIZE));


    public static final String MAINNET_GENESIS_HASH = networkProperties.getProperty(PROPERTY_MAINNET_GENESIS_HASH);
    public static final String TESTNET3_GENESIS_HASH = networkProperties.getProperty(PROPERTY_TESTNET3_GENESIS_HASH);
    public static final String REGTEST_GENESIS_HASH = networkProperties.getProperty(PROPERTY_REGTEST_GENESIS_HASH);
    public static final Supplier<String> GENESIS_HASH = () -> {
        switch (NETWORK) {
            case MAINNET:
                return MAINNET_GENESIS_HASH;
            case REGTEST:
                return REGTEST_GENESIS_HASH;
            case TESTNET3:
                return TESTNET3_GENESIS_HASH;
            default:
                throw new IllegalStateException("cannot determine genesis hash from network type " + NETWORK.name());
        }
    };
    public static final String REGTEST_GENESIS_LOWEST_BITS = networkProperties.getProperty(PROPERTY_REGTEST_GENESIS_LOWEST_BITS);

    private static Properties readNetworkProperties() {
        String filename = "network.properties";
        try {
            Properties props = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream stream = loader.getResourceAsStream(filename);
            if (stream == null) {
                throw new RuntimeException("Could not read network.properties file " + filename + "as stream");
            }
            props.load(stream);
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Could not load network.properties file " + filename, e);
        }
    }
}
