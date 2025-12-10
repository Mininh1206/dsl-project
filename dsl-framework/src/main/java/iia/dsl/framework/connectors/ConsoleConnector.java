package iia.dsl.framework.connectors;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.Port;
import iia.dsl.framework.ports.RequestPort;
import iia.dsl.framework.util.DocumentUtil;

/**
 * Conector simple que imprime el contenido de los documentos XML recibidos en
 * la salida estándar (System.out).
 * Es útil principalmente para depuración (debug) y para visualizar el flujo de
 * datos sin persistirlos.
 * Solo puede asociarse a puertos de salida (OutputPort).
 */
public class ConsoleConnector extends Connector {
    /**
     * Constructor para ConsoleConnector.
     * 
     * @param port El puerto de salida a monitorear.
     * @throws IllegalArgumentException Si se intenta usar con InputPort o
     *                                  RequestPort.
     */
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
