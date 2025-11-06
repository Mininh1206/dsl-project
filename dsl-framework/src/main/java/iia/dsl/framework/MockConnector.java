/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iia.dsl.framework;

import org.w3c.dom.Document;

/**
 *
 * @author Daniel
 */
public class MockConnector extends Connector {
    private Document mockDocument;
    
    public MockConnector(String id, Document mockDocument) {
        super(id);
        this.mockDocument = mockDocument;
    }
    
    @Override
    public Document call(Document input) {
        return mockDocument;
    }
}
