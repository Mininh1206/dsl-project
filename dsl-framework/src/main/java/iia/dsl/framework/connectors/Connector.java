package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

import iia.dsl.framework.core.Element;

public class Connector extends Element {
    public Connector(String id) {
        super(id);
    }
    
    public Document call(Document input) {
        // Aquí iría la lógica de llamada al exterior
        return null;
    }
}
