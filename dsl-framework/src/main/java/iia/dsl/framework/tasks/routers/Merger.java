package iia.dsl.framework.tasks.routers;

import java.util.List;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

/**
 * Merger Task - Router que combina múltiples flujos de entrada en uno de salida.
 * 
 * A diferencia del Aggregator (que combina contenidos), el Merger simplemente
 * fusiona/mezcla los mensajes de varios slots de entrada en uno de salida,
 * preservando los documentos sin modificar su contenido.
 * 
 * @author Javi
 */
public class Merger extends Task {
    
    /**
     * Constructor del Merger.
     * 
     * @param id Identificador único de la tarea
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
     * Ejecuta el merge de todos los documentos disponibles en los input slots.
     * 
     * Por cada llamada a execute(), procesa TODOS los documentos presentes
     * en TODOS los input slots y los pasa al output slot en orden.
     * 
     * @throws Exception si hay algún error durante el procesamiento
     */
    @Override
    public void execute() throws Exception {
        Slot outputSlot = outputSlots.get(0);
        
        // Iterar sobre TODOS los inputSlots
        for (Slot inputSlot : inputSlots) {
            if (inputSlot.hasMessage()) {
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
