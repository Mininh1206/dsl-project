package iia.dsl.framework.tasks;

import java.util.ArrayList;
import java.util.List;

import iia.dsl.framework.core.ExecutableElement;
import iia.dsl.framework.core.Slot;

/**
 * Clase base abstracta para todas las tareas del framework.
 * 
 * Una Tarea es un elemento ejecutable que procesa mensajes.
 * Define la estructura fundamental:
 * - Identificador único.
 * - Tipo de tarea (Router, Transformer, Modifier).
 * - Slots de entrada (InputSlots) y salida (OutputSlots).
 * 
 * Las subclases deben implementar el método execute() para definir la lógica de
 * negocio.
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

    /**
     * Añade una lista de slots de salida a la tarea.
     * 
     * @param outputsSlot Lista de slots a añadir.
     */
    public void addOutputSlots(List<Slot> outputsSlot) {
        outputSlots.addAll(outputsSlot);
    }

    /**
     * Añade un slot de entrada y se suscribe como listener para recibir
     * notificaciones
     * de nuevos mensajes.
     * 
     * @param slot El slot de entrada.
     */
    public final void addInputSlot(Slot slot) {
        inputSlots.add(slot);
        slot.addListener(this);
    }

    /**
     * Añade un slot de salida a la tarea.
     * 
     * @param slot El slot de salida.
     */
    public final void addOutputSlot(Slot slot) {
        outputSlots.add(slot);
    }

    /**
     * Obtiene la lista de slots de entrada.
     * 
     * @return Lista de InputSlots.
     */
    public List<Slot> getInputSlots() {
        return inputSlots;
    }

    /**
     * Obtiene la lista de slots de salida.
     * 
     * @return Lista de OutputSlots.
     */
    public List<Slot> getOutputSlots() {
        return outputSlots;
    }

    /**
     * Obtiene el tipo de la tarea basado en su clase concreta.
     * 
     * @return El nombre de la clase como String.
     */
    public String getType() {
        return this.getClass().getName();
    }
}
