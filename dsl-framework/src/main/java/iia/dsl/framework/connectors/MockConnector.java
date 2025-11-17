package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.RequestPort;

public class MockConnector extends Connector {
    private final Document mockDocument;
    
    public MockConnector(String id, Document mockDocument) {
        super(id);
        this.mockDocument = mockDocument;
    }

    public MockConnector(Document mockDocument) {
        super();
        this.mockDocument = mockDocument;
    }
    
    @Override
    protected Document call(Document input) {
        return mockDocument;
    }
    
    @Override
    public void execute() throws Exception {
        if (port == null) {
            throw new IllegalStateException("Port no asignado al MockConnector");
        }
        
        if (port instanceof InputPort) {
            InputPort inputPort = (InputPort) port;
            inputPort.handleDocument(mockDocument);
        } else if (port instanceof OutputPort) {
            OutputPort outputPort = (OutputPort) port;
            outputPort.getDocument();
            // Mock simplemente retorna sin hacer nada
        } else if (port instanceof RequestPort) {
            RequestPort requestPort = (RequestPort) port;
            Document request = requestPort.getRequestDocument();
            if (request != null) {
                requestPort.handleResponse(mockDocument);
            }
        }
    }
}
