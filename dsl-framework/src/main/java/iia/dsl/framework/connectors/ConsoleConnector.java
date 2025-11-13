package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

import iia.dsl.framework.util.DocumentUtil;

public class ConsoleConnector extends Connector {
    public ConsoleConnector(String id) {
        super(id);
    }
    
    public ConsoleConnector() {
        super();
    }
    
    @Override
    public Document call(Document input) {
        System.out.println("=== Output Document ===");
        System.out.println(DocumentUtil.documentToString(input));
        return input;
    }
}
