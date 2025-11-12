package iia.dsl.framework.tasks.modifiers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.TestUtils;

/**
 * Test unitario para ContextSlimmer.
 * 
 * Verifica que ContextSlimmer elimina correctamente nodos de contexto
 * usando expresiones XPath, incluyendo múltiples nodos simultáneamente.
 */
public class ContextSlimmerTest {

    @Test
    public void testContextSlimmerRemovesMultipleNodes() throws Exception {
        // Arrange - XML con múltiples items
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <order>
                    <header>
                        <orderId>12345</orderId>
                        <customer>John Doe</customer>
                    </header>
                    <context>
                        <routingInfo>temp-routing-data</routingInfo>
                        <timestamp>2025-11-07T10:00:00</timestamp>
                    </context>
                    <items>
                        <item>
                            <name>Product A</name>
                            <metadata>temporary</metadata>
                        </item>
                        <item>
                            <name>Product B</name>
                            <metadata>temporary</metadata>
                        </item>
                    </items>
                </order>
                """;
        
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        // Configurar ContextSlimmer para remover todos los nodos metadata
        ContextSlimmer slimmer = new ContextSlimmer("test-slimmer", 
            inputSlot, outputSlot, 
            "//metadata");
        
        // Act
        slimmer.execute();
        
        // Assert
        Document result = outputSlot.getDocument();
        assertNotNull(result, "Output document should not be null");
        
        NodeList metadataNodes = result.getElementsByTagName("metadata");
        assertEquals(0, metadataNodes.getLength(), 
            "All metadata nodes should be removed");
        
        // Verificar que otros nodos siguen presentes
        assertNotNull(result.getElementsByTagName("header").item(0));
        assertNotNull(result.getElementsByTagName("items").item(0));
        assertEquals(2, result.getElementsByTagName("item").getLength());
    }

    @Test
    public void testContextSlimmerRemovesContextSection() throws Exception {
        // Arrange - Documento con sección de contexto completa
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <message>
                    <header>
                        <id>MSG001</id>
                    </header>
                    <context>
                        <correlationId>abc-123</correlationId>
                        <returnAddress>queue://responses</returnAddress>
                        <routingSlip>step1,step2,step3</routingSlip>
                    </context>
                    <body>
                        <data>Important payload</data>
                    </body>
                </message>
                """;
        
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        // Remover toda la sección de contexto
        ContextSlimmer slimmer = new ContextSlimmer("test-slimmer", 
            inputSlot, outputSlot, 
            "/message/context");
        
        // Act
        slimmer.execute();
        
        // Assert
        Document result = outputSlot.getDocument();
        assertNotNull(result);
        
        assertNull(result.getElementsByTagName("context").item(0),
            "Context section should be removed");
        assertNull(result.getElementsByTagName("correlationId").item(0),
            "CorrelationId should be removed with context");
        
        // Verificar que header y body permanecen
        assertNotNull(result.getElementsByTagName("header").item(0));
        assertNotNull(result.getElementsByTagName("body").item(0));
        assertEquals("Important payload", 
            result.getElementsByTagName("data").item(0).getTextContent());
    }

    @Test
    public void testContextSlimmerWithNoMatchingNodes() throws Exception {
        // Arrange
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        // XPath que no coincide con ningún nodo
        ContextSlimmer slimmer = new ContextSlimmer("test-slimmer", 
            inputSlot, outputSlot, 
            "//nonexistent");
        
        // Act
        slimmer.execute();
        
        // Assert
        Document result = outputSlot.getDocument();
        assertNotNull(result);
        
        // El documento debe permanecer intacto
        assertEquals("order", result.getDocumentElement().getNodeName());
        assertNotNull(result.getElementsByTagName("header").item(0));
        assertNotNull(result.getElementsByTagName("items").item(0));
    }

    @Test
    public void testContextSlimmerWithNullDocument() {
        // Arrange
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        // No establecemos documento
        
        ContextSlimmer slimmer = new ContextSlimmer("test-slimmer", 
            inputSlot, outputSlot, 
            "//any");
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            slimmer.execute();
        });
        
        assertTrue(exception.getMessage().contains("No hay ningún documento"));
    }

    @Test
    public void testContextSlimmerWithInvalidXPath() {
        // Arrange
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setDocument(doc);
        
        ContextSlimmer slimmer = new ContextSlimmer("test-slimmer", 
            inputSlot, outputSlot, 
            "invalid xpath [[[");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            slimmer.execute();
        });
    }

    @Test
    public void testContextSlimmerInPipeline() throws Exception {
        // Arrange - Pipeline: Filter -> ContextSlimmer -> Translator
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <order>
                    <header>
                        <orderId>999</orderId>
                    </header>
                    <routingContext>
                        <step>intermediate</step>
                    </routingContext>
                    <items>
                        <item><name>Widget</name></item>
                        <item><name>Gadget</name></item>
                    </items>
                </order>
                """;
        
        Document doc = TestUtils.createXMLDocument(xml);
        
        Slot input = new Slot("input");
        Slot cleaned = new Slot("cleaned");
        
        input.setDocument(doc);
        
        // Limpiar el contexto de enrutamiento antes de procesar
        ContextSlimmer slimmer = new ContextSlimmer("pipeline-slimmer", 
            input, cleaned, 
            "/order/routingContext");
        
        // Act
        slimmer.execute();
        
        // Assert
        Document result = cleaned.getDocument();
        assertNotNull(result);
        
        assertNull(result.getElementsByTagName("routingContext").item(0),
            "Routing context should be removed");
        
        // Datos principales intactos
        assertEquals("999", 
            result.getElementsByTagName("orderId").item(0).getTextContent());
        assertEquals(2, result.getElementsByTagName("item").getLength());
    }
}
