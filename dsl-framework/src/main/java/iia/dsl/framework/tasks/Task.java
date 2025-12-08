package iia.dsl.framework.tasks;

import java.util.ArrayList;
import java.util.List;

import iia.dsl.framework.core.ExecutableElement;
import iia.dsl.framework.core.Slot;

/**
 * Clase base abstracta para todas las tareas del framework.
 * 
 * Define la estructura fundamental de una tarea:
 * - Identificador único.
 * - Tipo de tarea (Modifier, Router, Transformer, etc.).
 * - Gestión de slots de entrada y salida.
 * 
 * Las subclases deben implementar el método execute() para definir la lógica
 * específica.
 */
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

    public void addOutputSlots(List<Slot> outputsSlot) {
        outputSlots.addAll(outputsSlot);
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
