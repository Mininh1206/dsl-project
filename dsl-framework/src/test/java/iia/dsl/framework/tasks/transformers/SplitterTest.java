package iia.dsl.framework.tasks.transformers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import iia.dsl.framework.Slot;
import iia.dsl.framework.util.TestUtils;

public class SplitterTest {
    
    private static final String SIMPLE_XSLT = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                <xsl:output method="xml" indent="yes"/>
                
                <xsl:template match="/order">
                    <summary>
                        <orderId><xsl:value-of select="header/orderId"/></orderId>
                        <customer><xsl:value-of select="header/customer"/></customer>
                        <itemCount><xsl:value-of select="count(items/item)"/></itemCount>
                    </summary>
                </xsl:template>
            </xsl:stylesheet>
            """;
    
    @Test
    public void testSplitterTransformsDocument() throws Exception {
        // Arrange
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        Splitter splitter = new Splitter("test-splitter", inputSlot, outputSlot, SIMPLE_XSLT);
        
        // Act
        splitter.execute();
        
        // Assert
        Document result = outputSlot.getDocument();
        assertNotNull(result, "Output document should not be null");
        
        assertEquals("summary", result.getDocumentElement().getNodeName(), 
                "Root element should be 'summary'");
        
        String orderId = result.getElementsByTagName("orderId").item(0).getTextContent();
        assertEquals("12345", orderId, "OrderId should be preserved");
        
        String itemCount = result.getElementsByTagName("itemCount").item(0).getTextContent();
        assertEquals("2", itemCount, "Item count should be 2");
    }
    
    @Test
    public void testSplitterWithNullDocument() {
        // Arrange
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");

        Splitter splitter = new Splitter("test-splitter", inputSlot, outputSlot, SIMPLE_XSLT);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            splitter.execute();
        });
        
        assertTrue(exception.getMessage().contains("No hay ningún documento"));
    }
    
    @Test
    public void testSplitterWithInvalidXSLT() {
        // Arrange
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        String invalidXslt = "<?xml version='1.0'?><invalid>not xslt</invalid>";
        Splitter splitter = new Splitter("test-splitter", inputSlot, outputSlot, invalidXslt);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            splitter.execute();
        });
    }
    
    @Test
    public void testSplitterIdentityTransform() throws Exception {
        // Arrange
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        // XSLT de identidad: copia todo tal cual
        String identityXslt = """
                <?xml version="1.0" encoding="UTF-8"?>
                <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                    <xsl:template match="@*|node()">
                        <xsl:copy>
                            <xsl:apply-templates select="@*|node()"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>
                """;

        Splitter splitter = new Splitter("test-splitter", inputSlot, outputSlot, identityXslt);

        // Act
        splitter.execute();
        
        // Assert
        Document result = outputSlot.getDocument();
        assertNotNull(result);
        assertEquals("order", result.getDocumentElement().getNodeName());
    }
}