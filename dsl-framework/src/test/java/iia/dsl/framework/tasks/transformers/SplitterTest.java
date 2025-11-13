package iia.dsl.framework.tasks.transformers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.TestUtils;

/**
 * Test unitario para Splitter.
 * 
 * Verifica que Splitter divide correctamente un mensaje en múltiples fragmentos
 * usando expresiones XPath.
 */
public class SplitterTest {
    
    @Test
    public void testSplitsItemsIntoFragments() throws Exception {
        // Arrange
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        Slot input = new Slot("input");
        Slot output = new Slot("output");
        
        input.setMessage(new Message("split-123", doc));
        
        Splitter splitter = new Splitter("splitter-1", input, output, "/order/items/item");
        
        // Act
        splitter.execute();
        
        // Assert
        Message result = output.getMessage();
        assertNotNull(result, "Output message should not be null");
        
        // Verificar que tiene los headers de fragmentación
        assertTrue(result.hasHeader(Message.NUM_FRAG), "Should have NUM_FRAG header");
        assertTrue(result.hasHeader(Message.TOTAL_FRAG), "Should have TOTAL_FRAG header");
        
        // Verificar que el total de fragmentos es 2 (hay 2 items en SAMPLE_XML)
        assertEquals("2", result.getHeader(Message.TOTAL_FRAG), "Should have 2 total fragments");
        
        // Verificar que el último fragmento procesado es el 1 (índice 0 y 1)
        assertEquals("1", result.getHeader(Message.NUM_FRAG), "Last fragment should be number 1");
        
        // Verificar que el ID del mensaje original se mantiene
        assertEquals("split-123", result.getId(), "Message ID should be preserved");
    }
    
    @Test
    public void testThrowsWhenNoDocument() {
        // Arrange - Slot sin documento
        Slot input = new Slot("input");
        Slot output = new Slot("output");
        
        Splitter splitter = new Splitter("splitter-2", input, output, "/order/items/item");
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            splitter.execute();
        });
        
        assertTrue(exception.getMessage().contains("No hay ningun documento"));
    }
    
    @Test
    public void testSplitsWithCustomXPath() throws Exception {
        // Arrange - XML con múltiples elementos header
        String customXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <batch>
                    <record><id>1</id><data>First</data></record>
                    <record><id>2</id><data>Second</data></record>
                    <record><id>3</id><data>Third</data></record>
                </batch>
                """;
        
        Document doc = TestUtils.createXMLDocument(customXml);
        
        Slot input = new Slot("input");
        Slot output = new Slot("output");
        
        input.setMessage(new Message("batch-456", doc));
        
        Splitter splitter = new Splitter("splitter-3", input, output, "/batch/record");
        
        // Act
        splitter.execute();
        
        // Assert
        Message result = output.getMessage();
        assertNotNull(result, "Output message should not be null");
        assertEquals("3", result.getHeader(Message.TOTAL_FRAG), "Should have 3 total fragments");
        assertEquals("batch-456", result.getId(), "Message ID should be preserved");
    }
}
