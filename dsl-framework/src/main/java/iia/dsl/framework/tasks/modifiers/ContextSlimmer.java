package iia.dsl.framework.tasks.modifiers;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

/**
 * ContextSlimmer Task - Modifier que elimina información de contexto innecesaria.
 * 
 * Similar a Slimmer, pero diseñado específicamente para eliminar metadatos,
 * información de contexto, headers temporales, o datos de enrutamiento que
 * ya no son necesarios en una etapa posterior del pipeline.
 * 
 * Puede eliminar múltiples nodos que coincidan con el XPath especificado.
 * 
 * @author Javi
 */
public class ContextSlimmer extends Task {
    private final String xpath;

    /**
     * Constructor del ContextSlimmer.
     * 
     * @param id Identificador único de la tarea
     * @param inputSlot Slot de entrada con el documento a procesar
     * @param outputSlot Slot de salida donde se escribirá el documento sin contexto
     * @param xpath Expresión XPath para identificar nodos de contexto a eliminar
     */
    public ContextSlimmer(String id, Slot inputSlot, Slot outputSlot, String xpath) {
        super(id, TaskType.MODIFIER);

        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);

        this.xpath = xpath;
    }
    
    @Override
    public void execute() throws Exception {
        // TODO
    }
}
