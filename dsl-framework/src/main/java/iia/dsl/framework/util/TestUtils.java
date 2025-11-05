package iia.dsl.framework.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class TestUtils {
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
    
    public static final String SAMPLE_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <order>
                <header>
                    <orderId>12345</orderId>
                    <customer>John Doe</customer>
                    <date>2025-11-05</date>
                </header>
                <items>
                    <item>
                        <productId>P001</productId>
                        <name>Laptop</name>
                        <price>999.99</price>
                        <quantity>1</quantity>
                    </item>
                    <item>
                        <productId>P002</productId>
                        <name>Mouse</name>
                        <price>29.99</price>
                        <quantity>2</quantity>
                    </item>
                </items>
            </order>
            """;
}