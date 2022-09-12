package mandioca.bitcoin.network.message;

import java.io.IOException;

public class ConnectionResetException extends IOException {
    public ConnectionResetException(String message, Throwable cause) {
        super(message, cause);
    }
}
