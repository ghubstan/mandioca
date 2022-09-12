package mandioca.bitcoin.rpc.response;

public class BitcoindRpcResponse {
    protected RpcErrorResponse rpcErrorResponse;

    public BitcoindRpcResponse() {
    }

    public RpcErrorResponse getRpcErrorResponse() {
        return rpcErrorResponse;
    }

    public void setRpcErrorResponse(RpcErrorResponse rpcErrorResponse) {
        this.rpcErrorResponse = rpcErrorResponse;
    }
}
