package iia.dsl.framework.core.policy;

import iia.dsl.framework.core.ExecutableElement;

/**
 * Política que prioriza las tareas con más trabajo pendiente (workCount más
 * alto).
 * Útil para desatascar cuellos de botella.
 */
public class MostWorkPolicy implements ExecutionPolicy {

    @Override
    public int compare(ExecutableElement task1, long submissionTime1, ExecutableElement task2, long submissionTime2) {
        int work1 = task1.getWorkCount();
        int work2 = task2.getWorkCount();

        // Mayor workCount -> Mayor prioridad -> Valor negativo
        // (Invertimos el orden natural de Integer.compare)
        int comparison = Integer.compare(work2, work1);

        if (comparison == 0) {
            // Si tienen igual carga de trabajo, usar FIFO como desempate
            return Long.compare(submissionTime1, submissionTime2);
        }
        return comparison;
    }
}
