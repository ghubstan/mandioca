package mandioca.bitcoin.network.node;

public class HandshakeFailedException extends Exception {

    public HandshakeFailedException(String message) {
        super(message);
    }

    public HandshakeFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
