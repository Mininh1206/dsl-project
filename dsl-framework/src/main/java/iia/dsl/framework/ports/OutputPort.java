package iia.dsl.framework.ports;

import org.w3c.dom.Document;

import iia.dsl.framework.core.Slot;

import iia.dsl.framework.util.DocumentUtil;

/**
 * Puerto de salida que extrae mensajes de un Slot y los entrega a un Connector.
 * Permite la exportación de resultados del flujo hacia sistemas externos.
 * Puede aplicar una transformación XSLT final antes de la entrega.
 */
public class OutputPort extends Port {
    private final Slot inputSlot;

    public OutputPort(String id, Slot inputSlot) {
        super(id);
        this.inputSlot = inputSlot;
    }

    public OutputPort(String id, Slot inputSlot, String xslt) {
        super(id, xslt);
        this.inputSlot = inputSlot;
    }

    /**
     * Obtiene el documento disponible en el slot de entrada para ser procesado por
     * el Connector.
     * Si no hay mensaje, retorna null.
     * Aplica la transformación XSLT configurada si existe.
     * 
     * @return El documento XML (transformado o no) o null si el slot está vacío.
     */
    public Document getDocument() {
        if (!inputSlot.hasMessage()) {
            System.out.println("OutputPort '" + id + "' no tiene mensaje en slot '" + inputSlot.getId() + "'");
            return null;
        }

        var m = inputSlot.getMessage();

        if (m.hasDocument()) {
            System.out.println("OutputPort '" + id + "' obtuvo documento desde slot '" + inputSlot.getId() + "'");
            var doc = m.getDocument();
            if (xslt.isPresent()) {
                try {
                    return DocumentUtil.applyXslt(doc, xslt.get());
                } catch (Exception e) {
                    System.err.println("Error applying XSLT in OutputPort: " + e.getMessage());
                    return doc;
                }
            }
            return doc;
        } else {
            System.out.println("OutputPort '" + id + "' no encontró documento en slot '" + inputSlot.getId() + "'");
            return null;
        }
    }

    public Slot getInputSlot() {
        return inputSlot;
    }
}
