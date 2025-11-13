package iia.dsl.framework.tasks;

import java.util.ArrayList;
import java.util.List;

import iia.dsl.framework.core.ExecutableElement;
import iia.dsl.framework.core.Slot;

public abstract class Task extends ExecutableElement {
    protected final List<Slot> inputSlots;
    protected final List<Slot> outputSlots;
    protected final TaskType type;
    
    public Task(String id, TaskType type) {
        super(id);
        this.inputSlots = new ArrayList<>();
        this.outputSlots = new ArrayList<>();
        this.type = type;
    }
    
    public final void addInputSlot(Slot slot) {
        inputSlots.add(slot);
    }
    
    public final void addOutputSlot(Slot slot) {
        outputSlots.add(slot);
    }
    
    public List<Slot> getInputSlots() {
        return inputSlots;
    }
    
    public List<Slot> getOutputSlots() {
        return outputSlots;
    }

    public String getType() {
        return this.getClass().getName();
    }
}
