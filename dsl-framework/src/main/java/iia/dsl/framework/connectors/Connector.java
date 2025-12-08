package iia.dsl.framework.connectors;

import iia.dsl.framework.core.ExecutableElement;
import iia.dsl.framework.ports.Port;

public abstract class Connector extends ExecutableElement {
    protected Port port;

    public boolean isSource() {
        return port instanceof iia.dsl.framework.ports.InputPort;
    }

    public Connector(String id, Port port) {
        super(id);

        if (port == null) {
            throw new IllegalArgumentException("Port no puede ser null");
        }

        this.port = port;
        if (port instanceof iia.dsl.framework.ports.OutputPort) {
            ((iia.dsl.framework.ports.OutputPort) port).getInputSlot().addListener(this);
        } else if (port instanceof iia.dsl.framework.ports.RequestPort) {
            ((iia.dsl.framework.ports.RequestPort) port).getInputSlot().addListener(this);
        }
    }

    public Connector(Port port) {
        super();

        if (port == null) {
            throw new IllegalArgumentException("Port no puede ser null");
        }

        this.port = port;
        if (port instanceof iia.dsl.framework.ports.OutputPort) {
            ((iia.dsl.framework.ports.OutputPort) port).getInputSlot().addListener(this);
        } else if (port instanceof iia.dsl.framework.ports.RequestPort) {
            ((iia.dsl.framework.ports.RequestPort) port).getInputSlot().addListener(this);
        }
    }

    /**
     * Ejecuta el connector con el port asociado.
     * El connector obtiene/envía datos y delega al port el manejo del documento.
     */
    @Override
    public abstract void execute() throws Exception;
}
