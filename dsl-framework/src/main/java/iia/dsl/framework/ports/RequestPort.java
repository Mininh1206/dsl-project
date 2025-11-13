package iia.dsl.framework.ports;

import org.w3c.dom.Document;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;

public class RequestPort extends Port {
    private final Slot inputSlot;
    private final Slot outputSlot;

    public RequestPort(String id, Connector connector, Slot inputSlot, Slot outputSlot) {
        super(id, connector);
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
    }
    @Override
    public void execute() throws Exception {
        if (!inputSlot.hasMessage())
            throw new RuntimeException("No hay mensaje en el slot de entrada para RequestPort '" + id + "'");

        Document requestDoc = inputSlot.getMessage().getDocument();
        Document responseDoc = connector.call(requestDoc);
        outputSlot.setMessage(new Message(responseDoc));
    }
}
