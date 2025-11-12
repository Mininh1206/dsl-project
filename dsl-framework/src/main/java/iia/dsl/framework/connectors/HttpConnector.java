package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

public class HttpConnector extends Connector {
    private final String url;
    
    public HttpConnector(String id, String url) {
        super(id);
        this.url = url;
    }
    
    @Override
    public Document call(Document input) {
        // TODO implement
        return input;
    }
}
