package iia.dsl.framework.core.policy;

import iia.dsl.framework.core.ExecutableElement;

/**
 * Estrategia para determinar la prioridad de ejecución de un elemento.
 * Permite comparar dos tareas para decidir cuál debe ejecutarse antes.
 */
public interface ExecutionPolicy {

    /**
     * Compara dos tareas para determinar su prioridad.
     * Retorna:
     * - Valor negativo si task1 tiene MAYOR prioridad que task2 (se ejecuta antes).
     * - Valor positivo si task1 tiene MENOR prioridad que task2 (se ejecuta
     * después).
     * - Cero si tienen igual prioridad.
     * 
     * @param task1           El primer elemento a comparar wrapper
     * @param submissionTime1 Tiempo de timestamp (nanoTime) en que task1 fue
     *                        enviada a ejecución
     * @param task2           El segundo elemento a comparar wrapper
     * @param submissionTime2 Tiempo de timestamp (nanoTime) en que task2 fue
     *                        enviada a ejecución
     */
    int compare(ExecutableElement task1, long submissionTime1, ExecutableElement task2, long submissionTime2);
}
