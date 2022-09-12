package mandioca.bitcoin.rpc.error;

public class UnsupportedRpcErrorCodeException extends IllegalArgumentException {
    public UnsupportedRpcErrorCodeException(String s) {
        super(s);
    }
}
