package iia.dsl.framework.tasks.routers;

import javax.xml.xpath.XPathExpressionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.TestUtils;

public class FilterTest {
    
    @Test
    public void testFilterAcceptedDocument() throws Exception {
        // Arrange
        String xml = TestUtils.SAMPLE_XML;
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setMessage(new Message(doc));
        
        // Filtrar órdenes con más de 2 items
        Filter filter = new Filter("test-filter", inputSlot, outputSlot, 
                "count(/order/items/item) > 2");
        
        // Act
        filter.execute();
        
        // Assert
        assertFalse(outputSlot.hasMessage(), "Document should be filtered out as it has only 2 items");
    }
    
    @Test
    public void testFilterPassedDocument() throws Exception {
        // Arrange
        String xml = TestUtils.SAMPLE_XML;
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setMessage(new Message(doc));
        
        // Filtrar órdenes con al menos 1 item
        Filter filter = new Filter("test-filter", inputSlot, outputSlot, 
                "count(/order/items/item) >= 1");
        
        // Act
        filter.execute();
        
        // Assert
        assertTrue(outputSlot.hasMessage(), "Document should pass filter as it has items");
    }
    
    @Test
    public void testFilterWithInvalidXPath() {
        // Arrange
        String xml = TestUtils.SAMPLE_XML;
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setMessage(new Message(doc));
        
        Filter filter = new Filter("test-filter", inputSlot, outputSlot, "invalid xpath expression");
        
        // Act & Assert
        assertThrows(XPathExpressionException.class, () -> {
            filter.execute();
        });
    }
}