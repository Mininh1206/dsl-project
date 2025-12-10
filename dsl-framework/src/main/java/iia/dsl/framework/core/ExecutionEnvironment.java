package iia.dsl.framework.core;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton que gestiona el entorno de ejecución global (ThreadPool) para el
 * framework.
 * Soporta la ejecución de tareas priorizadas y la gestión del ciclo de vida de
 * los hilos.
 */
public class ExecutionEnvironment {
    private static ExecutionEnvironment instance;
    private final ThreadPoolExecutor executor;
    private final AtomicInteger activeCount = new AtomicInteger(0);

    private ExecutionEnvironment() {
        // Usamos un PriorityBlockingQueue para ordenar las tareas según su política.
        // Core pool size: 4 (ajustable), Max pool size: Integer.MAX_VALUE (crece si es
        // necesario)
        // Keep alive: 60s
        this.executor = new ThreadPoolExecutor(
                4, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new PriorityBlockingQueue<>());
    }

    public static synchronized ExecutionEnvironment getInstance() {
        if (instance == null) {
            instance = new ExecutionEnvironment();
        }
        return instance;
    }

    /**
     * Common interface for items in the priority queue.
     */
    private interface QueueItem extends Runnable, Comparable<QueueItem> {
        // Marker interface
    }

    /**
     * Envía una tarea para ser ejecutada por el ThreadPool.
     * Envuelve la tarea en un decorador con prioridad según corresponda.
     * 
     * @param task La tarea (Runnable) a ejecutar.
     */
    public void submit(Runnable task) {
        activeCount.incrementAndGet();

        QueueItem prioritizedRunnable;
        if (task instanceof ExecutableElement element) {
            prioritizedRunnable = new PrioritizedTask(element, System.nanoTime(), activeCount);
        } else {
            prioritizedRunnable = new PrioritizedRunnableWrapper(task, activeCount);
        }

        executor.execute(prioritizedRunnable);
    }

    /**
     * Espera a que el sistema alcance un estado de quietud (sin tareas activas).
     * 
     * @param inactivityWindowMillis Tiempo en milisegundos que el sistema debe
     *                               permanecer inactivo
     *                               para considerar que ha alcanzado la quietud.
     *                               Si se detecta actividad antes de completar este
     *                               tiempo, se reinicia la espera.
     */
    public void waitForQuiescence(long inactivityWindowMillis) {
        synchronized (activeCount) {
            while (true) {
                // Phase 1: Wait for tasks to finish (Infinite wait)
                while (activeCount.get() > 0) {
                    try {
                        activeCount.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                // Phase 2: Verify stability (Inactivity Window)
                if (inactivityWindowMillis <= 0) {
                    return;
                }

                long start = System.currentTimeMillis();
                try {
                    activeCount.wait(inactivityWindowMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                // If we are here, we woke up due to:
                // 1. Timeout (stability check passed?)
                // 2. Notify (task finished 1->0, meaning updated)
                // 3. Spurious wake

                if (activeCount.get() > 0) {
                    // Activity detected, go back to Phase 1
                    continue;
                }

                // If still 0... check if we waited the full window
                long elapsed = System.currentTimeMillis() - start;
                if (elapsed >= inactivityWindowMillis) {
                    // Success: System has been idle for the full window
                    return;
                }

                // If woken early but count is 0, silence was likely broken (0->1->0).
                // Restart window.
            }
        }
    }

    /**
     * Cierra el ThreadPoolExecutor, rechazando nuevas tareas.
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * Wrapper para comparar tareas según la política del elemento.
     */
    private static class PrioritizedTask implements QueueItem {
        private final ExecutableElement element;
        private final long submissionTime;
        private final AtomicInteger activeCount;

        public PrioritizedTask(ExecutableElement element, long submissionTime, AtomicInteger activeCount) {
            this.element = element;
            this.submissionTime = submissionTime;
            this.activeCount = activeCount;
        }

        @Override
        public void run() {
            try {
                element.run();
            } finally {
                decrementActiveCount();
            }
        }

        private void decrementActiveCount() {
            if (activeCount.decrementAndGet() == 0) {
                synchronized (activeCount) {
                    activeCount.notifyAll();
                }
            }
        }

        @Override
        public int compareTo(QueueItem other) {
            if (other instanceof PrioritizedTask otherTask) {
                // Both are PrioritizedTasks -> Use policy of 'this' element
                return this.element.getPolicy().compare(this.element, this.submissionTime, otherTask.element,
                        otherTask.submissionTime);
            } else {
                // This is a PrioritizedTask, other is Wrapper.
                // PrioritizedTask (High Priority) comes BEFORE Wrapper (Low Priority).
                // Returning negative value means 'this' is smaller (higher priority).
                return -1;
            }
        }
    }

    /**
     * Wrapper para Runnables genéricos sin prioridad específica.
     */
    private static class PrioritizedRunnableWrapper implements QueueItem {
        private final Runnable task;
        private final AtomicInteger activeCount;

        public PrioritizedRunnableWrapper(Runnable task, AtomicInteger activeCount) {
            this.task = task;
            this.activeCount = activeCount;
        }

        @Override
        public void run() {
            try {
                task.run();
            } finally {
                if (activeCount.decrementAndGet() == 0) {
                    synchronized (activeCount) {
                        activeCount.notifyAll();
                    }
                }
            }
        }

        @Override
        public int compareTo(QueueItem other) {
            if (other instanceof PrioritizedRunnableWrapper) {
                return 0;
            } else {
                // This is Wrapper, other is PrioritizedTask.
                // Wrapper (Low) comes AFTER Task (High).
                return 1;
            }
        }
    }
}
