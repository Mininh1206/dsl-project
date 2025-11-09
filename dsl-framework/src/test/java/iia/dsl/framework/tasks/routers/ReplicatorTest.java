package iia.dsl.framework.tasks.routers;

import iia.dsl.framework.Slot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReplicatorTest {
    
    private Slot inputSlot;
    private Slot outputSlot1;
    private Slot outputSlot2;
    private Slot outputSlot3;
    private DocumentBuilderFactory factory;
    
    @BeforeEach
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
        inputSlot.setDocument(doc);
        
        List<Slot> outputSlots = List.of(outputSlot1, outputSlot2, outputSlot3);
        Replicator replicator = new Replicator("rep-1", inputSlot, outputSlots);
        
        replicator.execute();
        
        assertNotNull(outputSlot1.getDocument());
        assertNotNull(outputSlot2.getDocument());
        assertNotNull(outputSlot3.getDocument());
    }
    
    @Test
    void testDocumentsAreIndependentCopies() throws Exception {
        String xml = "<message><content>Original</content></message>";
        Document doc = createXmlDocument(xml);
        inputSlot.setDocument(doc);
        
        List<Slot> outputSlots = List.of(outputSlot1, outputSlot2);
        Replicator replicator = new Replicator("rep-1", inputSlot, outputSlots);
        
        replicator.execute();
        
        Document doc1 = outputSlot1.getDocument();
        Document doc2 = outputSlot2.getDocument();
        
        assertNotNull(doc1);
        assertNotNull(doc2);
        assertNotSame(doc1, doc2);
        assertNotSame(doc, doc1);
        assertNotSame(doc, doc2);
    }
    
    @Test
    void testThrowsExceptionWhenNoDocument() throws Exception {
        inputSlot.setDocument(null);
        
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
        inputSlot.setDocument(doc);
        
        List<Slot> outputSlots = List.of(outputSlot1);
        Replicator replicator = new Replicator("rep-1", inputSlot, outputSlots);
        
        replicator.execute();
        
        assertNotNull(outputSlot1.getDocument());
        assertNull(outputSlot2.getDocument());
    }
    
    @Test
    void testContentPreservationAfterReplication() throws Exception {
        String xml = "<order><id>12345</id><amount>100.50</amount></order>";
        Document doc = createXmlDocument(xml);
        inputSlot.setDocument(doc);
        
        List<Slot> outputSlots = List.of(outputSlot1, outputSlot2);
        Replicator replicator = new Replicator("rep-1", inputSlot, outputSlots);
        
        replicator.execute();
        
        Document replicatedDoc = outputSlot1.getDocument();
        assertNotNull(replicatedDoc);
        assertEquals("order", replicatedDoc.getDocumentElement().getNodeName());
    }
}