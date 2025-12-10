/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iia.dsl.framework.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utilidades estáticas para el manejo de documentos XML (DOM).
 * 
 * <p>
 * Provee métodos para:
 * <ul>
 * <li>Convertir Documentos a String (serialización).</li>
 * <li>Parsear Strings a Documentos.</li>
 * <li>Aplicar transformaciones XSLT.</li>
 * <li>Visualizar la estructura del árbol XML.</li>
 * </ul>
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
                    xmlContent.getBytes(StandardCharsets.UTF_8));

            return builder.parse(input);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException("Error creating XML document", e);
        }
    }

    public static Document applyXslt(Document doc, String xslt) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            StreamSource xsltSource = new StreamSource(new StringReader(xslt));
            Transformer transformer = factory.newTransformer(xsltSource);

            DOMSource source = new DOMSource(doc);
            DOMResult result = new DOMResult();

            transformer.transform(source, result);

            return (Document) result.getNode();
        } catch (TransformerException e) {
            throw new RuntimeException("Error applying XSLT transformation", e);
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
