package iia.dsl.framework.tasks.routers;

import java.util.List;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

/**
 * Tarea de enrutamiento que duplica (broadcast) un mensaje entrante hacia
 * <b>todas</b> sus salidas.
 * 
 * <p>
 * Cada slot de salida recibe una copia nueva del mensaje (objeto
 * {@code Message}), permitiendo
 * que flujos paralelos procesen la misma información de manera independiente.
 * Nota: El documento XML interno es compartido (referencia) a menos que se
 * modifique explícitamente después.
 */
public class Replicator extends Task {

    Replicator(String id, Slot inputSlot, List<Slot> outputSlots) {
        super(id, TaskType.ROUTER);

        addInputSlot(inputSlot);
        outputSlots.forEach(this::addOutputSlot);

        if (outputSlots.isEmpty()) {
            throw new IllegalArgumentException("Replicator debe tener al menos un slot de salida.");
        }
    }

    @Override
    public void execute() throws Exception {
        var in = inputSlots.get(0);

        while (in.hasMessage()) {
            var m = in.getMessage();

            if (!m.hasDocument()) {
                throw new Exception("Replicator '" + id + "' no tiene documento para duplicar.");
            }

            for (Slot outputSlot : outputSlots) {
                outputSlot.setMessage(new Message(m));
                // Clonar el documento para que cada salida tenga copia independiente??
                // Document clonedDoc = (Document) m.getDocument().cloneNode(true);
                // Message clonedMessage = new Message(m.getId(), clonedDoc, m.getHeaders());
                // outputSlot.setMessage(clonedMessage);
            }
        }
    }
}
