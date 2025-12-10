package iia.dsl.framework.tasks.routers;

import java.util.List;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

/**
 * Tarea de enrutamiento que fusiona múltiples flujos de entrada en un único
 * flujo de salida.
 * 
 * <p>
 * Actúa como un embudo: recibe cualquier mensaje que llegue por cualquiera de
 * sus slots de entrada
 * y lo reenvía inmediatamente al slot de salida único. No modifica el contenido
 * del mensaje
 * ni garantiza un ordenamiento específico entre diferentes entradas (FCFS).
 */
public class Merger extends Task {

    /**
     * Constructor del Merger.
     * 
     * @param id         Identificador único de la tarea
     * @param inputSlots Lista de slots de entrada a fusionar
     * @param outputSlot Slot de salida donde se escribirán todos los mensajes
     */
    Merger(String id, List<Slot> inputSlots, Slot outputSlot) {
        super(id, TaskType.ROUTER);

        // Añadir todos los input slots
        for (Slot slot : inputSlots) {
            addInputSlot(slot);
        }

        // Un único output slot
        addOutputSlot(outputSlot);
    }

    /**
     * Procesa los mensajes pendientes en todas las entradas y los mueve a la
     * salida.
     * En ejecución concurrente, esto se invoca continuamente o bajo demanda.
     * 
     * @throws Exception Si ocurre un error al mover los mensajes.
     */
    @Override
    public void execute() throws Exception {
        Slot outputSlot = outputSlots.get(0);

        // Iterar sobre TODOS los inputSlots
        for (Slot inputSlot : inputSlots) {
            while (inputSlot.hasMessage()) {
                var msg = inputSlot.getMessage();

                if (msg.hasDocument()) {
                    outputSlot.setMessage(msg);
                } else {
                    throw new Exception("No hay Documento en el slot de entrada para Merger '" + id + "'");
                }
            }
        }
    }
}
