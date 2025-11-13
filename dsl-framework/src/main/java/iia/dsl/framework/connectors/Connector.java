package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

import iia.dsl.framework.core.Element;

public abstract class Connector extends Element {
    public Connector(String id) {
        super(id);
    }

    public Connector() {
        super();
    }
    
    public abstract Document call(Document input) throws Exception;
}
