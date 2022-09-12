package mandioca.bitcoin.rpc.response;

/**
 * Returns the number of blocks in the longest blockchain.
 */
public class GetBlockHashResponse extends BitcoindRpcResponse {

    // $ tn-cli getblockhash 1665872
    //000000004f37ada7cbd0385f92fdf13e500c60e14401411570a5647630f925ac

    private final String hash;

    public GetBlockHashResponse(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "GetBlockHashResponse{" + "hash=" + hash + '}';
    }
}

/*
    $ tn-cli help getblockhash
    getblockhash height

    Returns hash of block in best-block-chain at height provided.

    Arguments:
    1. height    (numeric, required) The height index

    Result:
    "hash"         (string) The block hash

    Examples:
    > bitcoin-cli getblockhash 1000
    > curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getblockhash", "params": [1000] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
 */
