package iia.dsl.framework.core;

import java.util.ArrayList;
import java.util.List;

import iia.dsl.framework.ports.Port;
import iia.dsl.framework.tasks.Task;

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
    
    public void addElement(ExecutableElement element) {
        if (element instanceof Port || element instanceof Task) {
            elements.add(element);
        } else {
            throw new IllegalArgumentException("Element must be a Port or Task");
        }
    }
    
    @Override
    public void execute() throws Exception {
        System.out.println("Ejecutando Flow: " + id);
        
        for (ExecutableElement element : elements) {
            element.execute();
        }

        System.out.println("\nFlow completado exitosamente\n");
    }
}
