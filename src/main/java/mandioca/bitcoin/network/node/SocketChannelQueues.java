package mandioca.bitcoin.network.node;

import mandioca.bitcoin.network.NetworkProperties;
import mandioca.ioc.annotation.Singleton;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

final class SocketChannelQueues {

    final List<Queue<SocketChannel>> socketChannelQueues = new ArrayList<>(NetworkProperties.POOL_SIZE);

    @Singleton
    public SocketChannelQueues() {
    }

    synchronized void add(Queue<SocketChannel> queue) {
        this.socketChannelQueues.add(queue);
    }

    synchronized Queue<SocketChannel> get(int index) {
        return this.socketChannelQueues.get(index);
    }
}
