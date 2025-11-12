package iia.dsl.framework.ports;

import org.w3c.dom.Document;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.core.Slot;

public class InputPort extends Port {
    public InputPort(String id, Connector connector, Slot slot) {
        super(id, connector, slot);
    }
    
    @Override
    public void execute() {
        // 1. Llama al connector para obtener datos del exterior
        Document doc = connector.call(null);
        
        // 2. Coloca el documento en el slot para que las tasks lo procesen
        slot.setDocument(doc);
        
        System.out.println("InputPort '" + id + "' cargó documento en slot '" + slot.getId() + "'");
    }
}
