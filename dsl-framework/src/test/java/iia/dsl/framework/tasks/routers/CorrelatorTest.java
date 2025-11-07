package iia.dsl.framework.tasks.routers;

import javax.xml.xpath.XPathExpressionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import iia.dsl.framework.Slot;
import iia.dsl.framework.util.TestUtils;

public class CorrelatorTest {
    
    @Test
    public void testCorrelatorFilteredOutDocument() throws XPathExpressionException {
        // Arrange
        String xml = TestUtils.SAMPLE_XML;
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        // Correlator que acepta órdenes con más de 2 items (la muestra tiene 2 => false)
        Correlator correlator = new Correlator("test-correlator", inputSlot, outputSlot, 
                "count(/order/items/item) > 2");
        
        // Act
        correlator.execute();
        
        // Assert
        assertNull(outputSlot.getDocument(), "Document should be filtered out as it has only 2 items");
    }
    
    @Test
    public void testCorrelatorPassedDocument() throws XPathExpressionException {
        // Arrange
        String xml = TestUtils.SAMPLE_XML;
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        // Correlator que acepta órdenes con al menos 1 item (la muestra tiene 2 => true)
        Correlator correlator = new Correlator("test-correlator", inputSlot, outputSlot, 
                "count(/order/items/item) >= 1");
        
        // Act
        correlator.execute();
        
        // Assert
        assertNotNull(outputSlot.getDocument(), "Document should pass correlator as it has items");
    }
    
    @Test
    public void testCorrelatorWithInvalidXPath() {
        // Arrange
        String xml = TestUtils.SAMPLE_XML;
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        Correlator correlator = new Correlator("test-correlator", inputSlot, outputSlot, "invalid xpath expression");
        
        // Act & Assert
        assertThrows(XPathExpressionException.class, () -> {
            correlator.execute();
        });
    }
}