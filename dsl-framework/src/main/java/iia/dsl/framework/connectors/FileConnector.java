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
import iia.dsl.framework.ports.Port;

public class FileConnector extends Connector {

    private final String filePath;
    private final File file;

    public FileConnector(Port port, String filePath) {
        super(port);
        this.filePath = filePath;
        this.file = new File(filePath);
    }

    @Override
    public void execute() throws Exception {
        if (port == null) {
            throw new IllegalStateException("Port no asignado al FileConnector");
        }
        
        if (port instanceof InputPort inputPort) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(file);
                doc.getDocumentElement().normalize();
                inputPort.handleDocument(doc);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                throw new RuntimeException("Error reading file: " + filePath, e);
            }
        } else if (port instanceof OutputPort outputPort) {
            try {
                Document doc = outputPort.getDocument();
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);
            } catch (TransformerException e) {
                throw new RuntimeException("Error writing to file: " + filePath, e);
            }
        }
    }
}
