package iia.dsl.framework.tasks.routers;

import java.util.List;

import org.w3c.dom.Document;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

public class Replicator extends Task {

    public Replicator(String id, Slot inputSlot, List<Slot> outputSlots) {
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
        Document d = in.getDocument();
        
        if (d == null) {
            throw new Exception("Replicator '" + id + "' no tiene documento para duplicar.");
        }

       
        for (Slot outputSlot : outputSlots) {
            outputSlot.setMessage(new Message(in.getMessageId(), (Document) d.cloneNode(true)));
        }
    }
}