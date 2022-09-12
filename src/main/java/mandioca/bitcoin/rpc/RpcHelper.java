package mandioca.bitcoin.rpc;

import mandioca.bitcoin.rpc.response.BitcoindRpcResponse;
import mandioca.bitcoin.rpc.response.RpcErrorResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
import static mandioca.bitcoin.function.ByteArrayFunctions.stringToBytes;
import static mandioca.bitcoin.rpc.response.JsonResponseFunctions.createResponseInstance;

// TODO support regtest
public class RpcHelper {

    // IDENTITY - field names are unchanged
    private static final GsonHelper GSON_HELPER = new GsonHelper(LOWER_CASE_WITH_UNDERSCORES, true);

    public static final Function<RpcCommand, byte[]> serializeRequestPayload = (c) -> stringToBytes.apply(GSON_HELPER.toJson(c));
    public static final BiFunction<String, Class<?>, Object> deserializeResponsePayload = GSON_HELPER::fromJson;
    public static final BiFunction<String, Class<?>, Object> deserializeErrorResponsePayload = (j, c) -> {
        RpcErrorResponse rpcErrorResponse = (RpcErrorResponse) deserializeResponsePayload.apply(j, RpcErrorResponse.class);
        BitcoindRpcResponse rpcResponse = createResponseInstance.apply(c);
        rpcResponse.setRpcErrorResponse(rpcErrorResponse);
        return rpcResponse;
    };
    private static final Properties configurationProperties = readConfigurationProperties();
    private static final BiFunction<String, Object, String> stripNamePrefix = (p, n) -> n.toString().trim().substring(p.length());
    private static final BiFunction<Object, String, Boolean> propNameHasPrefix = (n, p) -> n.toString().trim().startsWith(p);

    static Map<String, String> getConnectionParameters(boolean testnet) {
        String paramNamePrefix = testnet ? "testnet." : "mainnet.";  // TODO support regtest
        final Map<String, String> map = new HashMap<>();
        configurationProperties.forEach((k, v) -> {
            if (propNameHasPrefix.apply(k, paramNamePrefix)) {
                map.put(stripNamePrefix.apply(paramNamePrefix, k), v.toString());
            }
        });
        return map;
    }

    private static Properties readConfigurationProperties() {
        // TODO might want to get rid of rpc.properties now that we have network.properties, but analyze first
        String filename = "rpc.properties";
        try {
            Properties props = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream stream = loader.getResourceAsStream(filename);
            if (stream == null) {
                throw new RuntimeException("Could read rpc config file " + filename + "as stream");
            }
            props.load(stream);
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Could not load rpc config file " + filename, e);
        }
    }
}
