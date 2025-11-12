package iia.dsl.framework.ports;

import org.w3c.dom.Document;

import iia.dsl.framework.connectors.Connector;
import iia.dsl.framework.core.Slot;

public class RequestPort extends Port {
    public RequestPort(String id, Connector connector, Slot slot) {
        super(id, connector, slot);
    }
    
    public Document call(Document request) {
        return connector.call(request);
    }
    
    @Override
    public void execute() {
        // Default: no-op
    }
}
