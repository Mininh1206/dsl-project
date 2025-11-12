package iia.dsl.framework.tasks.transformers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.TestUtils;

public class AggregatorTest {

	/**
	 * Caso feliz: agrega los elementos hijos del documento de entrada usando el wrapper por defecto.
	 * Arrange: crear un documento con dos elementos <record> y colocarlo en el slot de entrada.
	 * Act: ejecutar Aggregator con el wrapper por defecto.
	 * Assert: el documento de salida no es nulo, el root es "aggregated" y contiene ambos elementos.
	 */
	@Test
	public void testAggregatorAggregatesChildrenDefaultWrapper() throws Exception {
		String xml = """
				<?xml version="1.0" encoding="UTF-8"?>
				<batch>
					<record><value>A</value></record>
					<record><value>B</value></record>
				</batch>
				""";

		Document doc = TestUtils.createXMLDocument(xml);

		Slot input = new Slot("in");
		Slot output = new Slot("out");

		input.setDocument(doc);

		Aggregator agg = new Aggregator("agg1", input, output);
		agg.execute();

		Document result = output.getDocument();
		assertNotNull(result, "Output document should not be null");
		assertEquals("aggregated", result.getDocumentElement().getNodeName());

		NodeList records = result.getDocumentElement().getElementsByTagName("record");
		assertEquals(2, records.getLength(), "Should aggregate two record elements");
	}

	/**
	 * Verifica que el Aggregator usa el wrapper personalizado y preserva el id del mensaje.
	 * Arrange: documento simple, obtener el id generado por el slot de entrada.
	 * Act: ejecutar Aggregator con wrapper "combined".
	 * Assert: el documento de salida tiene root "combined" y el Message de salida mantiene el id original.
	 */
	@Test
	public void testAggregatorUsesCustomWrapperAndPreservesId() throws Exception {
		String xml = """
				<?xml version="1.0" encoding="UTF-8"?>
				<items>
					<item>1</item>
				</items>
				""";

		Document doc = TestUtils.createXMLDocument(xml);

		Slot input = new Slot("in2");
		Slot output = new Slot("out2");

		input.setDocument(doc);
		String originalId = input.getMessage().getId();

		Aggregator agg = new Aggregator("agg2", input, output, "combined");
		agg.execute();

		Document result = output.getDocument();
		assertNotNull(result);
		assertEquals("combined", result.getDocumentElement().getNodeName());

		// Message id preserved
		assertNotNull(output.getMessage());
		assertEquals(originalId, output.getMessage().getId());
	}

	/**
	 * Error: no hay Message en el slot de entrada.
	 * Arrange: crear un Slot vacío (sin setMessage ni setDocument).
	 * Act & Assert: ejecutar Aggregator y comprobar que lanza Exception con texto "No hay mensaje".
	 */
	@Test
	public void testAggregatorThrowsWhenNoInputMessage() {
		Slot input = new Slot("empty");
		Slot output = new Slot("out");

		Aggregator agg = new Aggregator("agg3", input, output);

		Exception ex = assertThrows(Exception.class, () -> agg.execute());
		assertTrue(ex.getMessage().contains("No hay mensaje"));
	}

	/**
	 * Error: el Message existe pero su Document es null.
	 * Arrange: setMessage con un Message cuyo document es null.
	 * Act & Assert: ejecutar Aggregator y comprobar que lanza Exception con texto "No hay documento".
	 */
	@Test
	public void testAggregatorThrowsWhenInputDocumentNull() {
		Slot input = new Slot("s");
		Slot output = new Slot("o");

		// Create a Message with null document and set it explicitly
		input.setMessage(new iia.dsl.framework.core.Message("mid-1", null));

		Aggregator agg = new Aggregator("agg4", input, output);

		Exception ex = assertThrows(Exception.class, () -> agg.execute());
		assertTrue(ex.getMessage().contains("No hay documento"));
	}
}
