package iia.dsl.framework;

import org.w3c.dom.Document;
/**
 * Clase que encapsula Document con id para permitir identificación de splitter
 * @author javi
 */
public class Message {
    private String id;
    private Document document;
    
    public Message(String id, Document document) {
        this.id = id;
        this.document = document;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Document getDocument() {
        return document;
    }
    
    public void setDocument(Document document) {
        this.document = document;
    }
}