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

public class FileConnector extends Connector {
    private final String filePath;
    private final int type;
    private final File file;

    private static final int READ = 0;
    private static final int WRITE = 1;
    
    public FileConnector(String id, String filePath, Document xml) {
        super(id);
        this.filePath = filePath;
        this.type = WRITE;
        this.file = new File(filePath);
    }

    public FileConnector(String id, String filePath) {
        super(id);
        this.filePath = filePath;
        this.type = READ;
        this.file = new File(filePath);
    }
    
    @Override
    public Document call(Document input) {
        switch (type) {
            case READ -> {
                try {
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    return doc;
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    throw new RuntimeException("Error reading file: " + filePath, e);
                }
            }
            case WRITE -> {
                // Write document to file
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
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        }
    }
}
