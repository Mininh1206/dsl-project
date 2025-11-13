package iia.dsl.framework.tasks.transformers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.Storage;
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
        
        // El primer mensaje debería ser el fragmento 0
		assertEquals("0", result.getHeader(Message.NUM_FRAG), "First message should be fragment 0");
        
        // Verificar que el ID del mensaje original se mantiene
        assertEquals("split-123", result.getId(), "Message ID should be preserved");

        // Verificar que el documento contiene solo un item
		Document fragDoc = result.getDocument();
		assertNotNull(fragDoc, "Fragment document should not be null");
		assertEquals("item", fragDoc.getDocumentElement().getNodeName(), "Root element should be 'item'");
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
        
        assertTrue(exception.getMessage().contains("No hay ningun documento"), 
        "Exception should mention missing document");
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

    @Test
	public void testHandlesEmptyXPathResult() throws Exception {
		Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
		
		Slot input = new Slot("in");
		Slot output = new Slot("out");
		
		Message original = new Message("msg-789", doc);
		input.setMessage(original);
		
		// XPath que no coincide con nada
		Splitter splitter = new Splitter("spl-4", input, output, "//nonexistent");
		
		// No debería lanzar excepción, simplemente no genera fragmentos
		splitter.execute();
		
		// El documento original debería guardarse en Storage
		Document stored = Storage.getInstance().retrieveDocument("msg-789");
		assertNotNull(stored, "Original document should be stored");
	}

    @Test
	public void testSplitCreatesIndividualDocuments() throws Exception {
		Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
		
		Slot input = new Slot("in");
		
		Message original = new Message("msg-456", doc);
		input.setMessage(original);
		
		// Necesitamos capturar manualmente los mensajes porque setMessage sobreescribe
		List<Message> capturedMessages = new ArrayList<>();
		Slot captureOutput = new Slot("capture") {
			@Override
			public void setMessage(Message m) {
				// Capturamos una copia del mensaje
				Message copy = new Message(m.getId(), m.getDocument());
				m.getHeaders().forEach((k, v) -> copy.addHeader(k, v));
				capturedMessages.add(copy);
				super.setMessage(m);
			}
		};
		
		Splitter capturingSplitter = new Splitter("spl-2", input, captureOutput, "//item");
		capturingSplitter.execute();
		
		// Verificar que se crearon 2 mensajes
		assertEquals(2, capturedMessages.size(), "Should create 2 messages from 2 items");
		
		// Verificar primer fragmento
		Message frag0 = capturedMessages.get(0);
		assertEquals("0", frag0.getHeader(Message.NUM_FRAG), "First fragment should have NUM_FRAG=0");
		assertEquals("2", frag0.getHeader(Message.TOTAL_FRAG), "First fragment should have TOTAL_FRAG=2");
		assertNotNull(frag0.getDocument(), "First fragment should have a document");
		
		// Verificar segundo fragmento
		Message frag1 = capturedMessages.get(1);
		assertEquals("1", frag1.getHeader(Message.NUM_FRAG), "Second fragment should have NUM_FRAG=1");
		assertEquals("2", frag1.getHeader(Message.TOTAL_FRAG), "Second fragment should have TOTAL_FRAG=2");
		assertNotNull(frag1.getDocument(), "Second fragment should have a document");
	}
}
