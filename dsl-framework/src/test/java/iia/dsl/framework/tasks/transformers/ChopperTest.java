package iia.dsl.framework.tasks.transformers;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import iia.dsl.framework.Slot;
import iia.dsl.framework.util.TestUtils;

/**
 * Test unitario para Chopper.
 * 
 * Verifica que Chopper divide correctamente documentos grandes en fragmentos,
 * preservando metadatos y permitiendo el reensamblaje posterior.
 */
public class ChopperTest {

    @Test
    public void testChopperDividesOrderItems() throws Exception {
        // Arrange - Documento con múltiples items
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        // Dividir por cada item
        Chopper chopper = new Chopper("test-chopper", 
            inputSlot, outputSlot, 
            "/order/items/item",
            "itemFragment");
        
        // Act
        chopper.execute();
        
        // Assert
        // Como Slot solo guarda el último mensaje, verificamos que existe
        Document result = outputSlot.getDocument();
        assertNotNull(result, "Output should contain last fragment");
        
        // Verificar estructura del fragmento
        assertEquals("itemFragment", result.getDocumentElement().getNodeName(),
            "Fragment should be wrapped in itemFragment");
        
        // Verificar que contiene metadatos
        NodeList metadata = result.getElementsByTagName("choppedMetadata");
        assertEquals(1, metadata.getLength(), "Should contain chopped metadata");
        
        Element meta = (Element) metadata.item(0);
        assertNotNull(meta.getAttribute("fragmentIndex"));
        assertNotNull(meta.getAttribute("totalFragments"));
        assertEquals("2", meta.getAttribute("totalFragments"),
            "Should indicate 2 total fragments from sample XML");
    }

    @Test
    public void testChopperWithCustomWrapper() throws Exception {
        // Arrange
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <catalog>
                    <product>
                        <id>P001</id>
                        <name>Laptop</name>
                    </product>
                    <product>
                        <id>P002</id>
                        <name>Mouse</name>
                    </product>
                    <product>
                        <id>P003</id>
                        <name>Keyboard</name>
                    </product>
                </catalog>
                """;
        
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        // Usar wrapper personalizado
        Chopper chopper = new Chopper("test-chopper", 
            inputSlot, outputSlot, 
            "//product",
            "productMessage");
        
        // Act
        chopper.execute();
        
        // Assert
        Document result = outputSlot.getDocument();
        assertNotNull(result);
        
        assertEquals("productMessage", result.getDocumentElement().getNodeName(),
            "Should use custom wrapper name");
        
        // Verificar que contiene un elemento product
        NodeList products = result.getElementsByTagName("product");
        assertEquals(1, products.getLength(), 
            "Each fragment should contain one product");
    }

    @Test
    public void testChopperWithDefaultWrapper() throws Exception {
        // Arrange
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <batch>
                    <record><data>A</data></record>
                    <record><data>B</data></record>
                </batch>
                """;
        
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        // Usar constructor sin wrapper (default = "fragment")
        Chopper chopper = new Chopper("test-chopper", 
            inputSlot, outputSlot, 
            "//record");
        
        // Act
        chopper.execute();
        
        // Assert
        Document result = outputSlot.getDocument();
        assertNotNull(result);
        
        assertEquals("fragment", result.getDocumentElement().getNodeName(),
            "Should use default wrapper 'fragment'");
    }

    @Test
    public void testChopperPreservesFragmentMetadata() throws Exception {
        // Arrange
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <list>
                    <entry>First</entry>
                    <entry>Second</entry>
                    <entry>Third</entry>
                    <entry>Fourth</entry>
                    <entry>Fifth</entry>
                </list>
                """;
        
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        Chopper chopper = new Chopper("test-chopper", 
            inputSlot, outputSlot, 
            "//entry");
        
        // Act
        chopper.execute();
        
        // Assert - Verificar metadatos del último fragmento
        Document lastFragment = outputSlot.getDocument();
        assertNotNull(lastFragment);
        
        Element metadata = (Element) lastFragment.getElementsByTagName("choppedMetadata").item(0);
        assertNotNull(metadata, "Should contain metadata");
        
        assertEquals("4", metadata.getAttribute("fragmentIndex"),
            "Last fragment should be index 4 (0-based)");
        assertEquals("5", metadata.getAttribute("totalFragments"),
            "Should have 5 total fragments");
        
        String originalMsgId = metadata.getAttribute("originalMessageId");
        assertNotNull(originalMsgId, "Should preserve original message ID");
    }

    @Test
    public void testChopperWithNoMatchingNodes() throws Exception {
        // Arrange
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        // XPath que no coincide con ningún nodo
        Chopper chopper = new Chopper("test-chopper", 
            inputSlot, outputSlot, 
            "//nonexistent");
        
        // Act
        chopper.execute();
        
        // Assert
        // No se generan fragmentos, el output permanece como estaba
        // Como no se escribió nada nuevo, el slot podría estar vacío
        // Este test verifica que no lanza excepción con XPath sin coincidencias
    }

    @Test
    public void testChopperWithNullDocument() {
        // Arrange
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        // No establecemos documento
        
        Chopper chopper = new Chopper("test-chopper", 
            inputSlot, outputSlot, 
            "//any");
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            chopper.execute();
        });
        
        assertTrue(exception.getMessage().contains("No hay ningún documento"));
    }

    @Test
    public void testChopperInPipeline() throws Exception {
        // Arrange - Pipeline: Filter -> Chopper
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <orders>
                    <order>
                        <id>ORD001</id>
                        <item>Widget</item>
                    </order>
                    <order>
                        <id>ORD002</id>
                        <item>Gadget</item>
                    </order>
                    <order>
                        <id>ORD003</id>
                        <item>Doohickey</item>
                    </order>
                </orders>
                """;
        
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot input = new Slot("input");
        Slot chopped = new Slot("chopped");
        
        input.setDocument(doc);
        
        // Dividir cada order en su propio documento
        Chopper chopper = new Chopper("pipeline-chopper", 
            input, chopped, 
            "//order",
            "orderMessage");
        
        // Act
        chopper.execute();
        
        // Assert
        Document fragment = chopped.getDocument();
        assertNotNull(fragment);
        
        assertEquals("orderMessage", fragment.getDocumentElement().getNodeName());
        
        // Verificar que contiene un order
        NodeList orders = fragment.getElementsByTagName("order");
        assertEquals(1, orders.getLength(), 
            "Each fragment should contain exactly one order");
        
        // Verificar metadatos
        Element metadata = (Element) fragment.getElementsByTagName("choppedMetadata").item(0);
        assertEquals("3", metadata.getAttribute("totalFragments"),
            "Should have 3 fragments total");
    }
}
