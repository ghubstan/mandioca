package mandioca.bitcoin.network.node;

import mandioca.bitcoin.MandiocaTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.rangeClosed;
import static mandioca.bitcoin.network.NetworkProperties.LOCALHOST;
import static mandioca.bitcoin.network.NetworkProperties.MAIN_REACTOR_DEFAULT_PORT;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.createLocalPeerList;

@SuppressWarnings("unused")
public class NodeTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(NodeTest.class);

    protected static final int MAX_ALLOWED_NODES = 100;

    protected static final int FOURTY_EIGHT_K = 48_000;
    protected static final int THIRTY_SIX_K = 36_000;
    protected static final int TWENTY_FOUR_K = 24_000;
    protected static final int TWELVE_K = 12_000;
    protected static final int SIX_K = 6_000;
    protected static final int TWO_K = 2_000;
    protected static final int ONE_K = 1_000;
    protected static final int SIX_HUNDRED = 600;
    protected static final int THREE_HUNDRED = 300;
    protected static final int SEVENTY_FIVE = 75;
    protected static final int TWENTY_FIVE = 25;

    private static final int LETTER_A = 'A';

    protected static final NodeTestPortFactory portFactory =
            new NodeTestPortFactory(MAIN_REACTOR_DEFAULT_PORT, 100, 100);

    protected final NetworkEnvelopeHelper envelopeHelper = new NetworkEnvelopeHelper();

    protected List<Node> createServers(int n) {
        if (n > MAX_ALLOWED_NODES) {
            log.warn("{} is too many servers to run on single host; creating only one hundred nodes", n);
            n = MAX_ALLOWED_NODES;
        }
        int startPort = portFactory.nextPort.get();
        int[] ports = rangeClosed(startPort, startPort + (n - 1)).toArray();
        List<String> nodeNames = simpleNodeNames.apply(ports);
        assert nodeNames.size() == ports.length : "# of simple node names does not match # of ports";
        List<Node> servers = new ArrayList<>();
        for (int index = 0; index < ports.length; index++) {
            servers.add(new Node(nodeNames.get(index), LOCALHOST, ports[index]));
        }
        assert servers.size() == ports.length : "# of servers does not match # of ports and # node names";
        List<InetSocketAddress> peers = parsePeers(createLocalPeerList.apply(startPort, n));
        servers.forEach(s -> {
            Stream<InetSocketAddress> serverPeers = peers.stream().filter(p -> p.getPort() != s.getPort());
            s.setPeers(serverPeers.collect(Collectors.toList()));
        });
        return servers;
    }

    private final Function<Integer, List<String>> singleLetterNodeNames = (count) -> {
        final List<String> labels = new ArrayList<>();
        IntStream intStream = IntStream.rangeClosed(LETTER_A, (LETTER_A + count - 1));
        intStream.forEachOrdered(i -> labels.add("Node-" + (char) i));
        return labels;
    };
    private final BiFunction<Integer, Integer, List<String>> twoLetterNodeNames = (startLetter, count) -> {
        final List<String> labels = new ArrayList<>();
        IntStream intStream = IntStream.rangeClosed(LETTER_A, (LETTER_A + count - 1));
        intStream.forEachOrdered(i -> labels.add("Node-" + (char) startLetter.intValue() + (char) i));
        return labels;
    };
    private final Function<int[], List<String>> simpleNodeNames = (ports) -> {
        int numLetters = 26;
        final List<String> labels = new ArrayList<>(singleLetterNodeNames.apply(Math.min(ports.length, numLetters)));
        if (labels.size() == ports.length) {
            return labels;
        }
        int prefixLetter = LETTER_A;
        int remaining = ports.length - labels.size();
        while ((remaining < ports.length) && (prefixLetter < LETTER_A + numLetters)) {
            labels.addAll(twoLetterNodeNames.apply(prefixLetter++, Math.min(ports.length - labels.size(), numLetters)));
            if (labels.size() == ports.length) {
                break;
            }
        }
        return labels;
    };

    protected List<InetSocketAddress> parsePeers(String[] peers) {
        final List<InetSocketAddress> addresses = new ArrayList<>();
        stream(peers).forEachOrdered(p -> {
            String[] hostAndPort = p.split(":");
            addresses.add(new InetSocketAddress(hostAndPort[0], parseInt(hostAndPort[1])));
        });
        return addresses;
    }

    protected final void startServers(List<Node> servers) {
        if (servers.size() > MAX_ALLOWED_NODES) {
            log.warn("{} is too many servers to run on single host; starting only first hundred nodes", servers.size());
            for (int i = 0; i < MAX_ALLOWED_NODES; i++) {
                servers.get(i).start();
            }
        } else {
            //noinspection SimplifyStreamApiCallChains
            servers.stream().forEachOrdered(Node::start);
        }
    }

    protected final Node startServer(String hostname, int port) {
        return startServer("Node", hostname, port);
    }

    protected final Node startServer(String nodeName, String hostname, int port) {
        Node server = new Node(nodeName, hostname, port);
        server.start();
        return server;
    }

    protected final void stopServers(List<Node> servers) {
        //noinspection SimplifyStreamApiCallChains
        servers.stream().forEachOrdered(Node::shutdown);
    }

    protected final List<EchoClient> createSimpleClients(int port, int numClients, int numRequests) {
        InetSocketAddress peer = new InetSocketAddress(LOCALHOST, port);
        final List<EchoClient> clients = new ArrayList<>();
        for (int i = 0; i < numClients; i++) {
            clients.add(new EchoClient(peer, numRequests));
        }
        return clients;
    }

    protected final boolean resultsReady(List<Future<Boolean>> resultList) {
        for (Future<Boolean> r : resultList) {
            try {
                if (!r.get()) {
                    return false;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
