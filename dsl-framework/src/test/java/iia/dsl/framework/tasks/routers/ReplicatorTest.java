package iia.dsl.framework.tasks.routers;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;

class ReplicatorTest {
    
    private Slot inputSlot;
    private Slot outputSlot1;
    private Slot outputSlot2;
    private Slot outputSlot3;
    private DocumentBuilderFactory factory;
    
    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        inputSlot = new Slot("input-slot");
        outputSlot1 = new Slot("output-slot-1");
        outputSlot2 = new Slot("output-slot-2");
        outputSlot3 = new Slot("output-slot-3");
        factory = DocumentBuilderFactory.newInstance();
    }
    
    private Document createXmlDocument(String xmlContent) throws Exception {
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
    }
    
    @Test
    void testReplicatorCreation() {
        List<Slot> outputSlots = List.of(outputSlot1, outputSlot2);
        
        Replicator replicator = new Replicator("rep-1", inputSlot, outputSlots);
        
        assertNotNull(replicator);
        assertEquals("rep-1", replicator.getId());
        assertEquals(1, replicator.getInputSlots().size());
        assertEquals(2, replicator.getOutputSlots().size());
    }
    
    @Test
    void testThrowsExceptionWithEmptyOutputSlots() {
        List<Slot> emptyOutputSlots = new ArrayList<>();
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Replicator("rep-1", inputSlot, emptyOutputSlots);
        });
    }
    
    @Test
    void testReplicateToMultipleSlots() throws Exception {
        String xml = "<message><content>Test Message</content></message>";
        Document doc = createXmlDocument(xml);
        inputSlot.setMessage(new Message(doc));
        
        List<Slot> outputSlots = List.of(outputSlot1, outputSlot2, outputSlot3);
        Replicator replicator = new Replicator("rep-1", inputSlot, outputSlots);
        
        replicator.execute();
        
        assertTrue(outputSlot1.hasMessage());
        assertTrue(outputSlot2.hasMessage());
        assertTrue(outputSlot3.hasMessage());
    }
    
    @Test
    void testDocumentsAreIndependentCopies() throws Exception {
        String xml = "<message><content>Original</content></message>";
        Document doc = createXmlDocument(xml);
        inputSlot.setMessage(new Message(doc));
        
        List<Slot> outputSlots = List.of(outputSlot1, outputSlot2);
        Replicator replicator = new Replicator("rep-1", inputSlot, outputSlots);
        
        replicator.execute();
        
        Document doc1 = outputSlot1.getMessage().getDocument();
        Document doc2 = outputSlot2.getMessage().getDocument();
        
        assertNotNull(doc1);
        assertNotNull(doc2);
        assertNotSame(doc1, doc2);
        assertNotSame(doc, doc1);
        assertNotSame(doc, doc2);
    }
    
    @Test
    void testThrowsExceptionWhenNoDocument() throws Exception {
        inputSlot.setMessage(null);
        
        List<Slot> outputSlots = List.of(outputSlot1);
        Replicator replicator = new Replicator("rep-1", inputSlot, outputSlots);
        
        Exception exception = assertThrows(Exception.class, () -> {
            replicator.execute();
        });
        
        assertTrue(exception.getMessage().contains("no tiene documento para duplicar"));
    }
    
    @Test
    void testReplicateToSingleSlot() throws Exception {
        String xml = "<data><value>123</value></data>";
        Document doc = createXmlDocument(xml);
        inputSlot.setMessage(new Message(doc));
        
        List<Slot> outputSlots = List.of(outputSlot1);
        Replicator replicator = new Replicator("rep-1", inputSlot, outputSlots);
        
        replicator.execute();
        
        assertTrue(outputSlot1.hasMessage());
        assertFalse(outputSlot2.hasMessage());
    }
    
    @Test
    void testContentPreservationAfterReplication() throws Exception {
        String xml = "<order><id>12345</id><amount>100.50</amount></order>";
        Document doc = createXmlDocument(xml);
        inputSlot.setMessage(new Message(doc));
        
        List<Slot> outputSlots = List.of(outputSlot1, outputSlot2);
        Replicator replicator = new Replicator("rep-1", inputSlot, outputSlots);
        
        replicator.execute();
        
        assertTrue(outputSlot1.hasMessage());
        assertEquals("order", outputSlot1.getMessage().getDocument().getDocumentElement().getNodeName());
    }
}