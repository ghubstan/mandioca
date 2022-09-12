package mandioca.bitcoin.network.node;

// See http://www.4e00.com/blog/java/2019/03/16/reactor-pattern-time-server-example.html

import mandioca.ioc.DependencyInjectionConfig;
import mandioca.ioc.DependencyInjectionFramework;
import mandioca.ioc.module.AbstractSimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static mandioca.bitcoin.network.NetworkProperties.*;
import static mandioca.bitcoin.network.node.NodeExecutorServices.createSubReactorPool;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.addressToCacheKey;

/**
 * 多Reactor多线程模型
 *
 * <pre>
 *      与单Reactor多线程模型相比，是将Reactor分成两部分:
 *      (The reactor is divided into 2 parts, unlike the single reactor patter.)
 *
 *      1.  MainReactor负责监听server socket，用来处理新连接的接收，将建立的socketChannel指定注册给SubReactor。
 *      (MainReactor is responsible for listening to the server socket, using a handler accept new connections,
 *      and register the established socketChannel to SubReactor.)
 *
 *      2.  SubReactor维护自己的selector，基于mainReactor注册的socketChannel多路分离IO读写事件，读写网络数据，
 *          将接收到的数据发给业务线程池worker处理，并在SubReactor线程池中返回业务处理的结果。
 *          (SubReactor maintains its own selector, demultiplexes IO read and write events based on the socketChannel
 *          registered by mainReactor, reads & writes network data, sends received data to the business thread
 *          pool worker for processing, and return the result of the business processing in the SubReactor thread pool.)
 *
 *      3.  业务逻辑代码通常比较耗时，不要在reactor线程处理
 *          (Business logic code is usually time consuming, do not process it in the reactor thread.)
 *  </pre>
 */
public class Node {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(Node.class);

    private final DependencyInjectionFramework diFramework;

    private final Selectors selectors = new Selectors();
    private final SocketChannelQueues socketChannelQueues = new SocketChannelQueues();
    private final HandshakeCache handshakeCache = new HandshakeCache();
    private final ExecutorService subReactorPool = createSubReactorPool();

    private Thread mainReactorThread;

    private final String hostname;
    private int port;
    private final String nodeName;

    private final List<InetSocketAddress> peers = new ArrayList<>();
    private final NodeHelper nodeHelper;
    private boolean isRunning = false;

    private final Predicate<Integer> isUsingRandomServerPort = (p) -> p == 0;

    public Node(String hostname, int port) {
        this("server-" + hostname + "-" + port, hostname, port);
    }

    public Node(String nodeName, String hostname, int port) {
        this.nodeName = nodeName;
        this.hostname = hostname;
        this.port = port;
        this.diFramework = DependencyInjectionConfig.getFramework(new NodeDependencyInjectionModule());
        this.nodeHelper = (NodeHelper) diFramework.inject(NodeHelper.class);
    }

    public void start() {
        DataDirectory.initDataDir();
        MainReactor mainReactor = (MainReactor) diFramework.inject(MainReactor.class); // accepts tcp connections
        mainReactorThread = new Thread(mainReactor);
        mainReactorThread.setName("MAIN-REACTOR");
        mainReactorThread.start();
        try {
            MILLISECONDS.sleep(40L);
            if (isUsingRandomServerPort.test(this.port)) {
                // find the system chosen port in the main reactor and overwrite this node's port before continuing
                this.port = mainReactor.getPort();
            }
        } catch (InterruptedException ignored) {
        }
        for (int i = 0; i < POOL_SIZE; i++) {  // start POOL_SIZE SubReactors to handle read / write ops
            socketChannelQueues.add(new ConcurrentLinkedQueue<>());
            SubReactor subReactor = ((SubReactor) diFramework.inject(SubReactor.class)).setIndex(i);
            subReactorPool.submit(subReactor);
        }
        subReactorPool.shutdown();
        isRunning = true;
    }

    public boolean handshake() {
        if (isRunning) {
            return nodeHelper.handshakePeers(peers);
        } else {
            log.error("{} not running; cannot perform handshakes with peers", nodeName);
            return false;
        }
    }

    public boolean handshake(InetSocketAddress peer) throws HandshakeFailedException {
        if (isRunning) {
            return nodeHelper.handshake(peer);
        } else {
            log.error("{} not running; cannot handshake peer {}", nodeName, addressToCacheKey.apply(peer));
            return false;
        }
    }

    public boolean ping(InetSocketAddress peer) {
        String peerInfo = addressToCacheKey.apply(peer);
        if (isRunning) {
            boolean pinged = nodeHelper.ping(peer);
            if (!pinged) {
                peers.remove(peer);
            }
            return pinged;
        } else {
            log.error("{} not running; cannot ping peer {}", nodeName, peerInfo);
            return false;
        }
    }

    public void shutdown() {
        if (isRunning) {
            if (log.isDebugEnabled()) {
                log.debug("shutdown -- interrupting sub & main reactor threads");
            }
            try {
                subReactorPool.shutdownNow();
                MILLISECONDS.sleep(100);
                mainReactorThread.interrupt();
                MILLISECONDS.sleep(100);
                log.info("shutdown by interruption complete");
            } catch (Exception e) {
                e.printStackTrace();
            }
            isRunning = false;
        }
    }

    public String getHostname() {
        return this.hostname;
    }

    public int getPort() {
        return this.port;
    }

    String getNodeName() {
        return this.nodeName;
    }

    List<InetSocketAddress> getPeers() {
        return peers;
    }

    void setPeers(List<InetSocketAddress> peers) {
        this.peers.addAll(peers);
    }

    int getHandshakeCacheSize() {
        return this.handshakeCache.size();
    }

    private class NodeDependencyInjectionModule extends AbstractSimpleModule {
        @Override
        public void configure() {
            createMapping("nodeName", nodeName);
            createMapping("port", port);
            createSingletonMapping(mandioca.bitcoin.network.node.Selectors.class, selectors);
            createSingletonMapping(mandioca.bitcoin.network.node.SocketChannelQueues.class, socketChannelQueues);
            createSingletonMapping(mandioca.bitcoin.network.node.HandshakeCache.class, handshakeCache);
        }
    }

    public static void main(String[] args) {
        Node server = new Node(LOCALHOST, MAIN_REACTOR_DEFAULT_PORT);
        InetSocketAddress peer = new InetSocketAddress(server.getHostname(), server.getPort());
        server.start();

        // Test Client Requests
        final int numClients = 4;
        final int numRequests = 4;
        ExecutorService testExecutorService = Executors.newFixedThreadPool(numClients);
        for (int i = 0; i < numClients; i++) {
            testExecutorService.submit(new EchoClient(peer, numRequests));
        }
        testExecutorService.shutdown();

        try {
            SECONDS.sleep(3);
            server.shutdown();
            MILLISECONDS.sleep(100);
        } catch (InterruptedException ignored) {
        }
    }

}
