package iia.dsl.framework;

import org.w3c.dom.Document;

public class OutputPort extends Port {
    public OutputPort(String id, Connector connector, Slot slot) {
        super(id, connector, slot);
    }
    
    @Override
    public void execute() {
        // 1. Obtiene el documento procesado del slot
        Document doc = slot.getDocument();
        
        if (doc == null) {
            System.out.println("OutputPort '" + id + "' no tiene documento para enviar");
            return;
        }
        
        // 2. Llama al connector para enviar/guardar el documento
        connector.call(doc);
        
        System.out.println("OutputPort '" + id + "' envió documento desde slot '" + slot.getId() + "'");
    }
}