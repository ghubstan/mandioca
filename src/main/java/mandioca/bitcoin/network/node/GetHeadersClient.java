package mandioca.bitcoin.network.node;

import mandioca.bitcoin.network.block.BlockHeader;
import mandioca.bitcoin.network.block.io.BlockHeaderReader;
import mandioca.bitcoin.network.message.GetHeadersMessage;
import mandioca.bitcoin.network.message.HeadersMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static mandioca.bitcoin.function.BigIntegerFunctions.formatInt;
import static mandioca.bitcoin.function.ByteArrayFunctions.isZero;
import static mandioca.bitcoin.function.StorageUnitConversionFunctions.byteCountString;
import static mandioca.bitcoin.function.TimeFunctions.durationString;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;

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
public final class GetHeadersClient extends CallableClient<List<BlockHeader>> implements Client {

    // TODO if the node 'ignores' getheaders msg, run ./bitcoin-cli -regtest generate 1
    // SEE https://github.com/pzemtsov/article-buffering-streams

    private static final Logger log = LoggerFactory.getLogger(GetHeadersClient.class);

    private BlockHeaderReader blockHeaderReader;

    private final Predicate<byte[]> isLastPayload = (p) -> p.length == 0 && isZero.test(p);

    private final List<BlockHeader> blockHeaders = new ArrayList<>();

    private long startTime;

    private final String startBlockLocator;
    private final int batchLimit;  // 0 means no limit; get all headers in the blockchain after startBlockLocator

    // TODO add ctor taking endBlockLocator param (and byte[] block locators instead of string)

    public GetHeadersClient(
            String nodeName,
            SocketChannel socketChannel,
            int byteBufferSize,
            String startBlockLocator)
            throws IOException, IllegalStateException {
        this(nodeName, socketChannel, byteBufferSize, startBlockLocator, 0);
    }

    public GetHeadersClient(
            String nodeName,
            SocketChannel socketChannel,
            int byteBufferSize,
            String startBlockLocator,
            int batchLimit)
            throws IOException, IllegalStateException {
        super(nodeName, (InetSocketAddress) socketChannel.getRemoteAddress(), byteBufferSize);
        if (!socketChannel.isConnected()) {
            throw new IllegalStateException("socket channel not connected");
        }
        this.socketChannel = socketChannel;
        this.startBlockLocator = startBlockLocator;
        this.batchLimit = batchLimit;
    }

    @Override
    public List<BlockHeader> call() {
        try {
            if (socketChannel == null || !socketChannel.isConnected()) {
                throw new IllegalStateException("socket channel not configured and connected");
            }
            return getBlockHeaders();
        } finally {
            returnByteBuffer();  // don't close channel, but return buffer to pool
        }
    }

    public int getBatchLimit() {
        return batchLimit;
    }

    public long getTotalBytesDownloaded() {
        return blockHeaderReader != null ? blockHeaderReader.getAccumulatedReadCount() : 0L;
    }

    private List<BlockHeader> getBlockHeaders() {
        try {
            startTime = currentTimeMillis();
            blockHeaders.clear();
            blockHeaderReader = new BlockHeaderReader(socketChannel);  // reuse for instance's life
            try {
                downloadBlockHeaders();
            } catch (Exception e) {
                throw new RuntimeException("error downloading batch of block headers", e);
            }
            if (log.isDebugEnabled()) {
                log.debug(getTotalDownloadStatsString(currentTimeMillis() - startTime));
            }
        } catch (Exception e) {
            throw new RuntimeException("error downloading block headers", e);
        }
        return blockHeaders;
    }

    private final Function<Integer, Boolean> reachedBatchLimit = (n) -> this.getBatchLimit() > 0 && n >= this.getBatchLimit();

    private void downloadBlockHeaders() {
        String blockLocator = startBlockLocator;
        int batchCount = 0;
        while (!reachedBatchLimit.apply(batchCount)) {
            List<BlockHeader> batch = downloadBatch(blockLocator);
            if (batch.isEmpty()) {
                break;
            } else {
                blockHeaders.addAll(batch);
                batchCount++;
                if (blockHeaders.size() % 100_000 == 0) {
                    try {
                        MILLISECONDS.sleep(500L);
                    } catch (InterruptedException ignored) {
                    }
                }
                blockLocator = batch.get(batch.size() - 1).getHashHex();  // send me next 2k headers AFTER this one
            }
        }
    }

    // TODO decompose
    // TODO decompose
    // TODO decompose
    private List<BlockHeader> downloadBatch(String blockLocator) {
        try {
            long t0 = currentTimeMillis();
            sendGetHeaders(blockLocator);
            NetworkEnvelope envelope = blockHeaderReader.call();
            long payloadDownloadTime = currentTimeMillis() - t0;
            if (envelopeHelper.isHeaders.test(envelope)) {
                byte[] payload = envelope.getPayload();
                long p0 = currentTimeMillis();
                BlockHeader[] batch = HeadersMessage.parse(payload);
                long parseTime = currentTimeMillis() - p0;
                if (log.isDebugEnabled()) {
                    logBatchDownloadStats(payload.length, batch.length, payloadDownloadTime, parseTime);
                }
                return Arrays.stream(batch).collect(Collectors.toCollection(ArrayList::new));

            } else if (envelopeHelper.isSendHeaders.test(envelope)) {
                log.info("got sendheaders payload (no payload), continuing...");
                return downloadBatch(blockLocator); // recursively continue on

            } else if (envelopeHelper.isSendCmpct.test(envelope)) {
                log.info("got sendcmpct payload, continuing...");
                log.info(envelope.toString());
                // TODO is this dead code... never happens?
                return downloadBatch(blockLocator); // recursively continue on

            } else if (envelopeHelper.isInv.test(envelope)) {
                log.info("got inv payload");
                return new ArrayList<>();  // TODO
            } else if (isLastPayload.test(envelope.getPayload())) {
                return new ArrayList<>();  // done; we got a headers envelope containing 0x00
            } else if (envelopeHelper.isPing.test(envelope)) {
                setPayload(envelopeHelper.pongPayload.apply(envelope.getPayload(), NETWORK));
                send();  // send a pong to keep it happy
                log.debug("recursively calling downloadBatch after sending pong msg");
                return downloadBatch(blockLocator); // recursively continue on
            } else {
                log.error("unexpected envelope {}", envelope);
                throw new RuntimeException(envelope.getNetworkCommand().getAscii() + " is not a headers, sendheaders, inv, or ping envelope");
            }
        } catch (Exception e) {
            // TODO make sure block locator string is big endian hex
            throw new RuntimeException("error downloading blk header batch from block locator " + blockLocator, e);
        }
    }


    private void sendGetHeaders(String blockLocator) {
        GetHeadersMessage getHeadersMessage = new GetHeadersMessage(blockLocator);
        try {
            if (log.isDebugEnabled()) {
                log.debug("{} sending getheaders msg with blk locator '{}' hash stop '{}'",
                        nodeName,
                        getHeadersMessage.blockLocatorBigEndianHex(),
                        getHeadersMessage.hasZeroHashStop() ? "00..." : getHeadersMessage.hashStopBigEndianHex());
            }
            setPayload(envelopeHelper.headersPayload.apply(getHeadersMessage, NETWORK));
            send();
            MILLISECONDS.sleep(20L); // *must* wait more than 10ms before trying to read headers response
        } catch (InterruptedException ignored) {
        } catch (IOException e) {
            throw new RuntimeException("error sending getheaders msg with block locator "
                    + getHeadersMessage.blockLocatorBigEndianHex(), e);
        }
    }

    private void logBatchDownloadStats(int payloadSize, int batchSize, long payloadDownloadTime, long parseTime) {
        long accumulatedTime = currentTimeMillis() - startTime;
        if (batchSize == 0) {
            log.debug("{} processed end-of-batch payload in {}", nodeName, durationString.apply(accumulatedTime));
        } else {
            log.debug("{} {} ", nodeName,
                    getBatchDownloadStatsString(payloadSize, batchSize, payloadDownloadTime, parseTime, accumulatedTime));
        }
    }

    private static final String BATCH_STATS_FORMAT = "block header batch download stats: %d bytes (%s) downloaded in %s;%n"
            + "\t\t\t\t\t%d block headers parsed in %s; %s total headers downloaded in %s";

    private String getBatchDownloadStatsString(long payloadSize, int numHeaders, long downloadTime, long parseTime, long totalTime) {
        return String.format(BATCH_STATS_FORMAT,
                payloadSize, byteCountString.apply(payloadSize), durationString.apply(downloadTime),
                numHeaders, durationString.apply(parseTime),
                formatInt.apply(numHeaders + blockHeaders.size()), durationString.apply(totalTime));
    }

    private String getTotalDownloadStatsString(long time) {
        return String.format("block header download stats:  %s block headers (%s) downloaded in %s at rate of %d headers/s (%.2f MB/s)",
                formatInt.apply(blockHeaders.size()),
                byteCountString.apply(blockHeaderReader.getAccumulatedReadCount()),
                durationString.apply(time),
                blockHeaders.size() * 1000L / time,
                blockHeaderReader.getAccumulatedReadCount() * 0.001 / time);
    }
}
