package mandioca.bitcoin.network.node;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;

// See http://www.4e00.com/blog/java/2019/03/16/reactor-pattern-time-server-example.html
// See http://www.4e00.com/blog/java/2019/03/15/doug-lea-scalable-io-in-java.html

/**
 * A single Acceptor instance is attached to a ServerSocketChannel's SelectionKey after
 * server socket channel registration.
 * <p>
 * The synchronized accept method creates and configures new client socket channels.
 */
public class Acceptor {

    private static final Logger log = LoggerFactory.getLogger(Acceptor.class);

    private final Selectors selectors;
    private final SocketChannelQueues socketChannelQueues;

    public Acceptor(Selectors selectors, SocketChannelQueues socketChannelQueues) {
        this.selectors = selectors;
        this.socketChannelQueues = socketChannelQueues;
    }


    public synchronized void accept(int socketChannelQueueIndex, SelectionKey selectionKey) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (Objects.nonNull(socketChannel)) {
                socketChannel.configureBlocking(false);
                // Use this shared queue instead of the shared sub Selector to avoid lock contention on the Selector and its keys.
                boolean offer = socketChannelQueues.get(socketChannelQueueIndex).offer(socketChannel);
                //  Causes the first selection operation that has not yet returned to return immediately.
                selectors.get(socketChannelQueueIndex).wakeup();
                if (!offer) {
                    log.warn("offer socketChannel failure : {}", socketChannel);
                } else {
                    log.debug("offer socketChannel success : {}", socketChannel);
                }
            }
        } catch (Exception e) {
            log.error("acceptor exception", e);
        }
    }
}
