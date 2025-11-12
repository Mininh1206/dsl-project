package iia.dsl.framework.core;

import java.util.ArrayList;
import java.util.List;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.Port;
import iia.dsl.framework.ports.RequestPort;
import iia.dsl.framework.tasks.Task;

public class Flow extends Element {
    private final List<Port> ports;
    private final List<Task> tasks;
    
    public Flow(String id) {
        super(id);
        this.ports = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }
    
    public void addPort(Port port) {
        ports.add(port);
    }
    
    public void addTask(Task task) {
        tasks.add(task);
    }
    
    public void execute() {
        try {
            System.out.println("Ejecutando Flow: " + id);
            
            // 1. Ejecutar InputPorts (cargar datos)
            System.out.println("\n1. Cargando datos de entrada...");
            ports.stream()
                .filter(p -> p instanceof InputPort)
                .forEach(Port::execute);
            
            // 2. Ejecutar RequestPorts si existen
            System.out.println("\n2. Procesando requests externos...");
            ports.stream()
                .filter(p -> p instanceof RequestPort)
                .forEach(Port::execute);
            
            // 3. Ejecutar Tasks en orden
            System.out.println("\n3. Ejecutando pipeline de tareas...");
            for (Task task : tasks) {
                System.out.println("\tEjecutando: " + task.getId() + " (" + task.getType() + ")");
                task.execute();
            }
            
            // 4. Ejecutar OutputPorts (enviar resultados)
            System.out.println("\n4. Enviando resultados...");
            ports.stream()
                .filter(p -> p instanceof OutputPort)
                .forEach(Port::execute);
            
            System.out.println("\nFlow completado exitosamente\n");
            
        } catch (Exception e) {
            System.err.println("Error en Flow: " + e.getMessage());
            throw new RuntimeException("Flow execution failed", e);
        }
    }
}
