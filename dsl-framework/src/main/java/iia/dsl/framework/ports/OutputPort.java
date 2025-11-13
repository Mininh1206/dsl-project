package iia.dsl.framework.ports;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.core.Slot;

public class OutputPort extends Port {
    private final Slot inputSlot;

    public OutputPort(String id, Connector connector, Slot inputSlot) {
        super(id, connector);
        this.inputSlot = inputSlot;
    }
    
    @Override
    public void execute() throws Exception {
        if (!inputSlot.hasMessage())
            return;

        var m = inputSlot.getMessage();

        if (m.hasDocument()) {
            connector.call(m.getDocument());
            System.out.println("OutputPort '" + id + "' envió documento desde slot '" + inputSlot.getId() + "'");
        } else {
            System.out.println("OutputPort '" + id + "' no encontró documento en slot '" + inputSlot.getId() + "'");
        }
    }
}
