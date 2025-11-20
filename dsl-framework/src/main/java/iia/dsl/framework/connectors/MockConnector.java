package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.Port;
import iia.dsl.framework.ports.RequestPort;

public class MockConnector extends Connector {
    private final Document mockDocument;
    
    public MockConnector(Port port, Document mockDocument) {
        super(port);
        this.mockDocument = mockDocument;
    }

    @Override
    public void execute() throws Exception {
        if (port == null) {
            throw new IllegalStateException("Port no asignado al MockConnector");
        }
        
        if (port instanceof InputPort inputPort) {
            inputPort.handleDocument(mockDocument);
        } else if (port instanceof OutputPort outputPort) {
            outputPort.getDocument();
        } else if (port instanceof RequestPort requestPort) {
            Document request = requestPort.getRequestDocument();
            if (request != null) {
                requestPort.handleResponse(mockDocument);
            }
        }
    }
}
