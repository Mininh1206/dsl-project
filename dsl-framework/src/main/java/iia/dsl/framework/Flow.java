package iia.dsl.framework;

import java.util.ArrayList;
import java.util.List;

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
        // Ejecutar puertos de entrada y request
        for (Port port : ports) {
            if (port instanceof InputPort || port instanceof RequestPort) {
                port.execute();
            }
        }
        
        // Ejecutar tareas
        for (Task task : tasks) {
            try {
                task.execute();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        // Ejecutar puertos de salida
        for (Port port : ports) {
            if (port instanceof OutputPort) {
                port.execute();
            }
        }
    }
}