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
public class HttpConnector extends Connector {
    private String url;
    
    public HttpConnector(String id, String url) {
        super(id);
        this.url = url;
    }
    
    @Override
    public Document call(Document input) {
        // TODO implement
        return input;
    }
}
