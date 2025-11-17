package iia.dsl.framework.ports;

import java.util.Optional;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.core.ExecutableElement;
/*
public abstract class Port extends ExecutableElement {
    protected final Connector connector;
    protected final Optional<String> xslt;
    
    public Port(Connector connector) {
        super();
        this.connector = connector;
        this.xslt = Optional.empty();
    }

    public Port(Connector connector, String xslt) {
        super();
        this.connector = connector;
        this.xslt = Optional.of(xslt);
    }

    public Port(String id, Connector connector) {
        super(id);
        this.connector = connector;
        this.xslt = Optional.empty();
    }
}
 */
public abstract class Port extends ExecutableElement {
    protected final Connector connector;
    protected final Optional<String> xslt;
    
    public Port(Connector connector) {
        super();
        this.connector = connector;
        this.xslt = Optional.empty();
    }

    public Port(Connector connector, String xslt) {
        super();
        this.connector = connector;
        this.xslt = Optional.of(xslt);
    }

    public Port(String id, Connector connector) {
        super(id);
        this.connector = connector;
        this.xslt = Optional.empty();
    }
}
