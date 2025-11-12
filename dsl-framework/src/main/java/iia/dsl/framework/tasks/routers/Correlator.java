
package iia.dsl.framework.tasks.routers;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

/**
 * Correlator Task - Router que correlaciona mensajes de múltiples entradas.
 * 
 * Correlaciona los mensajes de sus múltiples entradas (normalmente usando un id)
 * y los saca al mismo tiempo por sus múltiples salidas.
 */
public class Correlator extends Task {

    private final String correlationXPath;

    /**
     * Constructor del Correlator.
     * 
     * @param id Identificador único de la tarea
     * @param inputSlot1 Primer slot de entrada
     * @param inputSlot2 Segundo slot de entrada
     * @param outputSlot1 Primer slot de salida
     * @param outputSlot2 Segundo slot de salida
     * @param correlationXPath XPath para extraer el valor de correlación
     */
    public Correlator(String id, Slot inputSlot1, Slot inputSlot2, Slot outputSlot1, Slot outputSlot2, String correlationXPath) {
        super(id, TaskType.ROUTER);
        
        addInputSlot(inputSlot1);
        addInputSlot(inputSlot2);
        addOutputSlot(outputSlot1);
        addOutputSlot(outputSlot2);
        
        this.correlationXPath = correlationXPath;
    }

    @Override
    public void execute() throws Exception {
        // TODO
    }
}
