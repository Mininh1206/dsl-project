package iia.dsl.framework.ports;

import org.w3c.dom.Document;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;

public class InputPort extends Port {
    private final Slot outputSlot;

    public InputPort(String id, Connector connector, Slot outputSlot) {
        super(id, connector);
        this.outputSlot = outputSlot;
    }
    
    @Override
    public void execute() throws Exception {
        // 1. Llama al connector para obtener datos del exterior
        Document doc = connector.call(null);
        
        // 2. Coloca el documento en el slot para que las tasks lo procesen
        outputSlot.setMessage(new Message(doc));
        
        System.out.println("InputPort '" + id + "' cargó documento en slot '" + outputSlot.getId() + "'");
    }
}
