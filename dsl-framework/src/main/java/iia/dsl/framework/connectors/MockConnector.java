package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

public class MockConnector extends Connector {
    private Document mockDocument;
    
    public MockConnector(String id, Document mockDocument) {
        super(id);
        this.mockDocument = mockDocument;
    }
    
    @Override
    public Document call(Document input) {
        return mockDocument;
    }
}
