package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.Port;
import iia.dsl.framework.ports.RequestPort;
import iia.dsl.framework.util.DocumentUtil;

public class ConsoleConnector extends Connector {
    public ConsoleConnector(String id) {
        super(id);
    }
    
    public ConsoleConnector() {
        super();
    }
    
    @Override
    protected Document call(Document input) {
        System.out.println("=== Output Document ===");
        System.out.println(DocumentUtil.documentToString(input));
        return input;
    }
    
    @Override
    public void execute(Port port) throws Exception {
        if (port instanceof InputPort) {
            // Para InputPort: leer de consola no implementado, retornar null
            System.out.println("ConsoleConnector no soporta lectura (InputPort)");
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
