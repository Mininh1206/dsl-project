package iia.dsl.framework.tasks.routers;

import iia.dsl.framework.Slot;
import iia.dsl.framework.Task;
import iia.dsl.framework.TaskType;
import org.w3c.dom.Document;

public class Threader extends Task {
    
    private final Task asynchronousTask; 

    
    public Threader(String id, Slot inputSlot, Slot outputSlot, Task taskToExecute) {
        super(id, TaskType.ROUTER);
        
        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);
        this.asynchronousTask = taskToExecute;
    }

    @Override
    public void execute() throws Exception {
        Document d = inputSlots.get(0).getDocument();
        
        if (d == null) {
            throw new Exception("Threader '" + id + "' no tiene documento para procesar.");
        }
        
        final Document docCopy = (Document) d.cloneNode(true);
        
        Runnable asyncJob = () -> {
            try {
                Slot inputOfTask = asynchronousTask.getInputSlots().get(0);
                inputOfTask.setDocument(docCopy);
                
                asynchronousTask.execute();
                
                
            } catch (Exception e) {
                System.err.println("Hilo ASÍNCRONO FALLO para '" + id + "': " + e.getMessage());
            }
        };
        new Thread(asyncJob, "AsyncWorker-" + id).start();
        
        System.out.println("✓ Threader '" + id + "' regresó control al flujo principal.");
    }
}