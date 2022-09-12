package mandioca.bitcoin.network.node;

import mandioca.bitcoin.network.block.BlockChainValidator;
import mandioca.bitcoin.network.block.BlockHeader;
import mandioca.bitcoin.network.block.InvalidBlockException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static junit.framework.TestCase.fail;
import static mandioca.bitcoin.function.BigIntegerFunctions.formatInt;
import static mandioca.bitcoin.function.StorageUnitConversionFunctions.byteCountString;
import static mandioca.bitcoin.function.TimeFunctions.durationString;
import static mandioca.bitcoin.network.NetworkProperties.GENESIS_HASH;
import static org.junit.Assert.assertTrue;

public class BitcoindGetHeadersTest extends BitcoindClientTest {

    private static final Logger log = LoggerFactory.getLogger(BitcoindGetHeadersTest.class);

    private static final int CLIENT_BUFFER_SIZE = 1024 * 1024 * 10; // 10 MB (depends on how many hdrs to download)

    private static final int DOWNLOAD_ITERATIONS = 1; // make looping possible for profiling, but there are < 700 k blocks

    private BlockChainValidator blockChainValidator = new BlockChainValidator();
    private int totalHeadersDownloaded = 0;
    private long totalBytesDownloaded = 0;

    @Before
    public void setup() {
        super.setup();
        super.doHandshakeWithLocalBitcoindNode(CLIENT_BUFFER_SIZE);
        super.printCachedEnvelopes();
    }

    @Test
    public void testGetAllBlockHeaders() {
        try {
            long t0 = currentTimeMillis();
            for (int i = 1; i <= DOWNLOAD_ITERATIONS; i++) {
                downloadAllHeaders();
                log.info("test iteration {} done", i);
            }
            log.info(getTestStatsString(currentTimeMillis() - t0));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private void downloadAllHeaders()
            throws IOException, ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        GetHeadersClient client = new GetHeadersClient(
                "BitcoindGetHeadersTest",
                super.client.socketChannel,
                CLIENT_BUFFER_SIZE,
                GENESIS_HASH.get());

        long t0 = currentTimeMillis();
        Future<List<BlockHeader>> result = executorService.submit(client);
        while (!result.isDone()) {
            try {
                MILLISECONDS.sleep(40L);
            } catch (InterruptedException ignored) {
            }
        }
        executorService.shutdown();

        int batchCount = result.get().size();
        assertTrue(batchCount > 0);
        totalHeadersDownloaded += batchCount;
        totalBytesDownloaded += client.getTotalBytesDownloaded();

        log.info(getDownloadStatsString(currentTimeMillis() - t0));

        try {
            blockChainValidator.validate(result.get());
        } catch (InvalidBlockException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private String getDownloadStatsString(long time) {
        return String.format("chain download stats:  %s block headers (%s) downloaded in %s at rate of %s headers/s (%.2f MB/s)",
                formatInt.apply(totalHeadersDownloaded),
                byteCountString.apply(totalBytesDownloaded),
                durationString.apply(time),
                formatInt.apply((int) (totalHeadersDownloaded * 1000L / time)),
                totalBytesDownloaded * 0.001 / time);
    }

    private String getTestStatsString(long time) {
        return String.format("final test stats:  %s block headers (%s) downloaded and validated in %s at rate of %s headers/s (%.2f MB/s)",
                formatInt.apply(totalHeadersDownloaded),
                byteCountString.apply(totalBytesDownloaded),
                durationString.apply(time),
                formatInt.apply((int) (totalHeadersDownloaded * 1000L / time)),
                totalBytesDownloaded * 0.001 / time);
    }
}
