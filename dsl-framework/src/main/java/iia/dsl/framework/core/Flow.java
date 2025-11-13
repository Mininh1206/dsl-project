package iia.dsl.framework.core;

import java.util.ArrayList;
import java.util.List;

import iia.dsl.framework.ports.Port;
import iia.dsl.framework.tasks.Task;

public class Flow extends Element {
    private final List<ExecutableElement> elements;
    
    public Flow(String id) {
        super(id);
        this.elements = new ArrayList<>();
    }
    
    public void addElement(ExecutableElement element) {
        if (element instanceof Port || element instanceof Task) {
            elements.add(element);
        } else {
            throw new IllegalArgumentException("Element must be a Port or Task");
        }

        elements.add(element);
    }
    
    public void execute() {
        try {
            System.out.println("Ejecutando Flow: " + id);
            
            for (ExecutableElement element : elements) {
                element.execute();
            }

            System.out.println("\nFlow completado exitosamente\n");
            
        } catch (Exception e) {
            System.err.println("Error en Flow: " + e.getMessage());
            throw new RuntimeException("Flow execution failed", e);
        }
    }
}
