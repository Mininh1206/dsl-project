package iia.dsl.framework.ports;

import org.w3c.dom.Document;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;

public class InputPort extends Port {
    private final Slot outputSlot;

    public InputPort(Connector connector, Slot outputSlot) {
        super(connector);
        this.outputSlot = outputSlot;
    }

    public InputPort(String id, Connector connector, Slot outputSlot) {
        super(id, connector);
        this.outputSlot = outputSlot;
    }
    
    @Override
    public void execute() throws Exception {
        // Delega al connector la ejecución
        connector.execute(this);
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
