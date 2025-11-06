/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iia.dsl.framework;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 *
 * @author Daniel
 */
public class FileConnector extends Connector {
    private String filePath;
    
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
        } catch (Exception e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }
}
