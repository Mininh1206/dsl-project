/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iia.dsl.framework.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Daniel
 */
public class DocumentUtil {

    public static String documentToString(Document doc) {
        var firstNode = doc.getFirstChild();

        StringBuilder sb = new StringBuilder();
        sb.append(firstNode.getNodeName());

        sb.append(getTree(firstNode.getChildNodes(), 1));

        return sb.toString();
    }

    public static Document createXMLDocument(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            ByteArrayInputStream input = new ByteArrayInputStream(
                xmlContent.getBytes(StandardCharsets.UTF_8)
            );
            
            return builder.parse(input);
        } catch (Exception e) {
            throw new RuntimeException("Error creating XML document", e);
        }
    }
    
    private static String getTree(NodeList childs, int profundidad) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < childs.getLength(); i++) {
            var n = childs.item(i);

            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            sb.append("\n");

            for (int p = 1; p < profundidad; p++) {
                sb.append("│");
            }

            if (i == childs.getLength() - 1) {
                sb.append("└");
            } else if (profundidad != 0) {
                sb.append("├");
            }

            sb.append(n.getNodeName());

            sb.append(getTree(n.getChildNodes(), profundidad + 1));
        }

        return sb.toString();
    }
}
