package iia.dsl.framework.tasks.modifiers;

import javax.xml.xpath.XPathExpressionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.TestUtils;

public class SlimmerTest {

    @Test
    public void testSlimmerRemovesNode() throws Exception {
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);

        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");
        
        inputSlot.setMessage(new Message(doc));

        // Configurar Slimmer para remover el header
        Slimmer slimmer = new Slimmer("test-slimmer", inputSlot, outputSlot, "/order/header");

        // Act
        slimmer.execute();

        // Assert
        Document result = outputSlot.getMessage().getDocument();
        assertNotNull(result, "Output document should not be null");

        Node headerNode = result.getElementsByTagName("header").item(0);
        assertNull(headerNode, "Header node should be removed");

        Node itemsNode = result.getElementsByTagName("items").item(0);
        assertNotNull(itemsNode, "Items node should still exist");
    }

    @Test
    public void testSlimmerWithInvalidXPath() {
        // Arrange
        String xml = TestUtils.SAMPLE_XML;
        Document doc = TestUtils.createXMLDocument(xml);

        Slot inputSlot = new Slot("input");
        Slot outputSlot = new Slot("output");

        inputSlot.setMessage(new Message(doc));

        Slimmer slimmer = new Slimmer("test-slimmer", inputSlot, outputSlot, "invalid xpath expression");

        // Act & Assert - Esperamos que lance excepción
        var exception = assertThrows(Exception.class, () -> {
            slimmer.execute();
        });

        assertTrue(exception instanceof  XPathExpressionException, 
            "Invalid XPath should throw XPathExpressionException");
    }
}
