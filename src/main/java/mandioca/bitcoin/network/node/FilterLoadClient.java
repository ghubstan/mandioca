package mandioca.bitcoin.network.node;

import mandioca.bitcoin.network.block.BloomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static mandioca.bitcoin.network.message.MessageType.FILTERLOAD;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class FilterLoadClient extends CallableClient<Boolean> implements Client {

    private static final Logger log = LoggerFactory.getLogger(FilterLoadClient.class);

    private final BloomFilter bloomFilter;  // max filter size:   9 + 1-36,000 bytes

    public FilterLoadClient(String nodeName, SocketChannel socketChannel, BloomFilter bloomFilter) throws IOException {
        super(nodeName, (InetSocketAddress) socketChannel.getRemoteAddress(), 36010); // TODO constant?
        if (!socketChannel.isConnected()) {
            throw new IllegalStateException("socket channel not connected");
        }
        this.socketChannel = socketChannel;
        this.bloomFilter = bloomFilter;
    }

    @Override
    public Boolean call() {
        try {
            if (socketChannel == null || !socketChannel.isConnected()) {
                throw new IllegalStateException("socket channel not configured and connected");
            }
            return sendFilterLoad(bloomFilter.filterLoad());
        } finally {
            returnByteBuffer();  // don't close channel, but return buffer to pool
        }
    }

    private boolean sendFilterLoad(byte[] filterLoad) {
        // TODO NetworkEnvelopeHelper func?  (takes filterLoad arg)
        NetworkEnvelope envelope = new NetworkEnvelope(FILTERLOAD.command(), filterLoad);
        byte[] payload = envelope.serialize();
        try {
            if (log.isDebugEnabled()) {
                // TODO
            }
            log.info("{} sending filterload {}", nodeName, HEX.encode(filterLoad));
            setPayload(payload);
            send();
            MILLISECONDS.sleep(20L); // *must* wait more than 10ms before trying to read response
        } catch (InterruptedException ignored) {
        } catch (IOException e) {
            throw new RuntimeException("error sending filterload msg ", e);
        }
        return true;
    }
}
