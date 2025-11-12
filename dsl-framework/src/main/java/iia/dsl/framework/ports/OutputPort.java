package iia.dsl.framework.ports;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.core.Slot;

public class OutputPort extends Port {
    public OutputPort(String id, Connector connector, Slot slot) {
        super(id, connector, slot);
    }
    
    @Override
    public void execute() {
        // Lógica de salida: tomar documento del slot y enviarlo mediante el connector
        if (slot.getDocument() != null) {
            connector.call(slot.getDocument());
            System.out.println("OutputPort '" + id + "' envió documento desde slot '" + slot.getId() + "'");
        } else {
            System.out.println("OutputPort '" + id + "' no encontró documento en slot '" + slot.getId() + "'");
        }
    }
}
