package mandioca.bitcoin.network.node;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;

final class NodeTestPortFactory {

    private final int startPort;
    private final int numPorts;
    private final int skip;
    private final List<Integer> ports;

    public NodeTestPortFactory(int startPort, int numPorts, int skip) {
        this.startPort = startPort;
        this.numPorts = numPorts;
        this.skip = skip;
        this.ports = portList.get();
    }

    public final Supplier<Integer> nextPort = () -> {
        if (getPorts().isEmpty()) {
            throw new RuntimeException("no more test ports");
        }
        return getPorts().remove(0);
    };

    private final Supplier<List<Integer>> portList = () -> {
        Stream<Integer> stream = Stream.iterate(getStartPort(), i -> i + getSkip());
        return stream.limit(getNumPorts()).collect(Collectors.toList());
    };

    private int getStartPort() {
        return this.startPort;
    }

    private int getNumPorts() {
        return this.numPorts;
    }

    private int getSkip() {
        return this.skip;
    }

    private List<Integer> getPorts() {
        return ports;
    }

    public static void main(String[] args) {
        int startPort = 50_000;
        int numPorts = 10;
        int skip = 100;
        NodeTestPortFactory factory = new NodeTestPortFactory(startPort, numPorts, skip);
        List<Integer> ports = factory.portList.get();
        for (int i = 0; i < numPorts; i++) {
            int port = factory.nextPort.get();
            out.println("\ttook port " + port);
        }
        assert ports.size() == 0 : "did not remove all ports";

        boolean threwException = false;
        try {
            factory.nextPort.get();
        } catch (Exception e) {
            assert "no more test ports".equals(e.getMessage());
            threwException = true;
        }
        assert true == threwException : "exhausted port supply but didn't throw expected exception";
    }

}
