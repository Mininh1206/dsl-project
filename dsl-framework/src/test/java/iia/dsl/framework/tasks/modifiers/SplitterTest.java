package iia.dsl.framework.tasks.modifiers;

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
import iia.dsl.framework.tasks.transformers.Splitter;
import iia.dsl.framework.util.Storage;
import iia.dsl.framework.util.TestUtils;

/**
 * Tests unitarios para Splitter.
 */
public class SplitterTest {
	
	@Test
	public void testSplitsItemsCorrectly() throws Exception {
		Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
		
		Slot input = new Slot("in");
		Slot output = new Slot("out");
		
		Message original = new Message("msg-123", doc);
		input.setMessage(original);
		
		Splitter splitter = new Splitter("spl-1", input, output, "//item");
		splitter.execute();
		
		// El Splitter envía cada mensaje al slot usando setMessage (que hace add a la cola)
		// Verificamos que se generaron los mensajes esperados
		assertEquals(2, output.getMessageCount(), "Should have 2 messages in output slot");
		
		// Obtener el primer mensaje (poll retorna y elimina el primero)
		Message firstMsg = output.getMessage();
		assertNotNull(firstMsg, "First message should not be null");
		
		// Verificar que el primer mensaje tiene los headers correctos
		assertTrue(firstMsg.hasHeader(Message.NUM_FRAG), "Message should have NUM_FRAG header");
		assertTrue(firstMsg.hasHeader(Message.TOTAL_FRAG), "Message should have TOTAL_FRAG header");
		
		// Verificamos que indica 2 fragmentos totales (hay 2 items en SAMPLE_XML)
		assertEquals("2", firstMsg.getHeader(Message.TOTAL_FRAG), "Should have 2 total fragments");
		
		// El primer mensaje debería ser el fragmento 0
		assertEquals("0", firstMsg.getHeader(Message.NUM_FRAG), "First message should be fragment 0");
		
		// Verificar que el documento contiene solo un item
		Document fragDoc = firstMsg.getDocument();
		assertNotNull(fragDoc, "Fragment document should not be null");
		assertEquals("item", fragDoc.getDocumentElement().getNodeName(), "Root element should be 'item'");
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
	
	@Test
	public void testThrowsWhenNoDocument() {
		Slot input = new Slot("in");
		Slot output = new Slot("out");
		
		// No establecemos ningún mensaje en el input
		
		Splitter splitter = new Splitter("spl-3", input, output, "//item");
		
		Exception ex = assertThrows(Exception.class, () -> {
			splitter.execute();
		});
		
		assertTrue(ex.getMessage().contains("No hay ningun documento"), 
			"Exception should mention missing document");
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
}