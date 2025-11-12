package iia.dsl.framework.tasks.modifiers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.TestUtils;

public class ContextEnricherTest {

    private Slot inputSlot;
    private Slot outputSlot;
    
    // XML simple para enriquecer
    private static final String ENRICH_SAMPLE_XML = "<report><data>value</data></report>";

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() throws Exception {
        Document doc = TestUtils.createXMLDocument(ENRICH_SAMPLE_XML);
        
        inputSlot = new Slot("input");
        inputSlot.setMessage(new Message(doc));

        outputSlot = new Slot("output");
    }
    
    // Se elimina el @AfterEach y cualquier referencia a la limpieza del Storage.

    @Test
    void testDocumentEnrichment() throws Exception {
        // Arrange
        ContextEnricher enricher = new ContextEnricher("test-enricher", inputSlot, outputSlot);

        // Act
        enricher.execute();

        // Assert 1: Verificar que el documento de salida no sea nulo
        Document resultDoc = outputSlot.getDocument();
        assertNotNull(resultDoc, "El slot de salida debe contener el documento enriquecido.");
        
        // Assert 2: Verificar que se haya añadido el nodo <context>
        NodeList contextNodes = resultDoc.getElementsByTagName("context");
        assertEquals(1, contextNodes.getLength(), "Debe haber exactamente un nodo <context> en el documento.");

        // Assert 3: Verificar que los metadatos técnicos se hayan añadido como atributos
        // Es crucial hacer el casting a Element para usar getAttribute()
        Element contextElement = (Element) contextNodes.item(0); 
        
        String enrichedBy = contextElement.getAttribute("enrichedBy");
        String messageId = contextElement.getAttribute("messageId");
        String timestamp = contextElement.getAttribute("timestamp");
        
        assertEquals("ContextEnricher", enrichedBy, "El atributo 'enrichedBy' debe ser 'ContextEnricher'.");
        assertEquals(inputSlot.getMessage().getId(), messageId, "El 'messageId' del contexto debe coincidir con el ID de entrada.");
        assertFalse(timestamp.isEmpty(), "El 'timestamp' no debe estar vacío.");
        
        // Assert 4: Verificar que el documento original no fue modificado (Inmutabilidad)
        NodeList originalContextNodes = inputSlot.getDocument().getElementsByTagName("context");
        assertEquals(0, originalContextNodes.getLength(), "El documento original en el inputSlot NO debe haber sido modificado.");
        
    }
}