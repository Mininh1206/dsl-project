package iia.dsl.framework.tasks.routers;

import java.util.Map;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

public class Distributor extends Task {
    private final Map<String, Slot> routingRules;
    private final Slot defaultOutputSlot; 

    public Distributor(String id, Slot inputSlot, Map<String, Slot> rules, Slot defaultSlot) {
        super(id, TaskType.ROUTER);
        
        addInputSlot(inputSlot);
        
        this.routingRules = rules;
        this.defaultOutputSlot = defaultSlot;

        rules.values().forEach(this::addOutputSlot);
        if (defaultSlot != null && !rules.containsValue(defaultSlot)) {
            addOutputSlot(defaultSlot);
        }
    }

    @Override
    public void execute() throws Exception {
        // TODO
    }
}