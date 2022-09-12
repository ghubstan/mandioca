package mandioca.bitcoin.network.node;

import mandioca.bitcoin.network.block.io.MerkleBlockReader;
import mandioca.bitcoin.network.message.GetDataMessage;
import mandioca.bitcoin.network.message.MerkleBlockMessage;
import mandioca.bitcoin.transaction.Tx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;
import static mandioca.bitcoin.util.HexUtils.HEX;

/**
 * From https://stackoverflow.com/questions/1371369/can-constructors-throw-exceptions-in-java#1371559
 * Yes, constructors can throw exceptions. Usually this means that the new object is immediately eligible for
 * garbage collection (although it may not be collected for some time, of course). It's possible for the
 * "half-constructed" object to stick around though, if it's made itself visible earlier in the constructor
 * (e.g. by assigning a static field, or adding itself to a collection).
 * <p>
 * One thing to be careful of about throwing exceptions in the constructor: because the caller (usually)
 * will have no way of using the new object, the constructor ought to be careful to avoid acquiring unmanaged
 * resources (file handles etc) and then throwing an exception without releasing them. For example, if the
 * constructor tries to open a FileInputStream and a FileOutputStream, and the first succeeds but the second
 * fails, you should try to close the first stream. This becomes harder if it's a subclass constructor which
 * throws the exception, of course... it all becomes a bit tricky. It's not a problem very often, but it's
 * worth considering.
 * <p>
 * <p>
 * <p>
 * Yes, they can throw exceptions. If so, they will only be partially initialized and if non-final, subject to attack.
 * <p>
 * The following is from the Secure Coding Guidelines 2.0.
 * <p>
 * Partially initialized instances of a non-final class can be accessed via a finalizer attack. The attacker overrides
 * the protected finalize method in a subclass, and attempts to create a new instance of that subclass.
 * This attempt fails (in the above example, the SecurityManager check in ClassLoader's constructor throws a
 * security exception), but the attacker simply ignores any exception and waits for the virtual machine to perform
 * finalization on the partially initialized object. When that occurs the malicious finalize method implementation is
 * invoked, giving the attacker access to this, a reference to the object being finalized. Although the object is
 * only partially initialized, the attacker can still invoke methods on it (thereby circumventing the SecurityManager
 * check).
 *
 * @throws IOException
 */
public final class GetDataClient extends CallableClient<Boolean> implements Client {

    private static final Logger log = LoggerFactory.getLogger(GetDataClient.class);

    private MerkleBlockReader merkleBlockReader;
    private final GetDataMessage getDataMessage;

    private long startTime;

    private final List<MerkleBlockMessage> merkleBlocks = new ArrayList<>();
    private final List<Tx> transactions = new ArrayList<>();

    public GetDataClient(
            String nodeName,
            SocketChannel socketChannel,
            int byteBufferSize,
            GetDataMessage getDataMessage)
            throws IOException, IllegalStateException {
        super(nodeName, (InetSocketAddress) socketChannel.getRemoteAddress(), byteBufferSize);
        if (!socketChannel.isConnected()) {
            throw new IllegalStateException("socket channel not connected");
        }
        this.socketChannel = socketChannel;
        this.getDataMessage = getDataMessage;
    }

    @Override
    public Boolean call() {
        try {
            if (socketChannel == null || !socketChannel.isConnected()) {
                throw new IllegalStateException("socket channel not configured and connected");
            }
            sendGetDataMessage();
            processResults();
            return merkleBlocks.size() > 0 && transactions.size() > 0;
        } finally {
            returnByteBuffer();  // don't close channel, but return buffer to pool
        }
    }


    private void processResults() {
        // TX describes a bitcoin transaction, in reply to 'getdata'. When a bloom filter is applied tx objects
        // are sent automatically for matching transactions following the merkleblock
        merkleBlocks.clear();
        startTime = currentTimeMillis();
        merkleBlockReader = new MerkleBlockReader(socketChannel);  // reuse for instance's life

        try {
            downloadResultBatches();
        } catch (Exception e) {
            throw new RuntimeException("error downloading batch of block headers", e);
        }
        if (log.isDebugEnabled()) {
            // log.debug(getTotalDownloadStatsString(currentTimeMillis() - startTime));
        }
    }

    public List<MerkleBlockMessage> getMerkleBlocks() {
        return this.merkleBlocks;
    }

    public List<Tx> getTransactions() {
        return this.transactions;
    }


    private List<MerkleBlockMessage> getMerkleBlocksDeprecATED() {
        sendGetDataMessage();

        // TX describes a bitcoin transaction, in reply to 'getdata'. When a bloom filter is applied tx objects
        // are sent automatically for matching transactions following the merkleblock
        merkleBlocks.clear();
        startTime = currentTimeMillis();
        merkleBlockReader = new MerkleBlockReader(socketChannel);  // reuse for instance's life

        try {
            downloadResultBatches();
        } catch (Exception e) {
            throw new RuntimeException("error downloading batch of block headers", e);
        }
        if (log.isDebugEnabled()) {
            // log.debug(getTotalDownloadStatsString(currentTimeMillis() - startTime));
        }
        return merkleBlocks;
    }

    private void downloadResultBatches() {
        while (true) {
            try {
                MILLISECONDS.sleep(100L); // *must* wait for socket
                List<MerkleBlockMessage> batch = downloadBatch();
                if (batch.isEmpty()) {
                    break;
                } else {
                    merkleBlocks.addAll(batch);
                }
            } catch (InterruptedException ignored) {
            }
        }
    }


    // TODO decompose
    private List<MerkleBlockMessage> downloadBatch() {
        try {
            long t0 = currentTimeMillis();

            NetworkEnvelope[] envelopes = merkleBlockReader.call();
            if (log.isDebugEnabled()) {
                log.debug("Rcvd {} merkleblock envelopes", envelopes.length);
            }

            List<MerkleBlockMessage> merkleBlocks = new ArrayList<>(envelopes.length);
            for (NetworkEnvelope envelope : envelopes) {
                if (envelopeHelper.isMerkleBlock.test(envelope)) {
                    merkleBlocks.add(MerkleBlockMessage.parse(stream.apply(envelope.getPayload())));
                } else if (envelopeHelper.isTx.test(envelope)) {
                    try {
                        transactions.add(Tx.parse(stream.apply(envelope.getPayload()), NETWORK));
                    } catch (Exception e) {
                        if (e.getCause().getMessage().contains("error decoding tx outputs")) {

                            //

                            log.error(e.getCause().getMessage() + " in envelope payload " + envelope.getPayloadAsHex());

                        } else {
                            throw e;
                        }
                    }
                } else {
                    throw new RuntimeException(envelope.getNetworkCommand().getAscii() + " is not a merkleblock or tx envelope");
                }
            }
            return merkleBlocks;
        } catch (Exception e) {
            // TODO make sure block locator string is big endian hex
            throw new RuntimeException("error downloading merkleblock & tx envelopes", e);
        }
    }

    private void sendGetDataMessage() {
        try {
            log.info("{} sending getdata msg", nodeName);
            byte[] bytes = envelopeHelper.getDataPayload.apply(getDataMessage, NETWORK);
            setPayload(bytes);
            if (log.isDebugEnabled()) {
                log.debug("{} sending getdata envelope {}", nodeName, HEX.encode(bytes));
            }
            send();
            MILLISECONDS.sleep(20L); // *must* wait more than 10ms before trying to read headers response

        } catch (InterruptedException ignored) {
        } catch (IOException e) {
            throw new RuntimeException("error sending getdata", e);
        }
    }

}
