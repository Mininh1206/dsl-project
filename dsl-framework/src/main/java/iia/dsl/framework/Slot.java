package iia.dsl.framework;

import org.w3c.dom.Document;

public class Slot extends Element {
    private Document document;
    
    public Slot(String id) {
        super(id);
    }
    
    public void setDocument(Document document) {
        this.document = document;
    }
    
    public Document getDocument() {
        return document;
    }
}