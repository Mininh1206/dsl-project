package iia.dsl.framework.ports;

import org.w3c.dom.Document;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;

/**
 * Puerto de entrada que recibe datos desde un Connector y los inyecta en un
 * Slot.
 * Actúa como la fuente de mensajes para el flujo interno.
 */
public class InputPort extends Port {
    private final Slot outputSlot;

    public InputPort(Slot outputSlot) {
        super();
        this.outputSlot = outputSlot;
    }

    public InputPort(String id, Slot outputSlot) {
        super(id);
        this.outputSlot = outputSlot;
    }

    /**
     * Recibe un documento desde el mundo exterior (vía Connector) e inyecta un
     * nuevo mensaje
     * en el Slot de salida asociado.
     * 
     * @param doc El documento XML recibido.
     */
    public void handleDocument(Document doc) {
        System.out.println("InputPort '" + id + "' cargó documento en slot '" + outputSlot.getId() + "'");
        outputSlot.setMessage(new Message(doc));
    }

    public Slot getOutputSlot() {
        return outputSlot;
    }
}
