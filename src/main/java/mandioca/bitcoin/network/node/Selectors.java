package mandioca.bitcoin.network.node;

import mandioca.bitcoin.network.NetworkProperties;
import mandioca.ioc.annotation.Singleton;

import java.nio.channels.Selector;

final class Selectors {

    // Selectors tell which of a set of channels has I/O events
    final Selector[] selectors = new Selector[NetworkProperties.POOL_SIZE];

    @Singleton
    public Selectors() {
    }

    synchronized void add(int index, Selector selector) {
        selectors[index] = selector;
    }

    synchronized Selector get(int index) {
        return selectors[index];
    }
}
