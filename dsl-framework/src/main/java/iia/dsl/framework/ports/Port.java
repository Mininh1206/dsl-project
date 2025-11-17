package iia.dsl.framework.ports;

import java.util.Optional;

import iia.dsl.framework.core.Element;

public abstract class Port extends Element {
    protected final Optional<String> xslt;
    
    public Port() {
        super();
        this.xslt = Optional.empty();
    }

    public Port(String id, String xslt) {
        super(id);
        this.xslt = Optional.ofNullable(xslt);
    }

    public Port(String id) {
        super(id);
        this.xslt = Optional.empty();
    }
    
    public Optional<String> getXslt() {
        return xslt;
    }
}
