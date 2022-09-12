package mandioca.bitcoin.transaction;

import mandioca.bitcoin.network.NetworkType;
import mandioca.bitcoin.rpc.RpcClient;
import mandioca.bitcoin.rpc.RpcCommand;
import mandioca.bitcoin.rpc.response.BitcoindRpcResponse;
import mandioca.bitcoin.rpc.response.GetRawTransactionResponse;
import mandioca.bitcoin.rpc.response.GetRawTransactionVerboseResponse;
import mandioca.bitcoin.rpc.response.GetTransactionResponse;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static mandioca.bitcoin.function.ByteArrayFunctions.hexToByteArrayInputStream;
import static mandioca.bitcoin.function.ByteArrayFunctions.toByteArrayInputStream;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.rpc.RpcCommand.*;
import static mandioca.bitcoin.transaction.Tx.parse;
import static mandioca.bitcoin.util.HexUtils.HEX;

// Depends on running bitcoind -deprecatedrpc=generate -testnet -daemon on localhost

public class TxFetcher {

    private static final RpcCommand rpcCommand = new RpcCommand();
    private static final RpcClient client = new RpcClient(true);

    protected static final Function<byte[], ByteArrayInputStream> stream = toByteArrayInputStream;

    private static final Map<String, Tx> txCache = new TreeMap<>();
    private static final BiFunction<String, Tx, Tx> cacheTx = txCache::put;
    private static final Function<String, Boolean> isCached = txCache::containsKey;
    private static final Function<String, Boolean> isNotCached = (k) -> !isCached.apply(k);

    private static final BiFunction<BitcoindRpcResponse, NetworkType, Tx> parseRawTx = (r, n) ->
            parse(hexToByteArrayInputStream.apply(((GetRawTransactionResponse) r).getData()), n);
    private static final BiFunction<BitcoindRpcResponse, NetworkType, Tx> parseRawVerboseTx = (r, n) ->
            parse(hexToByteArrayInputStream.apply(((GetRawTransactionVerboseResponse) r).getHex()), n);
    private static final BiFunction<BitcoindRpcResponse, NetworkType, Tx> parseTx = (r, n) ->
            parse(hexToByteArrayInputStream.apply(((GetTransactionResponse) r).getHex()), n);

    private static final BiFunction<String, Tx, Boolean> txIdsNotEqual = (txId, t) -> !Objects.equals(txId, t.id());

    public static Tx fetchRawTx(String txId, boolean fresh, NetworkType networkType) {
        if (fresh || isNotCached.apply(txId)) {
            rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION[0], new String[]{txId}, GET_RAWTRANSACTION[1]);
            GetRawTransactionResponse response = (GetRawTransactionResponse) client.runCommand(rpcCommand);
            // out.println("JSON response:  " + response);
            checkRpcError(response);
            return parseTx(txId, HEX.decode(response.getData()), response, parseRawTx, networkType);
        } else {
            return txCache.get(txId);
        }
    }

    public static Tx fetchRawVerboseTx(String txId, boolean fresh, NetworkType networkType) {
        if (fresh || isNotCached.apply(txId)) {
            rpcCommand.configureWithParams.apply(GET_RAWTRANSACTION_VERBOSE[0], new Object[]{txId, true}, GET_RAWTRANSACTION_VERBOSE[1]);
            GetRawTransactionVerboseResponse response = (GetRawTransactionVerboseResponse) client.runCommand(rpcCommand);
            // out.println("JSON response:  " + response);
            checkRpcError(response);
            return parseTx(txId, HEX.decode(response.getHex()), response, parseRawVerboseTx, networkType);
        } else {
            return txCache.get(txId);
        }
    }

    public static Tx fetch(String txId, boolean fresh, NetworkType networkType) {
        if (fresh || isNotCached.apply(txId)) {
            rpcCommand.configureWithParams.apply(GET_TRANSACTION[0], new String[]{txId}, GET_TRANSACTION[1]);
            GetTransactionResponse response = (GetTransactionResponse) client.runCommand(rpcCommand);
            // out.println("JSON response:  " + response);
            checkRpcError(response);
            return parseTx(txId, HEX.decode(response.getHex()), response, parseTx, networkType);
        } else {
            return txCache.get(txId);
        }
    }

    private static void checkRpcError(BitcoindRpcResponse response) {
        // TODO build out RcpServerError(with response), and figure out the best place to catch it
        if (response.getRpcErrorResponse() != null) {
            throw new RuntimeException("Rpc Error:  " + response.getRpcErrorResponse().getCode()
                    + ", " + response.getRpcErrorResponse().getMessage());
        }
    }

    private static Tx parseTx(
            String txId,
            byte[] rawTxBytes,
            BitcoindRpcResponse response,
            BiFunction<BitcoindRpcResponse, NetworkType, Tx> rpcResponseParseFunction,
            NetworkType networkType) {
        final Tx tx = parseRaw(rawTxBytes, txId, networkType);
        // parse json for debugging...
        // final Tx  tx = rpcResponseParseFunction.apply(response, networkType);
        cacheTx.apply(txId, tx);
        return tx;
    }

    private static Tx parseRaw(byte[] bytes, String txId, NetworkType networkType) {
        Tx tx = parse(stream.apply(bytes), networkType);
        final String computedHash;
        if (tx.isSegwit) {
            computedHash = tx.id();
        } else {
            computedHash = HEX.encode(reverse.apply(hash256.apply(bytes)));
        }
        if (!computedHash.equals(txId)) {
            throw new IllegalStateException("server lied;  txid in fetched tx '" + tx.id()
                    + "' != txid used in query '" + txId + "'");
        }
        return tx;
    }

    private static Tx parseJson(
            String txId,
            BitcoindRpcResponse response,
            BiFunction<BitcoindRpcResponse, NetworkType, Tx> rpcResponseParseFunction,
            NetworkType networkType) {
        Tx tx = rpcResponseParseFunction.apply(response, networkType);
        if (txIdsNotEqual.apply(txId, tx)) {
            throw new IllegalStateException("TxId in fetched tx '" + tx.id()
                    + "' does not match TxId used for the query '" + txId + "'");
        }
        return tx;
    }
}

























