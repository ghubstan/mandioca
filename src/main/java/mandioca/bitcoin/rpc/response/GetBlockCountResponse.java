package mandioca.bitcoin.rpc.response;

/**
 * Returns the number of blocks in the longest blockchain.
 */
public class GetBlockCountResponse extends BitcoindRpcResponse {

    private final long count;

    public GetBlockCountResponse(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "GetBlockCountResponse{" + "count=" + count + '}';
    }
}

/*
    $ tn-cli help getblockcount
    getblockcount

    Returns the number of blocks in the longest blockchain.

    Result:
    n    (numeric) The current block count

    Examples:
    > bitcoin-cli getblockcount
    > curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getblockcount", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/

 */
