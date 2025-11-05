package iia.dsl.framework.tasks.routers;

import javax.xml.xpath.XPathExpressionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import iia.dsl.framework.Slot;
import iia.dsl.framework.util.TestUtils;

public class FilterTest {
    
    @Test
    public void testFilterAcceptedDocument() throws XPathExpressionException {
        // Arrange
        String xml = TestUtils.SAMPLE_XML;
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        // Filtrar órdenes con más de 2 items
        Filter filter = new Filter("test-filter", inputSlot, outputSlot, 
                "count(/order/items/item) > 2");
        
        // Act
        filter.execute();
        
        // Assert
        assertNull(outputSlot.getDocument(), "Document should be filtered out as it has only 2 items");
    }
    
    @Test
    public void testFilterPassedDocument() throws XPathExpressionException {
        // Arrange
        String xml = TestUtils.SAMPLE_XML;
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        // Filtrar órdenes con al menos 1 item
        Filter filter = new Filter("test-filter", inputSlot, outputSlot, 
                "count(/order/items/item) >= 1");
        
        // Act
        filter.execute();
        
        // Assert
        assertNotNull(outputSlot.getDocument(), "Document should pass filter as it has items");
    }
    
    @Test
    public void testFilterWithInvalidXPath() {
        // Arrange
        String xml = TestUtils.SAMPLE_XML;
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        Filter filter = new Filter("test-filter", inputSlot, outputSlot, "invalid xpath expression");
        
        // Act & Assert
        assertThrows(XPathExpressionException.class, () -> {
            filter.execute();
        });
    }
}