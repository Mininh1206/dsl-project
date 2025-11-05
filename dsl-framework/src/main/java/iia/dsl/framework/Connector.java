package iia.dsl.framework;

import org.w3c.dom.Document;

public class Connector extends Element {
    public Connector(String id) {
        super(id);
    }
    
    public Document call(Document input) {
        // Aquí iría la lógica de llamada al exterior
        return null;
    }
}