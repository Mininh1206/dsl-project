/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iia.dsl.framework;

import java.io.File;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 *
 * @author Daniel
 */
public class FileWriterConnector extends Connector {
    private String outputPath;
    
    public FileWriterConnector(String id, String outputPath) {
        super(id);
        this.outputPath = outputPath;
    }
    
    @Override
    public Document call(Document input) {
        // Escribe el Document a archivo
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(
                new DOMSource(input), 
                new StreamResult(new File(outputPath))
            );
            return input; // Devuelve el mismo documento
        } catch (Exception e) {
            throw new RuntimeException("Error writing file: " + outputPath, e);
        }
    }
}
