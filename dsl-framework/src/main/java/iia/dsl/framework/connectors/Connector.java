package iia.dsl.framework.connectors;

import iia.dsl.framework.core.ExecutableElement;
import iia.dsl.framework.ports.Port;

public abstract class Connector extends ExecutableElement { 
    protected Port port;
    
    public Connector(String id, Port port) {
        super(id);
        this.port = port;
    }

    public Connector(Port port) {
        super();
        this.port = port;
    }
    
    /**
     * Ejecuta el connector con el port asociado.
     * El connector obtiene/envía datos y delega al port el manejo del documento.
     */
    @Override
    public abstract void execute() throws Exception;
}
