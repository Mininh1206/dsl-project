package iia.dsl.framework.ports;

import org.w3c.dom.Document;

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
        // Delega al connector la ejecución
        connector.execute(this);
    }
    
    /**
     * Obtiene el documento del slot de entrada para enviarlo.
     * Este método es llamado por el connector para obtener el documento a enviar.
     */
    public Document getDocument() {
        if (!inputSlot.hasMessage()) {
            System.out.println("OutputPort '" + id + "' no tiene mensaje en slot '" + inputSlot.getId() + "'");
            return null;
        }

        var m = inputSlot.getMessage();

        if (m.hasDocument()) {
            System.out.println("OutputPort '" + id + "' obtuvo documento desde slot '" + inputSlot.getId() + "'");
            return m.getDocument();
        } else {
            System.out.println("OutputPort '" + id + "' no encontró documento en slot '" + inputSlot.getId() + "'");
            return null;
        }
    }
    
    public Slot getInputSlot() {
        return inputSlot;
    }
}
