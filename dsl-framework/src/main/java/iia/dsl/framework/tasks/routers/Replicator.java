package iia.dsl.framework.tasks.routers;

import iia.dsl.framework.Slot;
import iia.dsl.framework.Task;
import iia.dsl.framework.TaskType;
import org.w3c.dom.Document;
import java.util.List;

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
        Document d = inputSlots.get(0).getDocument();
        
        if (d == null) {
            throw new Exception("Replicator '" + id + "' no tiene documento para duplicar.");
        }

       
        for (Slot outputSlot : outputSlots) {
            Document docCopy = (Document) d.cloneNode(true);
            outputSlot.setDocument(docCopy);
            System.out.println("✓ Replicator '" + id + "' duplicó mensaje a slot: " + outputSlot.getId());
        }
    }
}