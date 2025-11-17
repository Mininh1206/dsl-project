package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

import iia.dsl.framework.core.ExecutableElement;
import iia.dsl.framework.ports.Port;

public abstract class Connector extends ExecutableElement {
    protected Port port;
    
    public Connector(String id) {
        super(id);
    }

    public Connector() {
        super();
    }
    
    /**
     * Asocia un port a este connector.
     */
    public void setPort(Port port) {
        this.port = port;
    }
    
    public Port getPort() {
        return port;
    }
    
    /**
     * Obtiene o envía datos del/al sistema externo.
     * @param input Documento de entrada (null para lectura, Document para escritura)
     * @return Documento obtenido (null si es escritura)
     */
    protected abstract Document call(Document input) throws Exception;
    
    /**
     * Ejecuta el connector con el port asociado.
     * El connector obtiene/envía datos y delega al port el manejo del documento.
     */
    @Override
    public abstract void execute() throws Exception;
}
