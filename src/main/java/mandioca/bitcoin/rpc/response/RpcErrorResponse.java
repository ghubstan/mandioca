package mandioca.bitcoin.rpc.response;

// example response {"result":null,"error":{"code":-5,"message":"Invalid or non-wallet transaction id"},"id":"0"}
@SuppressWarnings("unused")
public class RpcErrorResponse {

    protected int code;
    protected String message;

    public RpcErrorResponse() {
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "RpcErrorResponse{" + "code=" + code + ", message='" + message + '\'' + '}';
    }
}
