package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

import iia.dsl.framework.core.Element;
import iia.dsl.framework.ports.Port;

public abstract class Connector extends Element {
    public Connector(String id) {
        super(id);
    }

    public Connector() {
        super();
    }
    
    /**
     * Obtiene o envía datos del/al sistema externo.
     * @param input Documento de entrada (null para lectura, Document para escritura)
     * @return Documento obtenido (null si es escritura)
     */
    protected abstract Document call(Document input) throws Exception;
    
    /**
     * Ejecuta el port usando este connector.
     * El connector obtiene/envía datos y delega al port el manejo del documento.
     */
    public abstract void execute(Port port) throws Exception;
}
