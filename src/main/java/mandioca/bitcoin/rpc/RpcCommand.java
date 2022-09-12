package mandioca.bitcoin.rpc;

import mandioca.bitcoin.function.ThrowingBiFunction;
import mandioca.bitcoin.function.ThrowingTriFunction;
import mandioca.bitcoin.rpc.response.*;

import java.util.Optional;
import java.util.function.Supplier;

//  Using Optional:  http://blog.codefx.org/techniques/intention-revealing-code-java-8-optional
//  https://www.baeldung.com/java-optional-throw-exception

public final class RpcCommand {

    // Basic info need for rpc requests:  method name and response type.  (Static fields are not serialized or deserialized.)
    public static final String[] DUMPPRIVKEY = new String[]{"" + "dumpprivkey", DumpPrivKeyResponse.class.getName()};
    public static final String[] GET_BLOCKCHAIN_INFO = new String[]{"getblockchaininfo", GetBlockChainInfoResponse.class.getName()};
    public static final String[] GET_BLOCKCOUNT = new String[]{"getblockcount", GetBlockCountResponse.class.getName()};
    public static final String[] GET_BLOCKHASH = new String[]{"getblockhash", GetBlockHashResponse.class.getName()};
    public static final String[] GET_RAWTRANSACTION = new String[]{"getrawtransaction", GetRawTransactionResponse.class.getName()};
    public static final String[] GET_RAWTRANSACTION_VERBOSE = new String[]{"getrawtransaction", GetRawTransactionVerboseResponse.class.getName()};
    public static final String[] GET_TRANSACTION = new String[]{"gettransaction", GetTransactionResponse.class.getName()};
    public static final String[] GET_WALLET_INFO = new String[]{"getwalletinfo", GetWalletInfoResponse.class.getName()};


    private final String schema = "2.0";
    private final String id = "0";
    private String method;
    private Object[] params;


    // transient fields & funcs are not serialized or deserialized
    private transient String error;
    public transient final Supplier<Boolean> causedError = () -> this.error != null;
    private transient Class<?> responseClass;
    public transient final ThrowingBiFunction<String, String, RpcCommand> configure = (m, c) ->
            this.reset().setMethod(m).setResponseClass(Class.forName(c));
    public transient final ThrowingTriFunction<String, Object[], String, RpcCommand> configureWithParams = (m, p, c) ->
            this.reset().setMethod(m).setParams(p).setResponseClass(Class.forName(c));

    public RpcCommand() {
    }

    public RpcCommand(String method) {
        this.method = method;
    }

    public Optional<String> getMethod() {
        return Optional.of(method);
    }

    public RpcCommand setMethod(String method) {
        this.method = method;
        return this; // for chaining
    }

    public RpcCommand setParams(Object[] params) {
        this.params = params;
        return this; // for chaining
    }

    public Optional<Class<?>> getResponseClass() {
        return Optional.ofNullable(responseClass);
    }

    public RpcCommand setResponseClass(Class<?> responseClass) {
        this.responseClass = responseClass;
        return this; // for chaining
    }

    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }

    @SuppressWarnings("UnusedReturnValue")
    public RpcCommand setError(String error) {
        this.error = error;
        return this; // for chaining
    }

    public RpcCommand reset() {
        method = null;
        params = null;
        responseClass = null;
        error = null;
        return this; // for chaining
    }
}
