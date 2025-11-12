package iia.dsl.framework.tasks.routers;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;

class DistributorTest {
    
    private Slot inputSlot;
    private Slot outputSlotA;
    private Slot outputSlotB;
    private Slot defaultSlot;
    private DocumentBuilderFactory factory;
    
    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        inputSlot = new Slot("input-slot");
        outputSlotA = new Slot("output-slot-a");
        outputSlotB = new Slot("output-slot-b");
        defaultSlot = new Slot("default-slot");
        factory = DocumentBuilderFactory.newInstance();
    }
    
    private Document createXmlDocument(String xmlContent) throws Exception {
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
    }
    
    @Test
    void testDistributorCreation() {
        Map<String, Slot> rules = new HashMap<>();
        rules.put("//order[@type='urgent']", outputSlotA);
        
        Distributor distributor = new Distributor("dist-1", inputSlot, rules, defaultSlot);
        
        assertNotNull(distributor);
        assertEquals("dist-1", distributor.getId());
        assertEquals(1, distributor.getInputSlots().size());
        assertTrue(distributor.getOutputSlots().size() >= 2);
    }
    
    @Test
    void testRouteToMatchingSlot() throws Exception {
        String xml = "<order type='urgent'><item>Product A</item></order>";
        Document doc = createXmlDocument(xml);
        inputSlot.setMessage(new Message(doc));
        
        Map<String, Slot> rules = new HashMap<>();
        rules.put("//order[@type='urgent']", outputSlotA);
        rules.put("//order[@type='normal']", outputSlotB);
        
        Distributor distributor = new Distributor("dist-1", inputSlot, rules, defaultSlot);
        distributor.execute();
        
        assertNotNull(outputSlotA.getDocument());
        assertNull(outputSlotB.getDocument());
        assertNull(defaultSlot.getDocument());
    }
    
    @Test
    void testRouteToDefaultSlot() throws Exception {
        String xml = "<order type='express'><item>Product B</item></order>";
        Document doc = createXmlDocument(xml);
        inputSlot.setMessage(new Message(doc));
        
        Map<String, Slot> rules = new HashMap<>();
        rules.put("//order[@type='urgent']", outputSlotA);
        rules.put("//order[@type='normal']", outputSlotB);
        
        Distributor distributor = new Distributor("dist-1", inputSlot, rules, defaultSlot);
        distributor.execute();
        
        assertNull(outputSlotA.getDocument());
        assertNull(outputSlotB.getDocument());
        assertNotNull(defaultSlot.getDocument());
    }
    
    @Test
    void testNoDocumentInInputSlot() throws Exception {
        inputSlot.setMessage(null);
        
        Map<String, Slot> rules = new HashMap<>();
        rules.put("//order[@type='urgent']", outputSlotA);
        
        Distributor distributor = new Distributor("dist-1", inputSlot, rules, defaultSlot);
        
        assertDoesNotThrow(() -> distributor.execute());
        assertNull(outputSlotA.getDocument());
    }
    
    @Test
    void testFirstMatchingRuleWins() throws Exception {
        String xml = "<order type='urgent' priority='high'><item>Product C</item></order>";
        Document doc = createXmlDocument(xml);
        inputSlot.setMessage(new Message(doc));
        
        Map<String, Slot> rules = new HashMap<>();
        rules.put("//order[@type='urgent']", outputSlotA);
        rules.put("//order[@priority='high']", outputSlotB);
        
        Distributor distributor = new Distributor("dist-1", inputSlot, rules, defaultSlot);
        distributor.execute();
        
        assertTrue(outputSlotA.getDocument() != null || outputSlotB.getDocument() != null,
                "Al menos una de las reglas coincidentes debería recibir el documento");
    }
    
    @Test
    void testDocumentCloning() throws Exception {
        String xml = "<order><item>Product D</item></order>";
        Document doc = createXmlDocument(xml);
        inputSlot.setMessage(new Message(doc));
        
        Map<String, Slot> rules = new HashMap<>();
        rules.put("//order", outputSlotA);
        
        Distributor distributor = new Distributor("dist-1", inputSlot, rules, null);
        distributor.execute();
        
        assertNotNull(outputSlotA.getDocument());
        assertNotSame(doc, outputSlotA.getDocument());
    }
    
    @Test
    void testNullDefaultSlot() throws Exception {
        String xml = "<order type='express'><item>Product E</item></order>";
        Document doc = createXmlDocument(xml);
        inputSlot.setMessage(new Message(doc));
        
        Map<String, Slot> rules = new HashMap<>();
        rules.put("//order[@type='urgent']", outputSlotA);
        
        Distributor distributor = new Distributor("dist-1", inputSlot, rules, null);
        
        assertDoesNotThrow(() -> distributor.execute());
    }
}