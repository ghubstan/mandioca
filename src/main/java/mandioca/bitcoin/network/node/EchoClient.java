package mandioca.bitcoin.network.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

import static mandioca.bitcoin.function.ByteArrayFunctions.bytesToInt;
import static mandioca.bitcoin.function.ByteArrayFunctions.intToBytes;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;
import static mandioca.bitcoin.network.node.NetworkEnvelopeHelper.unknownPayload;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.channelInfo;

/**
 * Sends 'unknown' msg type containing an int representing the nth request sent in a caller's loop, and receives
 * an echo response.
 */
public class EchoClient extends CallableClient<Boolean> implements Client {

    private static final Logger log = LoggerFactory.getLogger(EchoClient.class);

    // Every client must have its own envelope parser to avoid concurrency hell.
    private final NetworkEnvelopeParser parser = new NetworkEnvelopeParser();
    private int numResponses;
    private int requestCount = 1;
    private final int numRequests;

    public EchoClient(InetSocketAddress peer, int numRequests) {
        super(peer, 512);
        this.numRequests = numRequests;
    }

    @Override
    public Boolean call() throws IOException, InterruptedException {
        try {
            socketChannel = configureClientChannel();
            while (requestCount <= numRequests) {
                send();
                // imaginary processing time
                //  MILLISECONDS.sleep(50L);
                // not sleeping makes this cpu bound
                read();
                requestCount++;
            }
            if (numResponses != numRequests) {
                log.error("Failed to complete {} round trips to server, only rcvd {} responses", numRequests, numResponses);
                return false;
            } else {
                return true;
            }
        } finally {
            closeChannel();
            returnByteBuffer();
        }
    }

    @Override
    public void send() throws IOException {
        byte[] payload = unknownPayload.apply(intToBytes.apply(requestCount), NETWORK);
        byteBuffer.put(payload);
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        if (log.isTraceEnabled()) {
            log.trace("sent req #{} via channel {}", requestCount, channelInfo.apply(socketChannel));
        }
    }

    @Override
    public void read() throws IOException {
        byteBuffer.clear();
        if (socketChannel.read(byteBuffer) > 0) {
            byteBuffer.flip();
            byte[] response = new byte[byteBuffer.remaining()];
            byteBuffer.get(response);

            // TODO do a handshake up front, then ping pong, instead of this unknown msg crap

            NetworkEnvelope envelope = parser.init(stream.apply(response)).parse(NETWORK);
            if (envelopeHelper.isError.test(envelope)) {
                String errorPayload = new String(envelope.getPayload());
                log.error("rcvd error msg from server, interrupting thead now");
                log.error(errorPayload);
                Thread.currentThread().interrupt();
            }

            if (log.isTraceEnabled()) {
                log.trace("read echo response #{} '{}' from channel {}",
                        requestCount, bytesToInt.apply(envelope.getPayload()), channelInfo.apply(socketChannel));
            }
            byteBuffer.clear();
            numResponses++;
        }
    }
}
