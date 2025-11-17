package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.Port;
import iia.dsl.framework.ports.RequestPort;

public class HttpConnector extends Connector {
    private final String url;
    
    public HttpConnector(String id, String url) {
        super(id);
        this.url = url;
    }
    
    @Override
    protected Document call(Document input) {
        // TODO implement
        return input;
    }
    
    @Override
    public void execute(Port port) throws Exception {
        if (port instanceof InputPort) {
            InputPort inputPort = (InputPort) port;
            Document doc = call(null);
            inputPort.handleDocument(doc);
        } else if (port instanceof OutputPort) {
            OutputPort outputPort = (OutputPort) port;
            Document doc = outputPort.getDocument();
            if (doc != null) {
                call(doc);
            }
        } else if (port instanceof RequestPort) {
            RequestPort requestPort = (RequestPort) port;
            Document request = requestPort.getRequestDocument();
            if (request != null) {
                Document response = call(request);
                requestPort.handleResponse(response);
            }
        }
    }
}
