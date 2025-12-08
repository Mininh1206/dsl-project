package iia.dsl.framework.core;

import java.util.ArrayList;
import java.util.List;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.tasks.Task;

/**
 * Clase que representa un flujo de ejecución.
 * 
 * Un flujo contiene una lista de elementos ejecutables (tareas y conectores).
 * Puede ejecutarse de forma concurrente o secuencial (por defecto).
 */
public class Flow extends ExecutableElement {
    private final List<ExecutableElement> elements;

    public Flow() {
        super();
        this.elements = new ArrayList<>();
    }

    public Flow(String id) {
        super(id);
        this.elements = new ArrayList<>();
    }

    public Flow(boolean concurrent) {
        super();
        this.elements = new ArrayList<>();
        this.concurrent = concurrent;
    }

    public Flow(String id, boolean concurrent) {
        super(id);
        this.elements = new ArrayList<>();
        this.concurrent = concurrent;
    }

    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    public boolean isConcurrent() {
        return concurrent;
    }

    public void addElement(ExecutableElement element) {
        if (element instanceof Connector || element instanceof Task) {
            elements.add(element);
        } else {
            throw new IllegalArgumentException("Element must be a Connector or Task");
        }
    }

    @Override
    public void execute() throws Exception {
        System.out.println("Iniciando Flow: " + (id != null ? id : "unnamed") + " [Concurrent: " + concurrent + "]");

        // Propagate concurrency setting to elements
        for (ExecutableElement element : elements) {
            element.setConcurrent(concurrent);
        }

        if (concurrent) {
            // CONCURRENT MODE: Kickstart execution via ThreadPool
            // BUG FIX: Must call scheduleExecution to correctly increment workCount.
            // Direct submit() leaves workCount at 0, causing it to go negative after first
            // run,
            // preventing subsequent runs.
            // BUG FIX: Only kickstart Source Connectors.
            // Tasks and Output Connectors should stay dormant until they receive a message
            // via SlotListener.
            for (ExecutableElement element : elements) {
                if (element instanceof Connector connector && connector.isSource()) {
                    System.out.println("DEBUG: Scheduling Source Connector: "
                            + (element.getId() != null ? element.getId() : element.getClass().getSimpleName()));
                    element.scheduleExecution();
                } else {
                    // Do nothing. They will be triggered by onMessageAvailable.
                }
            }
            System.out.println("Flow iniciado en modo CONCURRENTE (ejecución asíncrona en progreso)");
        } else {
            // SEQUENTIAL MODE: Execute elements in order in the current thread
            for (ExecutableElement element : elements) {
                try {
                    element.execute();
                } catch (Exception e) {
                    System.err.println("[WARNING] Error executing sequential element "
                            + (element.getId() != null ? element.getId() : element.getClass().getSimpleName()) + ": "
                            + e.getMessage());
                    // Continue with next element
                }
            }
            System.out.println("\nFlow completado exitosamente en modo SECUENCIAL\n");
        }
    }
}
