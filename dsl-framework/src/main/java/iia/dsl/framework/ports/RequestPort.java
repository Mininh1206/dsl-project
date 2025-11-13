package iia.dsl.framework.ports;

import org.w3c.dom.Document;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.DocumentUtil;

public class RequestPort extends Port {
    private final Slot inputSlot;
    private final Slot outputSlot;

    public RequestPort(String id, Connector connector, Slot inputSlot, Slot outputSlot) {
        super(id, connector);
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
    }

    public RequestPort(String id, Connector connector, Slot inputSlot, Slot outputSlot, String xslt) {
        super(connector, xslt);
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
    }

    @Override
    public void execute() throws Exception {
        if (!inputSlot.hasMessage())
            return;

        var m = inputSlot.getMessage();
            
        if (!m.hasDocument())
            throw new Exception("No hay Documento en el slot de entrada para RequestPort '" + id + "'");

        Document requestDoc = m.getDocument();
        Document responseDoc = connector.call(requestDoc);

        // Si hay una transformación XSLT definida, aplicarla al documento de respuesta
        if (xslt.isPresent()) {
            var xsltString = xslt.get();
            
            responseDoc = DocumentUtil.applyXslt(responseDoc, xsltString);
        }

        outputSlot.setMessage(new Message(responseDoc));
    }
}
