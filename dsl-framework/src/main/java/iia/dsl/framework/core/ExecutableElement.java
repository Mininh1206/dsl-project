package iia.dsl.framework.core;

import iia.dsl.framework.core.policy.FifoPolicy;
import iia.dsl.framework.core.policy.ExecutionPolicy;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base para elementos que pueden ser ejecutados (Tasks, Connectors, Flows).
 * Implementa la lógica de ejecución concurrente vs secuencial y la gestión de
 * prioridades.
 * Actúa como Listener de Slots para reaccionar a la llegada de mensajes.
 */
public abstract class ExecutableElement extends Element implements Runnable, SlotListener {
    // Counts the number of pending execution requests (messages)
    protected final AtomicInteger workCount = new AtomicInteger(0);
    protected boolean concurrent = false;
    protected ExecutionPolicy policy = new FifoPolicy();

    public ExecutableElement() {
        super();
    }

    public ExecutableElement(String id) {
        super(id);
    }

    /**
     * Ejecuta la lógica principal del elemento.
     * 
     * @throws Exception Si ocurre un error durante la ejecución.
     */
    public abstract void execute() throws Exception;

    /**
     * Define si el elemento debe ejecutarse de forma concurrente,
     * utilizando el ExecutionEnvironment.
     * 
     * @param concurrent true para ejecución concurrente (hilos), false para
     *                   secuencial.
     */
    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    /**
     * Devuelve si el elemento está configurado para ejecución concurrente.
     * 
     * @return true si es concurrente, false de lo contrario.
     */
    public boolean isConcurrent() {
        return concurrent;
    }

    /**
     * Establece la política de ejecución (prioridad) para este elemento.
     * 
     * @param policy La política a aplicar (ej. Fifo, MostWork).
     */
    public void setPolicy(iia.dsl.framework.core.policy.ExecutionPolicy policy) {
        this.policy = policy;
    }

    public iia.dsl.framework.core.policy.ExecutionPolicy getPolicy() {
        return policy;
    }

    public int getWorkCount() {
        return workCount.get();
    }

    /**
     * Método invocado cuando un Slot observado recibe un nuevo mensaje.
     * Desencadena la ejecución del elemento según su modo (secuencial o
     * concurrente).
     * 
     * @param slot El slot que ha recibido el mensaje.
     */
    @Override
    public void onMessageAvailable(Slot slot) {
        if (concurrent) {
            scheduleExecution();
        } else {
            // Sequential: Execute immediately on the current thread
            // We delegate to run() to ensure exception handling is consistent
            run();
        }
    }

    /**
     * Programa la ejecución de este elemento en el ExecutionEnvironment.
     * Gestiona el contador de trabajo atómico para evitar envíos redundantes.
     */
    protected void scheduleExecution() {
        // Increment work count. If we were at 0, we need to kickstart the thread.
        // If we were > 0, the running thread will pick it up on its next
        // loop/resubmission.
        if (workCount.getAndIncrement() == 0) {
            ExecutionEnvironment.getInstance().submit(this);
        }
    }

    /**
     * Implementación de Runnable para la ejecución en hilos.
     * Envuelve la llamada a execute() con gestión de errores y re-envío automático
     * si hay más trabajo pendiente (workCount > 0).
     */
    @Override
    public void run() {
        try {
            // Execute ONE unit of work (or drain queue depending on impl)
            try {
                execute();
            } catch (Exception ex) {
                System.err.println("[ERROR] Error executing element " + (id != null ? id : "unnamed") + ": "
                        + ex.getMessage());
                ex.printStackTrace();
                Logger.getLogger(ExecutableElement.class.getName()).log(Level.SEVERE,
                        "Error executing element " + id, ex);
            }

            // Decrement work count. If there is still work pending, RESUBMIT to the queue.
            // This ensures fairness: we go back to the priority queue and let the policy
            // decide again.
            // ONLY in concurrent mode.
            if (concurrent && workCount.decrementAndGet() > 0) {
                ExecutionEnvironment.getInstance().submit(this);
            }

        } catch (Exception e) {
            System.err.println("[CRITICAL] Catastrophic failure in element " + (id != null ? id : "unnamed"));
            e.printStackTrace();
            // Reset in case of catastrophic failure to avoid stuck state
            workCount.set(0);
        }
    }
}
