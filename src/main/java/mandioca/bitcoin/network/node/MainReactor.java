package mandioca.bitcoin.network.node;

import mandioca.ioc.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static java.lang.System.currentTimeMillis;
import static mandioca.bitcoin.function.TimeFunctions.durationString;
import static mandioca.bitcoin.network.NetworkProperties.MAIN_REACTOR_SELECT_TIMEOUT;
import static mandioca.bitcoin.network.NetworkProperties.POOL_SIZE;

// See http://www.4e00.com/blog/java/2019/03/16/reactor-pattern-time-server-example.html
// See http://www.4e00.com/blog/java/2019/03/15/doug-lea-scalable-io-in-java.html

/**
 * The MainReactor accepts new client connections via the Acceptor.
 */
@SuppressWarnings("unused")
public class MainReactor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MainReactor.class);

    private static final AtomicInteger MAIN_REACTOR_LOOPS = new AtomicInteger();

    @Inject
    private String nodeName;
    @Inject
    private int port;
    @Inject
    private Selectors selectors;
    @Inject
    private SocketChannelQueues socketChannelQueues;
    @Inject
    private HandshakeCache handshakeCache;

    private final Predicate<Integer> isUsingRandomServerPort = (p) -> p == 0;
    private long startTime;

    public MainReactor() {
    }

    @Override
    public void run() {
        try {
            Selector selector = Selector.open();
            SelectionKey selectionKey = registerServerSocketChannel(selector);
            if (isUsingRandomServerPort.test(this.port)) {
                // We've chosen to allow the system to choose a server port for us;  find it in the
                // selectionKey and overwrite the injected port number with the system selected port.
                this.port = getSystemSelectedServerPort(selectionKey);
            }
            log.info("{} listening on port {}", nodeName, port);
            runMainReactorLoop(selector, selectionKey);
            log.warn("{} interrupted", nodeName);
            if (selector.isOpen()) {
                selector.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return port;   // has been overridden if system chose the server socket port during registration
    }

    private int getSystemSelectedServerPort(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        InetSocketAddress sa = (InetSocketAddress) serverSocketChannel.getLocalAddress();
        return sa.getPort();
    }

    private void runMainReactorLoop(Selector selector, SelectionKey selectionKey) throws IOException {
        int queueIndex = 0; // index that assigns socketChannel to different SubReactors
        while (!Thread.interrupted()) {
            MAIN_REACTOR_LOOPS.getAndIncrement();
            int select = selector.select(MAIN_REACTOR_SELECT_TIMEOUT); // TODO can & should catch timeout & log error?
            logProgress(select);
            if (select != 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey sk : selectionKeys) {
                    if (sk.isAcceptable()) {
                        Acceptor acceptor = (Acceptor) sk.attachment();
                        acceptor.accept(queueIndex++, selectionKey);
                        queueIndex = (queueIndex >= POOL_SIZE) ? 0 : queueIndex;
                    }
                }
                selectionKeys.clear();  // Removes all of the elements from this set of selection keys
            }
        } // end while not interrupted
    }

    private void logProgress(int select) {
        int counter = MAIN_REACTOR_LOOPS.get();
        if ((counter & 0x3F) == 0) {  // 0x3F = 63
            log.info("{} MAIN_REACTOR_LOOPS {} for main selector {} uptime {}", nodeName, counter, select, uptime());
        }
    }

    private SelectionKey registerServerSocketChannel(Selector selector) throws IOException {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            SelectionKey selectionKey = serverSocketChannel.register(selector, serverSocketChannel.validOps());
            selectionKey.attach(new Acceptor(selectors, socketChannelQueues)); // Main acceptor distributes to sub-reactors
            startTime = currentTimeMillis();
            return selectionKey;
        } catch (IOException e) {
            log.error("could not bind server socket to port {}", port);
            throw e;
        }
    }

    public String uptime() {
        return durationString.apply(currentTimeMillis() - startTime);
    }

}

