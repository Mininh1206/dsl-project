package iia.dsl.framework.tasks.modifiers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import iia.dsl.framework.core.Message;
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
    public void testRemovesNodeWhenValidXPathProvided() throws Exception {
        // Crear documento principal
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        // Crear mensaje de contexto con xpath para eliminar el nodo <date>
        String contextXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <context>
                    <xpath>/order/header/date</xpath>
                </context>
                """;
        Document contextDoc = TestUtils.createXMLDocument(contextXml);
        
        Slot input = new Slot("in");
        Slot context = new Slot("context");
        Slot output = new Slot("out");
        
        Message mainMessage = new Message("msg-1", doc);
        Message contextMessage = new Message("ctx-1", contextDoc);
        
        input.setMessage(mainMessage);
        context.setMessage(contextMessage);
        
        ContextSlimmer slimmer = new ContextSlimmer("slimmer-1", input, context, output);
        slimmer.execute();
        
        Message result = output.getMessage();
        assertNotNull(result, "Output message must not be null");
        assertNotNull(result.getDocument(), "Output document must not be null");
        
        // Verificar que el nodo <date> fue eliminado
        NodeList dateNodes = result.getDocument().getElementsByTagName("date");
        assertEquals(0, dateNodes.getLength(), "The <date> node should have been removed");
        
        // Verificar que otros nodos siguen presentes
        NodeList orderIdNodes = result.getDocument().getElementsByTagName("orderId");
        assertEquals(1, orderIdNodes.getLength(), "The <orderId> node should still be present");
    }
    
    @Test
    public void testRemovesComplexNode() throws Exception {
        // Crear documento principal
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        // Crear mensaje de contexto con xpath para eliminar todo el nodo <items>
        String contextXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <context>
                    <xpath>/order/items</xpath>
                </context>
                """;
        Document contextDoc = TestUtils.createXMLDocument(contextXml);
        
        Slot input = new Slot("in");
        Slot context = new Slot("context");
        Slot output = new Slot("out");
        
        input.setMessage(new Message("msg-2", doc));
        context.setMessage(new Message("ctx-2", contextDoc));
        
        ContextSlimmer slimmer = new ContextSlimmer("slimmer-2", input, context, output);
        slimmer.execute();
        
        Message result = output.getMessage();
        assertNotNull(result, "Output message must not be null");
        
        // Verificar que el nodo <items> completo fue eliminado
        NodeList itemsNodes = result.getDocument().getElementsByTagName("items");
        assertEquals(0, itemsNodes.getLength(), "The <items> node should have been removed");
        
        // Verificar que los <item> también desaparecieron
        NodeList itemNodes = result.getDocument().getElementsByTagName("item");
        assertEquals(0, itemNodes.getLength(), "All <item> nodes should have been removed with <items>");
        
        // Verificar que el header sigue presente
        NodeList headerNodes = result.getDocument().getElementsByTagName("header");
        assertEquals(1, headerNodes.getLength(), "The <header> node should still be present");
    }
    
    @Test
    public void testThrowsWhenXPathNodeNotFoundInContext() {
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        // Contexto sin el nodo /xpath (usando otro nombre de nodo raíz)
        String contextXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <context>
                    <wrongNode>/order/header/date</wrongNode>
                </context>
                """;
        Document contextDoc = TestUtils.createXMLDocument(contextXml);
        
        Slot input = new Slot("in");
        Slot context = new Slot("context");
        Slot output = new Slot("out");
        
        input.setMessage(new Message("msg-5", doc));
        context.setMessage(new Message("ctx-5", contextDoc));
        
        ContextSlimmer slimmer = new ContextSlimmer("slimmer-5", input, context, output);
        
        Exception ex = assertThrows(Exception.class, () -> {
            slimmer.execute();
        });
        
        assertTrue(ex.getMessage().contains("No se encontró el nodo de XPath en el mensaje de contexto"), 
                "Exception should mention missing XPath node in context");
    }
    
    @Test
    public void testThrowsWhenNodeToRemoveNotFound() {
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        // XPath que no existe en el documento principal
        String contextXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <context>
                    <xpath>/order/nonExistentNode</xpath>
                </context>
                """;
        Document contextDoc = TestUtils.createXMLDocument(contextXml);
        
        Slot input = new Slot("in");
        Slot context = new Slot("context");
        Slot output = new Slot("out");
        
        input.setMessage(new Message("msg-6", doc));
        context.setMessage(new Message("ctx-6", contextDoc));
        
        ContextSlimmer slimmer = new ContextSlimmer("slimmer-6", input, context, output);
        
        Exception ex = assertThrows(Exception.class, () -> {
            slimmer.execute();
        });
        
        assertTrue(ex.getMessage().contains("No se encontró el nodo a eliminar en el mensaje"), 
                "Exception should mention node to remove not found");
    }
}
