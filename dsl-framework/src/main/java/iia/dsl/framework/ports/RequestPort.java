package iia.dsl.framework.ports;

import org.w3c.dom.Document;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.DocumentUtil;

/**
 * Puerto bidireccional para operaciones de Petición-Respuesta
 * (Request-Response).
 * Combina la funcionalidad de input y output: toma un mensaje de un slot
 * (Request),
 * lo entrega al conector y espera una respuesta para colocarla en otro slot
 * (Response).
 */
public class RequestPort extends Port {
    private final Slot inputSlot;
    private final Slot outputSlot;
    private Message currentMessage; // Guardar el mensaje actual

    public RequestPort(String id, Slot inputSlot, Slot outputSlot) {
        super(id);
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        this.currentMessage = null;
    }

    public RequestPort(String id, Slot inputSlot, Slot outputSlot, String xslt) {
        super(id, xslt);
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        this.currentMessage = null;
    }

    /**
     * Obtiene el documento de solicitud (Request) del slot de entrada.
     * 
     * @return El documento XML de la solicitud.
     * @throws Exception Si el mensaje no contiene un documento válido o el slot
     *                   está vacío.
     */
    public Document getRequestDocument() throws Exception {
        if (!inputSlot.hasMessage())
            return null;

        currentMessage = inputSlot.getMessage();

        if (!currentMessage.hasDocument())
            throw new Exception("No hay Documento en el mensaje del slot de entrada para RequestPort '" + id + "'");

        return currentMessage.getDocument();
    }

    /**
     * Procesa la respuesta recibida del conector.
     * Aplica XSLT opcional y coloca el resultado en el slot de salida.
     * Preserva las cabeceras del mensaje original (Correlation ID, etc.).
     * 
     * @param responseDoc El documento de respuesta recibido.
     * @throws Exception Si ocurre un error de transformación.
     */
    public void handleResponse(Document responseDoc) throws Exception {
        if (responseDoc == null)
            return;

        // Si hay una transformación XSLT definida, aplicarla al documento de respuesta
        if (xslt.isPresent()) {
            var xsltString = xslt.get();
            responseDoc = DocumentUtil.applyXslt(responseDoc, xsltString);
        }

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
