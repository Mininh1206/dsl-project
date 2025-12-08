package iia.dsl.framework.connectors;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.Port;
import iia.dsl.framework.ports.RequestPort;
import iia.dsl.framework.util.DocumentUtil;

/**
 * Connector que envía el contenido del documento recibido a la salida estándar
 * (System.out).
 * Útil para depuración y visualización del flujo de mensajes.
 * 
 * No requiere una estructura específica en el documento de entrada.
 */
public class ConsoleConnector extends Connector {
    public ConsoleConnector(Port port) {
        super(port);

        if (port instanceof InputPort || port instanceof RequestPort) {
            throw new IllegalArgumentException("ConsoleConnector no soporta InputPort o RequestPort");
        }
    }

    @Override
    public void execute() throws Exception {
        if (port instanceof OutputPort) {
            OutputPort outputPort = (OutputPort) port;
            System.out.println("=== Output Document ===");
            System.out.println(DocumentUtil.documentToString(outputPort.getDocument()));
        }
    }
}
