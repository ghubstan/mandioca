package mandioca.bitcoin.network.node;

import mandioca.bitcoin.network.message.MessageType;
import mandioca.bitcoin.network.message.NonceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.channels.SelectionKey;
import java.util.Queue;
import java.util.function.Function;

import static mandioca.bitcoin.network.NetworkProperties.NETWORK;
import static mandioca.bitcoin.network.message.MessageType.getMessageType;
import static mandioca.bitcoin.network.node.NetworkEnvelopeHelper.unknownMessageEnvelope;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.channelInfo;

// http://www.4e00.com/blog/java/2019/03/16/reactor-pattern-time-server-example.html

/**
 * Does work of creating appropriate response for a single request,
 * and attaching the response payload to the instance's SelectionKey.
 */
public class Processor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Processor.class);

    private static final NonceFactory nonceFactory = new NonceFactory();

    // static NetworkEnvelopeParser.parse is not thread safe; each processor has it's own envelope parser
    private final NetworkEnvelopeParser parser = new NetworkEnvelopeParser();
    private final NetworkEnvelopeHelper envelopeHelper = new NetworkEnvelopeHelper();

    private final Function<byte[], ByteArrayInputStream> stream = ByteArrayInputStream::new;

    private final String nodeName;  // this node's name, not the peer's name (for logging, debugging)
    private byte[] request;
    private SelectionKey selectionKey;
    private Queue<SelectionKey> queue;

    Processor(String nodeName, byte[] request, SelectionKey selectionKey, Queue<SelectionKey> queue) {
        this.nodeName = nodeName;
        this.request = request;
        this.selectionKey = selectionKey;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("{} processing req '{}' from channel {}", nodeName, request, channelInfo.apply(selectionKey.channel()));
            }
            boolean offer = queue.offer(selectionKey);
            if (!offer) {
                log.warn("{} add queue failure for selectionKey {}", nodeName, selectionKey);
            } else {
                NetworkEnvelope requestEnvelope = parseRequest(request);
                byte[] response = createResponse(requestEnvelope);
                byte[] attachment = response;  // attachment will be response payload
                selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);  // add interest OP_WRITE
                selectionKey.attach(attachment);
                selectionKey.selector().wakeup();   // wakeup selector blocked by select()
            }
        } catch (Exception e) {
            // TODO err handling
            e.printStackTrace();
        }
    }

    private NetworkEnvelope parseRequest(byte[] request) {
        try {
            NetworkEnvelope envelope = parser.init(stream.apply(request)).parse(NETWORK);
            return envelope;
        } catch (Exception e) {
            return unknownMessageEnvelope.apply(request, NETWORK);
        }
    }

    private byte[] createResponse(NetworkEnvelope requestEnvelope) {
        MessageType messageType = getMessageType.apply(requestEnvelope.getNetworkCommand());
        if (log.isTraceEnabled()) {
            log.trace("{} rcvd '{}' msg", nodeName, messageType.command().getAscii());
        }
        switch (messageType) {
            case PING:
                return envelopeHelper.pongPayload.apply(requestEnvelope.getPayload(), NETWORK);
            case PONG:
                return null; // don't respond to pong
            case SENDHEADERS:
                return null; // don't respond to sendheaders
            case VERSION:
                return handshakePayload();
            case VERACK:
                return null; // don't respond to verack
            case ERROR:
                // send the error envelope that's been built...
            case UNKNOWN:
                // or an echo
                return requestEnvelope.serialize();
            default:
                throw new RuntimeException("don't know what to do with request " + requestEnvelope.toString());
        }
    }

    private byte[] handshakePayload() {
        // do not try to make this static, or nonce generator will fail
        HandshakeResponses hr = new HandshakeResponses(
                envelopeHelper.versionPayload.apply(nonceFactory.getNonce(), NETWORK),
                envelopeHelper.verackPayload.apply(NETWORK));
        return hr.serializeInternal();
    }
}
