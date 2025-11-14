package iia.dsl.framework.tasks.routers;

import java.util.List;

import org.w3c.dom.Document;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

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

            // Validar que el mensaje no sea nulo y tenga documento
            if (m == null || !m.hasDocument()) {
                throw new Exception("Replicator '" + id + "' no tiene documento para duplicar.");
            }

            // Clonar el documento para cada salida para asegurar copias independientes
            for (Slot outputSlot : outputSlots) {
                Document clonedDoc = (Document) m.getDocument().cloneNode(true);
                Message clonedMessage = new Message(m.getId(), clonedDoc, m.getHeaders());
                outputSlot.setMessage(clonedMessage);
            }
        }
    }
}
