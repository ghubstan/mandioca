package mandioca.bitcoin.network.block;

public class InvalidBlockException extends Exception {

    public InvalidBlockException(String message) {
        super(message);
    }

    public InvalidBlockException(String message, Throwable cause) {
        super(message, cause);
    }
}
