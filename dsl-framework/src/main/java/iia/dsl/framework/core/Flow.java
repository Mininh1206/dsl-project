package iia.dsl.framework.core;

import java.util.ArrayList;
import java.util.List;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.core.policy.ExecutionPolicy;
import iia.dsl.framework.tasks.Task;

/**
 * Representa un flujo de ejecución compuesto por una serie de elementos
 * ejecutables.
 * Puede contener Tareas (Tasks), Conectores y otros Flujos anidados.
 * Soporta ejecución secuencial (en el hilo actual) y concurrente (en
 * ThreadPool).
 */
public class Flow extends ExecutableElement {
    private final List<ExecutableElement> elements;
    private boolean concurrent;
    private ExecutionPolicy executionPolicy;

    private Flow(String id, boolean concurrent) {
        super(id);
        this.elements = new ArrayList<>();
        this.concurrent = concurrent;
        this.executionPolicy = null; // Default behavior
    }

    private Flow(String id, boolean concurrent, ExecutionPolicy policy) {
        super(id);
        this.elements = new ArrayList<>();
        this.concurrent = concurrent;
        this.executionPolicy = policy;
    }

    private Flow(boolean concurrent, ExecutionPolicy policy) {
        super();
        this.elements = new ArrayList<>();
        this.concurrent = concurrent;
        this.executionPolicy = policy;
    }

    // Default constructor for simple non-concurrent unnamed flow
    private Flow() {
        super();
        this.elements = new ArrayList<>();
        this.concurrent = false;
    }

    /**
     * Crea un nuevo Builder para construir instancias de Flow de forma fluida.
     * 
     * @return Una nueva instancia de Flow.Builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private boolean concurrent = false;
        private ExecutionPolicy policy;

        /**
         * Asigna un identificador al Flow.
         * 
         * @param id El ID del Flow.
         * @return El Builder actual.
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Configura el Flow para ejecución secuencial (por defecto).
         * 
         * @return El Builder actual.
         */
        public Builder sequential() {
            this.concurrent = false;
            return this;
        }

        public Flow build() {
            // Logic: If concurrent and no policy set, default to FifoPolicy
            if (concurrent && policy == null) {
                policy = new iia.dsl.framework.core.policy.FifoPolicy();
            }

            if (id != null) {
                return new Flow(id, concurrent, policy);
            } else {
                return new Flow(concurrent, policy);
            }
        }

        /**
         * Configura el Flow para ejecución concurrente con una política específica.
         * 
         * @param policy La política de ejecución a utilizar.
         * @return El Builder actual.
         */
        public Builder concurrent(ExecutionPolicy policy) {
            this.concurrent = true;
            this.policy = policy;
            return this;
        }
    }

    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    public boolean isConcurrent() {
        return concurrent;
    }

    public void setExecutionPolicy(iia.dsl.framework.core.policy.ExecutionPolicy executionPolicy) {
        this.executionPolicy = executionPolicy;
    }

    /**
     * Añade un elemento ejecutable (Task, Connector o Flow) a este flujo.
     * 
     * @param element El elemento a añadir.
     * @throws IllegalArgumentException Si el elemento no es de un tipo válido.
     */
    public void addElement(ExecutableElement element) {
        if (element instanceof Connector || element instanceof Task || element instanceof Flow) {
            elements.add(element);
        } else {
            throw new IllegalArgumentException("Element must be a Connector or Task");
        }
    }

    /**
     * Inicia la ejecución del flujo.
     * En modo concurrente, programa los conectores de origen.
     * En modo secuencial, ejecuta los elementos en orden.
     * 
     * @throws Exception Si ocurre un error fatal durante la inicialización.
     */
    @Override
    public void execute() throws Exception {
        boolean shouldLog = concurrent || hasActiveElements();

        if (shouldLog) {
            System.out.println(
                    "Iniciando Flow: " + (id != null ? id : "unnamed") + " [Concurrent: " + concurrent + "]");
        }

        // Propagate concurrency and policy to elements
        for (ExecutableElement element : elements) {
            element.setConcurrent(concurrent);
            if (executionPolicy != null) {
                element.setPolicy(executionPolicy);
            }
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
                } else if (element instanceof Flow) {
                    System.out.println(
                            "DEBUG: Executing Nested Flow: " + (element.getId() != null ? element.getId() : "unnamed"));
                    element.execute();
                } else {
                    // Do nothing. Tasks are triggered by onMessageAvailable.
                }
            }
            if (shouldLog) {
                System.out.println("Flow iniciado en modo CONCURRENTE (ejecución asíncrona en progreso)");
            }
        } else {
            // SEQUENTIAL MODE: Execute elements in order in the current thread
            for (ExecutableElement element : elements) {
                try {
                    if (element instanceof Connector connector && connector.isSource()) {
                        element.execute();
                    } else if (element instanceof Flow) {
                        element.execute();
                    }
                    // Tasks and other connectors are triggered via listeners (DFS)
                } catch (Exception e) {
                    System.err.println("[WARNING] Error executing sequential element "
                            + (element.getId() != null ? element.getId() : element.getClass().getSimpleName()) + ": "
                            + e.getMessage());
                    // Continue with next element
                }
            }
            if (shouldLog) {
                System.out.println("\nFlow completado exitosamente en modo SECUENCIAL\n");
            }
        }
    }

    private boolean hasActiveElements() {
        for (ExecutableElement element : elements) {
            if (element instanceof Connector connector && connector.isSource()) {
                return true;
            }
            if (element instanceof Flow flow && flow.hasActiveElements()) {
                return true;
            }
        }
        return false;
    }
}
