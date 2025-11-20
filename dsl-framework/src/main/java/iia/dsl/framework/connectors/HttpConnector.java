package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.Port;
import iia.dsl.framework.ports.RequestPort;
import iia.dsl.framework.util.Method;

public class HttpConnector extends Connector {
    private final String url;
    private final Method method;
    
    public HttpConnector(Port port, String url, Method method) {
        super(port);
        this.url = url;
        this.method = method;
    }
    
    @Override
    public void execute() throws Exception {
        // TODO implement
    }
}
