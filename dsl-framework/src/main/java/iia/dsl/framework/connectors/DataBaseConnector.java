package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

public class DataBaseConnector extends Connector {
    private final String connectionString;
    
    public DataBaseConnector(String connectionString) {
        super();
        this.connectionString = connectionString;
    }

    @Override
    public Document call(Document input) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
