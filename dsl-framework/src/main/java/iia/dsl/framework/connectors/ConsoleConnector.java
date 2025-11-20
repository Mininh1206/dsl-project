package iia.dsl.framework.connectors;

import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.Port;
import iia.dsl.framework.util.DocumentUtil;

public class ConsoleConnector extends Connector {
    public ConsoleConnector(String id, Port port) {
        super(id, port);
    }
    
    public ConsoleConnector(Port port) {
        super(port);
    }
    
    @Override
    public void execute() throws Exception {
        if (port == null) {
            throw new IllegalStateException("Port no asignado al ConnectorConsole");
        }
        
        if (port instanceof OutputPort) {
            OutputPort outputPort = (OutputPort) port;
            System.out.println("=== Output Document ===");
            System.out.println(DocumentUtil.documentToString(outputPort.getDocument()));
        }
    }
}
