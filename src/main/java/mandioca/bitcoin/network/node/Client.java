package mandioca.bitcoin.network.node;

import java.io.IOException;

public interface Client {

    void send() throws IOException;

    void read() throws IOException;

    void returnByteBuffer();
}
