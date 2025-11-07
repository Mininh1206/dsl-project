package iia.dsl.framework.tasks.routers;

import iia.dsl.framework.Message;
import iia.dsl.framework.Slot;
import iia.dsl.framework.Task;
import iia.dsl.framework.TaskType;
import java.util.List;
import org.w3c.dom.Document;

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
    public Merger(String id, List<Slot> inputSlots, Slot outputSlot) {
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
            // Leer documento del slot de entrada
            Document doc = inputSlot.getDocument();
            
            // Si hay documento, pasarlo al outputSlot
            if (doc != null) {
                // Intentar obtener el mensaje completo (con ID) si existe
                Message msg = inputSlot.getMessage();
                
                if (msg != null) {
                    // Pasar el mensaje completo (preserva ID)
                    outputSlot.setMessage(msg);
                } else {
                    // Fallback: crear mensaje nuevo con el documento
                    outputSlot.setDocument(doc);
                }
            }
        }
    }
}
