package iia.dsl.framework.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Singleton que gestiona el entorno de ejecución global (ThreadPool).
 * Asegura que todos los flujos compartan los mismos recursos de hilos.
 */
public class ExecutionEnvironment {
    private static ExecutionEnvironment instance;
    private final ExecutorService executor;

    private ExecutionEnvironment() {
        // Usamos un CachedThreadPool para que crezca según demanda,
        // pero reutilice hilos inactivos.
        this.executor = Executors.newCachedThreadPool();
    }

    public static synchronized ExecutionEnvironment getInstance() {
        if (instance == null) {
            instance = new ExecutionEnvironment();
        }
        return instance;
    }

    private final java.util.concurrent.atomic.AtomicInteger activeCount = new java.util.concurrent.atomic.AtomicInteger(
            0);

    public void submit(Runnable task) {
        activeCount.incrementAndGet();
        executor.submit(() -> {
            try {
                task.run();
            } finally {
                if (activeCount.decrementAndGet() == 0) {
                    synchronized (activeCount) {
                        activeCount.notifyAll();
                    }
                }
            }
        });
    }

    public void waitForQuiescence(long timeoutMillis) {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        synchronized (activeCount) {
            while (activeCount.get() > 0) {
                long timeLeft = deadline - System.currentTimeMillis();
                if (timeLeft <= 0) {
                    System.err
                            .println("[WARNING] Quiescence timeout reached. Tasks still active: " + activeCount.get());
                    break;
                }
                try {
                    activeCount.wait(timeLeft);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
