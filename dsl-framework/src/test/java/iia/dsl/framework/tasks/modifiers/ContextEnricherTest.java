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
 * Tests unitarios para ContextEnricher.
 */
public class ContextEnricherTest {
    
    // Mensaje de contexto que indica dónde enriquecer (/xpath) y qué añadir (/body)
    private static final String CONTEXT_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <context>
                <xpath>/order/items</xpath>
                <body>
                    <item>
                        <productId>P003</productId>
                        <name>Keyboard</name>
                        <price>49.99</price>
                        <quantity>1</quantity>
                    </item>
                </body>
            </context>
            """;

    @Test
    public void testEnrichesMessageWithContext() throws Exception {
        // El mensaje principal es SAMPLE_XML que será enriquecido
        Document mainDoc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        Document contextDoc = TestUtils.createXMLDocument(CONTEXT_XML);

        Slot input = new Slot("in");
        Slot context = new Slot("context");
        Slot output = new Slot("out");

        Message mainMessage = new Message("msg-123", mainDoc);
        Message contextMessage = new Message("ctx-123", contextDoc);
        
        input.setMessage(mainMessage);
        context.setMessage(contextMessage);

        ContextEnricher enricher = new ContextEnricher("ce-1", input, context, output);
        enricher.execute();

        Message out = output.getMessage();
        assertNotNull(out, "Output message must not be null");
        assertNotNull(out.getDocument(), "Document should be present on the output message");
        
        // Verificar que el nodo /order/items ahora tiene más items (los 2 originales de SAMPLE_XML + 1 del contexto)
        NodeList items = out.getDocument().getElementsByTagName("item");
        assertEquals(3, items.getLength(), "Should have 2 original items from SAMPLE_XML plus 1 enriched item from context");
        
        // Verificar que el nuevo item está presente
        boolean keyboardFound = false;
        for (int i = 0; i < items.getLength(); i++) {
            var item = items.item(i);
            var children = item.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                if ("name".equals(children.item(j).getNodeName()) && 
                    "Keyboard".equals(children.item(j).getNodeValue())) {
                    keyboardFound = true;
                    break;
                }
            }
        }
        assertTrue(keyboardFound, "Enriched item (Keyboard) should be present in the output");
    }

    @Test
    public void testThrowsWhenInputSlotEmpty() {
        Slot input = new Slot("in");
        Slot context = new Slot("context");
        Slot output = new Slot("out");

        Document contextDoc = TestUtils.createXMLDocument(CONTEXT_XML);
        context.setMessage(new Message("ctx-123", contextDoc));

        ContextEnricher enricher = new ContextEnricher("ce-2", input, context, output);

        Exception ex = assertThrows(Exception.class, () -> {
            enricher.execute();
        });

        assertTrue(ex.getMessage().contains("No hay Mensaje"), "Exception should mention missing message");
    }

    @Test
    public void testThrowsWhenContextSlotEmpty() {
        Slot input = new Slot("in");
        Slot context = new Slot("context");
        Slot output = new Slot("out");

        Document mainDoc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        input.setMessage(new Message("msg-123", mainDoc));

        ContextEnricher enricher = new ContextEnricher("ce-3", input, context, output);

        Exception ex = assertThrows(Exception.class, () -> {
            enricher.execute();
        });

        assertTrue(ex.getMessage().contains("No hay Mensaje"), "Exception should mention missing message");
    }

    @Test
    public void testThrowsWhenInputDocumentMissing() throws Exception {
        Slot input = new Slot("in");
        Slot context = new Slot("context");
        Slot output = new Slot("out");

        Document contextDoc = TestUtils.createXMLDocument(CONTEXT_XML);
        
        input.setMessage(new Message("msg-123", null));
        context.setMessage(new Message("ctx-123", contextDoc));

        ContextEnricher enricher = new ContextEnricher("ce-4", input, context, output);

        Exception ex = assertThrows(Exception.class, () -> {
            enricher.execute();
        });

        assertTrue(ex.getMessage().contains("No hay Documento"), "Exception should mention missing document");
    }

    @Test
    public void testThrowsWhenXPathNodeNotFoundInContext() throws Exception {
        String contextWithoutXPath = """
                <?xml version="1.0" encoding="UTF-8"?>
                <context>
                    <body>
                        <item>
                            <productId>P003</productId>
                            <name>Keyboard</name>
                        </item>
                    </body>
                </context>
                """;
        
        Document mainDoc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        Document contextDoc = TestUtils.createXMLDocument(contextWithoutXPath);

        Slot input = new Slot("in");
        Slot context = new Slot("context");
        Slot output = new Slot("out");

        input.setMessage(new Message("msg-123", mainDoc));
        context.setMessage(new Message("ctx-123", contextDoc));

        ContextEnricher enricher = new ContextEnricher("ce-5", input, context, output);

        Exception ex = assertThrows(Exception.class, () -> {
            enricher.execute();
        });

        assertTrue(ex.getMessage().contains("No se encontró el nodo de XPath"), 
                   "Exception should mention missing XPath node in context");
    }
    
    @Test
    public void testThrowsWhenBodyNodeNotFoundInContext() throws Exception {
        // Mensaje de contexto sin nodo /body
        String contextWithoutBody = """
                <?xml version="1.0" encoding="UTF-8"?>
                <context>
                    <xpath>/order/items</xpath>
                </context>
                """;
        
        Document mainDoc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        Document contextDoc = TestUtils.createXMLDocument(contextWithoutBody);

        Slot input = new Slot("in");
        Slot context = new Slot("context");
        Slot output = new Slot("out");

        input.setMessage(new Message("msg-123", mainDoc));
        context.setMessage(new Message("ctx-123", contextDoc));

        ContextEnricher enricher = new ContextEnricher("ce-6", input, context, output);

        Exception ex = assertThrows(Exception.class, () -> {
            enricher.execute();
        });

        assertTrue(ex.getMessage().contains("No se encontró el nodo de cuerpo"), 
                   "Exception should mention missing body node in context");
    }
}