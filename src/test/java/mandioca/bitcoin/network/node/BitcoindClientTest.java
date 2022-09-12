package mandioca.bitcoin.network.node;

import mandioca.bitcoin.network.message.SendCmpctMessage;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Optional;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static mandioca.bitcoin.function.TimeFunctions.durationString;
import static mandioca.bitcoin.network.NetworkProperties.*;
import static mandioca.bitcoin.network.message.FeeFilterMessage.getFeeRate;
import static mandioca.bitcoin.network.message.MessageType.*;
import static mandioca.bitcoin.network.node.HandshakeClient.DEFAULT_BUFFER_SIZE;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.addressInfo;
import static org.junit.Assert.*;

// SEE https://bitcoindev.network/bitcoin-wire-protocol

@SuppressWarnings("SpellCheckingInspection")
class BitcoindClientTest extends NodeTest {

    private static final Logger log = LoggerFactory.getLogger(BitcoindClientTest.class);

    protected InetSocketAddress peer;       // a remote node, in most cases a local bitcoind -regtest node
    protected HandshakeClient client;       // client that initiates handshake and subsequent socket read/writes

    // caches for post handshake responses from bitcoind
    protected NetworkEnvelope sendHeaders;  // 1st envelope in the payload from bitcoind after handshake completion
    protected NetworkEnvelope sendCmpct1;   // 2nd envelope in the payload from bitcoind after handshake completion
    protected NetworkEnvelope sendCmpct2;   // 3rd envelope in the payload from bitcoind after handshake completion
    protected NetworkEnvelope ping;         // 4th envelope in the payload from bitcoind after handshake completion
    protected NetworkEnvelope feefilter;    // 5th envelope in the payload from bitcoind after handshake completion

    @Before
    public void setup() {
        peer = getLocalPeer();
    }

    protected void doHandshakeWithLocalBitcoindNode(int bufferSize) {
        this.client = new HandshakeClient("BitcoindClientTest", peer, bufferSize);
        doHandshake();
    }

    protected void doHandshakeWithLocalBitcoindNode() {
        this.client = new HandshakeClient("BitcoindClientTest", peer, DEFAULT_BUFFER_SIZE);
        doHandshake();
    }

    private void doHandshake() {
        try {
            long t0 = currentTimeMillis();
            boolean handshakeResult = client.call();
            assertTrue(handshakeResult);
            long executionTime = currentTimeMillis() - t0;
            log.info("completed handshake with local {} bitcoind node in {}",
                    NETWORK.name().toLowerCase(), durationString.apply(executionTime));

            t0 = currentTimeMillis();
            Optional<NetworkEnvelope[]> envelopes = client.waitForEnvelopes(SENDHEADERS, SENDCMPCT, SENDCMPCT, PING, FEEFILTER);
            if (envelopes.isPresent() && envelopes.get().length == 5) {
                executionTime = currentTimeMillis() - t0;

                this.sendHeaders = envelopes.get()[0];
                assertNotNull("did not rcv sendheaders after handshake", sendHeaders);

                this.sendCmpct1 = envelopes.get()[1];
                assertNotNull("did not rcv sendcmpct1 after handshake", sendCmpct1);

                this.sendCmpct2 = envelopes.get()[2];
                assertNotNull("did not rcv sendcmpct1 after handshake", sendCmpct2);

                this.ping = envelopes.get()[3];
                assertNotNull("did not rcv ping after handshake", ping);

                log.info("send" +
                        "ing pong to peer {}", addressInfo.apply(peer));
                byte[] pong = envelopeHelper.pongPayload.apply(ping.getPayload(), NETWORK);
                client.setPayload(pong);
                client.send(); // send pong

                this.feefilter = envelopes.get()[4];
                assertNotNull("did not rcv feefilter after handshake", feefilter);

                log.info("rcvd msgs  '{}', '{}', '{}', '{}', '{}' in one payload from bitcoind node {} in {}",
                        sendHeaders.getNetworkCommand().getAscii(),
                        sendCmpct1.getNetworkCommand().getAscii(), sendCmpct2.getNetworkCommand().getAscii(),
                        ping.getNetworkCommand().getAscii(), feefilter.getNetworkCommand().getAscii(),
                        addressInfo.apply(peer), durationString.apply(executionTime));

                MILLISECONDS.sleep(200L); // linger to see what peer does (nothing, apparently)
            }
        } catch (Exception e) {
            fail(e.getMessage());

        } finally {
            client.returnByteBuffer();
        }
    }

    protected InetSocketAddress getLocalPeer() {
        return new InetSocketAddress(LOCALHOST, BITCOIND_PORT.get());
    }

    protected void printCachedEnvelopes() {
        String format = "envelopes cached after handshake:\n\t%s%n"
                + "\t%s (isUsingCmpctBlk=%s, version=%d)%n\t%s (isUsingCmpctBlk=%s, version=%d)%n"
                + "\t%s%n"
                + "\t%s   (feerate=%d sats/1k bytes)";
        String envelopesString = String.format(format,
                sendHeaders.toShortString(),
                sendCmpct1.toShortString(), SendCmpctMessage.isUsingCmpctBlk(sendCmpct1.getPayload()), SendCmpctMessage.getVersionAsLong(sendCmpct1.getPayload()),
                sendCmpct2.toShortString(), SendCmpctMessage.isUsingCmpctBlk(sendCmpct2.getPayload()), SendCmpctMessage.getVersionAsLong(sendCmpct2.getPayload()),
                ping.toShortString(),
                feefilter.toShortString(), getFeeRate(feefilter.getPayload()));
        log.info(envelopesString);
    }
}
