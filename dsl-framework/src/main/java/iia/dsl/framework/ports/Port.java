package iia.dsl.framework.ports;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.core.Element;

public abstract class Port extends Element {
    protected Connector connector;
    
    public Port(String id, Connector connector) {
        super(id);
        this.connector = connector;
    }
    
    public abstract void execute();
}
