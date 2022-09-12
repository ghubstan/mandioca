package mandioca.bitcoin.network.node;

import mandioca.bitcoin.util.Triple;
import mandioca.bitcoin.util.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static mandioca.bitcoin.function.LongFunctions.formatLong;
import static mandioca.bitcoin.function.TimeFunctions.logTimeAndRequestRate;
import static mandioca.bitcoin.function.TimeFunctions.minutesSecondsToMilliseconds;
import static mandioca.bitcoin.network.NetworkProperties.LOCALHOST;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("ConstantConditions")
public class EchoClientLongRunningTest extends NodeTest {

    private static final Logger log = LoggerFactory.getLogger(EchoClientLongRunningTest.class);

    private static final boolean FORCE_SHUTDOWN = true;
    private static final boolean USE_RANDOM_SERVER_PORT = true;

    private static final int NUM_CLIENTS = 500;
    private static final int NUM_REQUESTS_PER_CLIENT = SIX_K;
    private static final long TOTAL_REQUESTS = NUM_CLIENTS * NUM_REQUESTS_PER_CLIENT;

    protected static final Supplier<Long> baselineEstimatedExecTimeInMillis = () -> {
        // baseline timings for this test have been consistent as of 2020-01-17 (but client slept after ea req)
        // baseline timings improve on 2020-01-30T11:50:48-03:00 (client never sleeps & test is now cpu bound)
        switch (NUM_CLIENTS * NUM_REQUESTS_PER_CLIENT) {
            case 24_000_000:
                return minutesSecondsToMilliseconds.apply(4, 45);
            case 18_000_000:
                return minutesSecondsToMilliseconds.apply(3, 32);
            case 12_000_000:
                return minutesSecondsToMilliseconds.apply(2, 20);
            case 6_000_000:
                return minutesSecondsToMilliseconds.apply(1, 10);
            case 3_000_000:
                return 33_000L;
            case 1_000_000:
                return 12_000L;
            case 500_000:
                return 7_500L;
            case 300_000:
                return 5_600L;
            case 150_000:
                return 4_000L;
            case 37_500:
                return 2_600L;
            case 12_500:
                return 1_600L;
            default:
                return 1_000L;
        }
    };
    private static final long ESTIMATED_EXECUTION_TIME = baselineEstimatedExecTimeInMillis.get();
    private static final long EXTRA_EXECUTION_TIME = 10_000L;

    private Node server;

    @Before
    public void setup() {
        if (USE_RANDOM_SERVER_PORT) {
            server = startServer(LOCALHOST, 0);
        } else {
            server = startServer(LOCALHOST, portFactory.nextPort.get());
        }
    }

    @Test
    public void testSimpleClient() {
        logTimeAndRequestRate.accept(log, "estimated exec time", TOTAL_REQUESTS, ESTIMATED_EXECUTION_TIME);
        List<EchoClient> clients = createSimpleClients(server.getPort(), NUM_CLIENTS, NUM_REQUESTS_PER_CLIENT);
        ExecutorService executorService = Executors.newFixedThreadPool(clients.size());
        long t0 = currentTimeMillis();

        final Triple<List<Future<Boolean>>, Boolean, Long> testResults = runRequestLoops(executorService, clients, t0);
        final List<Future<Boolean>> results = testResults.getX();
        boolean isFinished = testResults.getY();
        long actualExecutionTime = testResults.getZ();

        if (FORCE_SHUTDOWN) {
            executorService.shutdownNow();  // TODO what's the difference between shutdownNow and shutdown?
            if (!isFinished) {
                Tuple<Boolean, Long> overtimeResults = allowExtraTime(results, t0);
                isFinished = overtimeResults.getX();
                actualExecutionTime = overtimeResults.getY();
            }
            server.shutdown();
        } else {
            executorService.shutdown();
            actualExecutionTime = currentTimeMillis() - t0;
        }

        if (!isFinished) {
            fail("did not complete test within time limit " +
                    formatLong.apply(ESTIMATED_EXECUTION_TIME + EXTRA_EXECUTION_TIME) + " ms");
        }

        assertTrue(resultsAreValid(Objects.requireNonNull(results)));
        logTimeAndRequestRate.accept(log, "actual exec time", TOTAL_REQUESTS, actualExecutionTime);
    }

    private Tuple<Boolean, Long> allowExtraTime(final List<Future<Boolean>> results, long startTime) {
        log.warn("did not complete test within estimated time limit {} ms;  allowing an extra {} ms to finish",
                ESTIMATED_EXECUTION_TIME, EchoClientLongRunningTest.EXTRA_EXECUTION_TIME);
        long t1 = currentTimeMillis(), executionTime = -1;
        boolean isFinished = false;
        while ((currentTimeMillis() - t1) < EchoClientLongRunningTest.EXTRA_EXECUTION_TIME) {
            try {
                MILLISECONDS.sleep(150);
                if (resultsReady(Objects.requireNonNull(results))) {
                    executionTime = currentTimeMillis() - startTime;
                    isFinished = true;
                    break;
                }
            } catch (InterruptedException ignored) {
            }
        }
        return new Tuple<>(isFinished, executionTime);
    }

    private Triple<List<Future<Boolean>>, Boolean, Long> runRequestLoops(ExecutorService executorService, List<EchoClient> clients, long startTime) {
        boolean isFinished = false;
        long executionTime = -1;
        List<Future<Boolean>> results = null;
        try {
            results = executorService.invokeAll(clients);
            // Instead of using testExecutorService.awaitTermination(estimatedExecutionTime, MILLISECONDS),
            // check for task completion every 200 ms, until complete, or 'estimatedExecutionTime' has elapsed.
            // Then, run testExecutorService.shutdownNow() and check the results.
            // I don't want these test to run longer than necessary; it also helps to estimate response times and
            // check against a hopefully improving baseline.
            while ((currentTimeMillis() - startTime) < ESTIMATED_EXECUTION_TIME) {
                try {
                    MILLISECONDS.sleep(150);
                    if (resultsReady(Objects.requireNonNull(results))) {
                        executionTime = currentTimeMillis() - startTime;
                        isFinished = true;
                        break;
                    }
                } catch (InterruptedException ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return new Triple<>(results, isFinished, executionTime);
    }


    private boolean resultsAreValid(List<Future<Boolean>> results) {
        if (NUM_CLIENTS != results.size()) {
            return false;
        }
        for (Future<Boolean> r : results) {
            try {
                if (!r.get()) {
                    return false;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
