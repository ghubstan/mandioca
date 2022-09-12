package mandioca.bitcoin.network.node;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;

// SEE https://stackoverflow.com/questions/15884556/a-generic-callable

class CallableClient<T> extends AbstractClient implements Callable<T> {

    public CallableClient(InetSocketAddress peer, int byteBufferSize) {
        super(peer, byteBufferSize);
    }

    public CallableClient(String nodeName, InetSocketAddress peer, int byteBufferSize) {
        super(nodeName, peer, byteBufferSize);
    }

    @Override
    public T call() throws Exception {
        throw new RuntimeException("sublcasses must implement call()");
    }

}
