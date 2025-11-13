package iia.dsl.framework.ports;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.core.ExecutableElement;

public abstract class Port extends ExecutableElement {
    protected Connector connector;
    
    public Port(Connector connector) {
        super();
        this.connector = connector;
    }

    public Port(String id, Connector connector) {
        super(id);
        this.connector = connector;
    }
}
