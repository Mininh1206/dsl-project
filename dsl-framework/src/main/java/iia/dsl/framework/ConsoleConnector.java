/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iia.dsl.framework;

import iia.dsl.framework.util.DocumentUtil;
import org.w3c.dom.Document;

/**
 *
 * @author Daniel
 */
public class ConsoleConnector extends Connector {
    public ConsoleConnector(String id) {
        super(id);
    }
    
    @Override
    public Document call(Document input) {
        System.out.println("=== Output Document ===");
        System.out.println(DocumentUtil.documentToString(input));
        return input;
    }
}
