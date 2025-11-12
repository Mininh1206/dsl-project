package iia.dsl.framework.tasks.modifiers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;


public class ContextEnricher extends Task {
    
    // Nodo para el contexto
    private static final String CONTEXT_NODE_NAME = "context";
    
    public ContextEnricher(String id, Slot input, Slot output) {
        super(id, TaskType.MODIFIER);
        
        if (input != null) addInputSlot(input);
        if (output != null) addOutputSlot(output);
    }

    @Override
    public void execute() throws Exception {
        Slot in = inputSlots.get(0);
        Document d = in.getDocument();
        Message message = in.getMessage();

        if (d == null) {
            System.out.println("ContextEnricher '" + id + "' no tiene documento para enriquecer.");
            return; 
        }
        
     
        Document enrichedDoc = (Document) d.cloneNode(true);
        Element root = enrichedDoc.getDocumentElement();

        Element contextElement = enrichedDoc.createElement(CONTEXT_NODE_NAME);
        
        contextElement.setAttribute("enrichedBy", getClass().getSimpleName());
        contextElement.setAttribute("timestamp", String.valueOf(java.time.Instant.now()));
        
        if (message != null) {
             contextElement.setAttribute("messageId", message.getId()); 
        }
        
        root.appendChild(contextElement);
        
        for (Slot out : outputSlots) {
            out.setDocument(enrichedDoc);
            System.out.println("✓ ContextEnricher '" + id + "' enriqueció documento y escribió en slot: " + out.getId());
        }
    }
}