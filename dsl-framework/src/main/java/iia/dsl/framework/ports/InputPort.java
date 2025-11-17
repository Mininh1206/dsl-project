package iia.dsl.framework.ports;

import org.w3c.dom.Document;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;

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
     * Maneja el documento recibido del connector.
     * Este método es llamado por el connector después de obtener los datos.
     */
    public void handleDocument(Document doc) {
        outputSlot.setMessage(new Message(doc));
        System.out.println("InputPort '" + id + "' cargó documento en slot '" + outputSlot.getId() + "'");
    }
    
    public Slot getOutputSlot() {
        return outputSlot;
    }
}
