package iia.dsl.framework.connectors;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class FileConnector extends Connector {
    private final String filePath;
    
    public FileConnector(String id, String filePath) {
        super(id);
        this.filePath = filePath;
    }
    
    @Override
    public Document call(Document input) {
        // Lee XML desde archivo y lo convierte a Document
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new File(filePath));
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }
}
