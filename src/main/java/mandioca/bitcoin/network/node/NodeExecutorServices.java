package mandioca.bitcoin.network.node;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.err;
import static java.util.concurrent.TimeUnit.SECONDS;
import static mandioca.bitcoin.network.NetworkProperties.POOL_SIZE;

@SuppressWarnings("unused")
class NodeExecutorServices {

    private static final AtomicInteger SUB_REACTOR_NUMBER = new AtomicInteger(1);
    private static final AtomicInteger WORKER_NUMBER = new AtomicInteger(1);

    static ExecutorService createSubReactorPool() {
        return new ThreadPoolExecutor(
                POOL_SIZE, POOL_SIZE,
                60, SECONDS,
                new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setName("SUB-REACTOR-" + SUB_REACTOR_NUMBER.getAndIncrement());
            return thread;
        }, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    static ExecutorService createIOWorkerPool(boolean bounded) {
        return bounded ? createBoundedIOWorkerPool() : createCachedIOWorkerPool();
    }

    private static ExecutorService createBoundedIOWorkerPool() {
        return new ThreadPoolExecutor(
                POOL_SIZE, POOL_SIZE * 10,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), r -> {
            Thread thread = new Thread(r);
            thread.setName("IO-WORKER-" + WORKER_NUMBER.getAndIncrement());
            return thread;
        }, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private static ExecutorService createCachedIOWorkerPool() {
        // *Experimental*   creates pool of unbounded size, but reuses (short lived) processor threads when done
        //  (at risk of being swamped by too many clients).
        //
        // Profiling long running echo test shows this pool uses 200-300 more threads than the bounded pool,
        // but don't yet know about memory reduction and and exec time costs.
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool(
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("IO-WORKER-" + WORKER_NUMBER.getAndIncrement());
                    return thread;
                }
        );
        executorService.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        err.println("NodeExecutorServices :: TODO profile & compare CachedThreadPool against BoundedThreadPool");
        return executorService;
    }
}
