package iia.dsl.framework.ports;

import org.w3c.dom.Document;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.DocumentUtil;

public class RequestPort extends Port {
    private final Slot inputSlot;
    private final Slot outputSlot;
    private Message currentMessage; // Guardar el mensaje actual

    public RequestPort(String id, Connector connector, Slot inputSlot, Slot outputSlot) {
        super(id, connector);
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        this.currentMessage = null;
    }

    public RequestPort(String id, Connector connector, Slot inputSlot, Slot outputSlot, String xslt) {
        super(connector, xslt);
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        this.currentMessage = null;
    }

    @Override
    public void execute() throws Exception {
        // Delega al connector la ejecución
        connector.execute(this);
    }
    
    /**
     * Obtiene el documento de request del slot de entrada.
     * Este método es llamado por el connector para obtener el documento a enviar.
     */
    public Document getRequestDocument() throws Exception {
        if (!inputSlot.hasMessage())
            return null;

        currentMessage = inputSlot.getMessage();
            
        if (!currentMessage.hasDocument())
            throw new Exception("No hay Documento en el slot de entrada para RequestPort '" + id + "'");

        return currentMessage.getDocument();
    }
    
    /**
     * Maneja el documento de respuesta recibido del connector.
     * Este método es llamado por el connector después de recibir la respuesta.
     */
    public void handleResponse(Document responseDoc) throws Exception {
        if (responseDoc == null)
            return;
        
        // Si hay una transformación XSLT definida, aplicarla al documento de respuesta
        if (xslt.isPresent()) {
            var xsltString = xslt.get();
            responseDoc = DocumentUtil.applyXslt(responseDoc, xsltString);
        }

        // Usar los headers del mensaje guardado
        if (currentMessage != null) {
            outputSlot.setMessage(new Message(responseDoc, currentMessage.getHeaders()));
        } else {
            outputSlot.setMessage(new Message(responseDoc));
        }
    }
    
    public Slot getInputSlot() {
        return inputSlot;
    }
    
    public Slot getOutputSlot() {
        return outputSlot;
    }
}
