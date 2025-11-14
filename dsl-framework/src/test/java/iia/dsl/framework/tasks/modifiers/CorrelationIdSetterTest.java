package iia.dsl.framework.tasks.modifiers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.TestUtils;

/**
 * Tests unitarios para CorrelationIdSetter.
 */
public class CorrelationIdSetterTest {

	@Test
	public void testAssignsIdWhenMessagePresent() throws Exception {
		Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);

		Slot input = new Slot("in");
		Slot output = new Slot("out");

		Message original = new Message("orig-123", doc);
		input.setMessage(original);

		CorrelationIdSetter setter = new CorrelationIdSetter("cis-1", input, output);
		setter.execute();

		Message out = output.getMessage();
		assertNotNull(out, "Output message must not be null");
		assertTrue(out.hasHeader("correlation-id"), "ID should be replaced with a generated correlation id");
		assertTrue(out.getHeader("correlation-id").matches("\\d{6}"), "Generated id must be 6 digits");
		assertNotNull(out.getDocument(), "Document should be preserved on the output message");
	}

	@Test
	public void testCreatesMessageWhenOnlyDocumentPresent() throws Exception {
		Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);

		Slot input = new Slot("in");
		Slot output = new Slot("out");
		
		input.setMessage(new Message(doc));

		CorrelationIdSetter setter = new CorrelationIdSetter("cis-2", input, output);
		setter.execute();

		Message out = output.getMessage();
		assertNotNull(out, "Output message must not be null when input had a document");
		assertNotNull(out.getHeader("correlation-id"), "Generated id must not be null");
		assertTrue(out.getHeader("correlation-id").matches("\\d{6}"), "Generated id must be 6 digits");
		assertNotNull(out.getDocument(), "Document should be set on the generated message");
		assertEquals("order", out.getDocument().getDocumentElement().getNodeName());
	}
}
