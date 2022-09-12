package mandioca.ioc.example;

import mandioca.ioc.annotation.Inject;

public class MockNode {

    @Inject
    private String nodeName;

    @Inject
    private int port;

    @Inject
    private int numRequests;

    @Inject
    private MockCache singletonCache;

    public String getNodeName() {
        return nodeName;
    }

    public int getPort() {
        return port;
    }

    public int getNumRequests() {
        return numRequests;
    }

    public MockCache getSingletonCache() {
        return singletonCache;
    }
}
