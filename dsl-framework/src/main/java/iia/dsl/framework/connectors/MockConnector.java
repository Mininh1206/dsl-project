package iia.dsl.framework.connectors;

import org.w3c.dom.Document;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.Port;
import iia.dsl.framework.ports.RequestPort;

/**
 * Conector simulado (Mock) para pruebas y prototipado.
 * Permite inyectar o devolver documentos estáticos sin depender de sistemas
 * externos reales.
 * 
 * <ul>
 * <li><b>Input:</b> Inyecta el documento mock al inicio del flujo.</li>
 * <li><b>Output/Request:</b> Responde inmediatamente con el documento mock
 * ignorando la entrada.</li>
 * </ul>
 */
public class MockConnector extends Connector {
    private final Document mockDocument;

    /**
     * Constructor para MockConnector.
     * 
     * @param port         El puerto asociado.
     * @param mockDocument El documento XML que servirá como entrada o respuesta
     *                     fija.
     */
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
