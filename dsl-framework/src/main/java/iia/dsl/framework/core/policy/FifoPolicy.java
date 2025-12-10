package iia.dsl.framework.core.policy;

import iia.dsl.framework.core.ExecutableElement;

/**
 * Política First-In-First-Out (FIFO).
 * El elemento que entró primero a la cola de ejecución se ejecuta primero.
 */
public class FifoPolicy implements ExecutionPolicy {

    @Override
    public int compare(ExecutableElement task1, long submissionTime1, ExecutableElement task2, long submissionTime2) {
        // Menor tiempo de envío -> Mayor prioridad -> Valor negativo
        return Long.compare(submissionTime1, submissionTime2);
    }
}
