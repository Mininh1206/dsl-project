package iia.dsl.framework.tasks.modifiers;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;


public class ContextEnricher extends Task {
    
    // Nodo para el contexto
    private static final String CONTEXT_NODE_NAME = "context";
    
    public ContextEnricher(String id, Slot input, Slot output) {
        super(id, TaskType.MODIFIER);
        
        if (input != null) addInputSlot(input);
        if (output != null) addOutputSlot(output);
    }

    @Override
    public void execute() throws Exception {
        // TODO
    }
}