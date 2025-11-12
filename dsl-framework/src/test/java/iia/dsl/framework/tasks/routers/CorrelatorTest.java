package iia.dsl.framework.tasks.routers;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import iia.dsl.framework.core.Slot;

public class CorrelatorTest {

	private Document createDocument(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
	}

	/**
	 * Caso feliz: ambos documentos tienen el mismo valor de correlación (/order/id).
	 * Arrange: crear dos Document con el mismo id.
	 * Act: ejecutar Correlator.
	 * Assert: verificar que ambos slots de salida contienen documento con el id esperado.
	 */
	@Test
	public void testCorrelationMatch() throws Exception {
		String a = "<order><id>123</id><value>A</value></order>";
		String b = "<order><id>123</id><value>B</value></order>";

		Slot in1 = new Slot("in1");
		Slot in2 = new Slot("in2");
		Slot out1 = new Slot("out1");
		Slot out2 = new Slot("out2");

		in1.setDocument(createDocument(a));
		in2.setDocument(createDocument(b));

		Correlator c = new Correlator("corr1", in1, in2, out1, out2, "/order/id");
		c.execute();

		assertNotNull(out1.getDocument(), "Salida 1 debe contener documento cuando hay correlación");
		assertNotNull(out2.getDocument(), "Salida 2 debe contener documento cuando hay correlación");

		String idOut1 = out1.getDocument().getElementsByTagName("id").item(0).getTextContent();
		String idOut2 = out2.getDocument().getElementsByTagName("id").item(0).getTextContent();

		assertEquals("123", idOut1);
		assertEquals("123", idOut2);
	}

	/**
	 * No match: los documentos tienen ids distintos.
	 * Arrange: crear dos Document con ids diferentes.
	 * Act: ejecutar Correlator.
	 * Assert: las salidas deben permanecer vacías (no se envía nada).
	 */
	@Test
	public void testCorrelationNoMatch() throws Exception {
		String a = "<order><id>111</id></order>";
		String b = "<order><id>222</id></order>";

		Slot in1 = new Slot("in1");
		Slot in2 = new Slot("in2");
		Slot out1 = new Slot("out1");
		Slot out2 = new Slot("out2");

		in1.setDocument(createDocument(a));
		in2.setDocument(createDocument(b));

		Correlator c = new Correlator("corr2", in1, in2, out1, out2, "/order/id");
		c.execute();

		assertNull(out1.getDocument(), "Salida 1 debe seguir vacía cuando no hay correlación");
		assertNull(out2.getDocument(), "Salida 2 debe seguir vacía cuando no hay correlación");
	}

	/**
	 * Entrada nula: uno de los slots de entrada no tiene documento.
	 * Arrange: dejar el primer slot sin document y el segundo con uno válido.
	 * Act & Assert: al ejecutar debe lanzarse una excepción indicando falta de documento.
	 */
	@Test
	public void testNullInputDocumentThrows() throws Exception {
		String a = "<order><id>1</id></order>";

		Slot in1 = new Slot("in1");
		Slot in2 = new Slot("in2");
		Slot out1 = new Slot("out1");
		Slot out2 = new Slot("out2");

		// in1 left null
		in2.setDocument(createDocument(a));

		Correlator c = new Correlator("corr3", in1, in2, out1, out2, "/order/id");

		var exception = assertThrows(Exception.class, () -> c.execute(), "Debe lanzar excepción si falta documento en entradas");

		assertTrue(exception instanceof IllegalArgumentException, "Debe lanzar IllegalArgumentException si falta documento en entradas");
	}

	/**
	 * Insuficientes slots: simula que la tarea tiene menos de 2 entradas.
	 * Arrange: crear Correlator y eliminar uno de los input slots.
	 * Act & Assert: debe lanzar IllegalArgumentException por la validación previa.
	 */
	@Test
	public void testInsufficientSlotsThrows() throws Exception {
		String a = "<order><id>9</id></order>";
		String b = "<order><id>9</id></order>";

		Slot in1 = new Slot("in1");
		Slot in2 = new Slot("in2");
		Slot out1 = new Slot("out1");
		Slot out2 = new Slot("out2");

		in1.setDocument(createDocument(a));
		in2.setDocument(createDocument(b));

		Correlator c = new Correlator("corr4", in1, in2, out1, out2, "/order/id");

		// leave only one input slot to trigger the check
		c.getInputSlots().remove(1);

		assertThrows(IllegalArgumentException.class, () -> c.execute(), "Debe lanzar IllegalArgumentException si hay menos de 2 entradas/salidas");
	}
}
