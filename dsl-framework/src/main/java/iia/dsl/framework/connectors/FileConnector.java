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
import iia.dsl.framework.ports.RequestPort;
import iia.dsl.framework.core.Slot;

/**
 * Conector para lectura y escritura en el sistema de archivos local.
 * Soporta operaciones tanto de entrada (Source) como de salida (Sink).
 * 
 * <ul>
 * <li><b>InputPort:</b> Lee un archivo XML (o todos los .xml de un directorio)
 * y genera mensajes.</li>
 * <li><b>OutputPort:</b> Escribe el contenido XML recibido en un archivo
 * destino.</li>
 * </ul>
 */
public class FileConnector extends Connector {

    private final String filePath;
    private final File file;

    @Override
    public void onMessageAvailable(Slot slot) {
        super.onMessageAvailable(slot);
    }

    /**
     * Constructor para FileConnector.
     * 
     * @param port     El puerto asociado (InputPort o OutputPort).
     * @param filePath Ruta absoluta o relativa al archivo o directorio.
     */
    public FileConnector(Port port, String filePath) {
        super(port);

        if (port instanceof RequestPort) {
            throw new IllegalArgumentException("FileConnector no soporta RequestPort");
        }

        this.filePath = filePath;
        this.file = new File(filePath);
    }

    /**
     * Ejecuta la operación de archivo según el tipo de puerto.
     * - Input: Parsea el archivo XML y notifica al puerto.
     * - Output: Transforma el DOM en texto y lo escribe en disco.
     * 
     * @throws Exception Si hay errores de E/S o de parseo XML.
     */
    @Override
    public void execute() throws Exception {
        if (port instanceof InputPort inputPort) {
            try {
                if (file.isDirectory()) {
                    File[] xmlFiles = file.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
                    if (xmlFiles != null) {
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        for (File xmlFile : xmlFiles) {
                            Document doc = dBuilder.parse(xmlFile);
                            doc.getDocumentElement().normalize();
                            inputPort.handleDocument(doc);
                        }
                    }
                } else {
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    inputPort.handleDocument(doc);
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                throw new RuntimeException("Error reading file(s) from: " + filePath, e);
            }
        } else if (port instanceof OutputPort outputPort) {
            try {
                Document doc = outputPort.getDocument();
                if (doc == null)
                    return;

                System.err.println("DEBUG: FileConnector ready to write.");
                System.err.println("DEBUG: Doc child nodes: " + doc.getChildNodes().getLength());
                if (doc.getDocumentElement() != null) {
                    System.err.println("DEBUG: Root Name: " + doc.getDocumentElement().getNodeName());
                } else {
                    System.err.println("DEBUG: Root is NULL!");
                }

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
