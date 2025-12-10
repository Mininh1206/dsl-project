package iia.dsl.framework.tasks.transformers;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;
import iia.dsl.framework.util.DocumentUtil;

/**
 * Tarea de transformación que modifica la estructura de un mensaje XML
 * aplicando una hoja de estilos XSLT.
 * 
 * <p>
 * Toma el documento del mensaje de entrada, le aplica la transformación XSLT
 * configurada,
 * y coloca el documento resultante en un nuevo mensaje en la salida.
 */
public class Translator extends Task {

    private final String xslt;

    Translator(String id, Slot inputSlot, Slot outputSlot, String xslt) {
        super(id, TaskType.MODIFIER);

        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);

        this.xslt = xslt;
    }

    @Override
    public void execute() throws Exception {
        var in = inputSlots.get(0);

        while (in.hasMessage()) {
            var m = in.getMessage();

            if (!m.hasDocument()) {
                throw new Exception("No hay Documento en el slot de entrada para Translator '" + id + "'");
            }

            var d = m.getDocument();

            var transformedDoc = DocumentUtil.applyXslt(d, xslt);

            outputSlots.get(0).setMessage(new Message(m.getId(), transformedDoc, m.getHeaders()));
        }
    }
}
