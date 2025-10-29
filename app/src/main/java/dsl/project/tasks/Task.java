package dsl.project.tasks;

import dsl.project.model.Slot;
import dsl.project.model.TaskType;

import java.util.ArrayList;
import java.util.List;

public abstract class Task {
    protected TaskType type;
    protected final List<Slot> inputSlots = new ArrayList<>();
    protected final List<Slot> outputSlots = new ArrayList<>();

    public Task(TaskType type) {
        this.type = type;
    }

    public TaskType getType() {
        return type;
    }

    public void addInputSlot(Slot s) {
        inputSlots.add(s);
    }

    public void addOutputSlot(Slot s) {
        outputSlots.add(s);
    }

    public List<Slot> getInputSlots() {
        return inputSlots;
    }

    public List<Slot> getOutputSlots() {
        return outputSlots;
    }

    public abstract void execute();
}
