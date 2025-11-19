package iia.dsl.framework.connectors;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import iia.dsl.framework.ports.InputPort;
import iia.dsl.framework.ports.OutputPort;
import iia.dsl.framework.ports.RequestPort;

public class FileConnector extends Connector {

    private final String filePath;
    private final File file;

    public FileConnector(String id, String filePath) {
        super(id);
        this.filePath = filePath;
        this.file = new File(filePath);
    }

    public FileConnector(String filePath) {
        super();
        this.filePath = filePath;
        this.file = new File(filePath);
    }

    @Override
    protected Document call(Document input) {
        if (input == null) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(file);
                doc.getDocumentElement().normalize();
                return doc;
            } catch (ParserConfigurationException | SAXException | IOException e) {
                throw new RuntimeException("Error reading file: " + filePath, e);
            }
        } else {
            // Write document to file
            // If this connector is acting as a RequestPort, treat the file as a
            // static response source: read and return its Document instead of
            // treating the call as a pure write. This mimics MockConnector's
            // behavior for request/response testing.
            if (this.port instanceof iia.dsl.framework.ports.RequestPort) {
                try {
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    return doc;
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    throw new RuntimeException("Error reading file (as response): " + filePath, e);
                }
            }

            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(input);
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);
                return null;
            } catch (TransformerException e) {
                throw new RuntimeException("Error writing to file: " + filePath, e);
            }
        }
    }

    /**
     * Lectura directa del documento desde el fichero (API pública).
     * Útil para depuración o uso fuera del flujo de `Port`.
     */
    public Document readDocument() {
        return call(null);
    }

    /**
     * Escritura directa del documento al fichero (API pública).
     * @return null
     */
    public void writeDocument(Document doc) {
        call(doc);
    }
    
    @Override
    public void execute() throws Exception {
        if (port == null) {
            throw new IllegalStateException("Port no asignado al FileConnector");
        }
        
        if (port instanceof InputPort) {
            InputPort inputPort = (InputPort) port;
            Document doc = call(null);
            inputPort.handleDocument(doc);
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
