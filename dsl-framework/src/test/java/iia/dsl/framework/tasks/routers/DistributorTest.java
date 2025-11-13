package iia.dsl.framework.tasks.routers;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.TestUtils;

/**
 * Tests unitarios para Distributor.
 */
class DistributorTest {
    
    @Test
    public void testDistributesToMultipleSlots() throws Exception {
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        Slot input = new Slot("in");
        Slot output1 = new Slot("out1");
        Slot output2 = new Slot("out2");
        Slot output3 = new Slot("out3");
        
        Message original = new Message("msg-123", doc);
        input.setMessage(original);
        
        // XPath que evalúan a true (1.0) para el documento de ejemplo
        List<String> xpaths = Arrays.asList(
            "count(//order) = 1",                      // Hay 1 elemento order
            "count(//item) = 2",                       // Hay 2 items
            "count(//customer) = 1"                    // Existe 1 elemento customer
        );
        
        List<Slot> outputs = Arrays.asList(output1, output2, output3);
        
        Distributor distributor = new Distributor("dist-1", input, outputs, xpaths);
        distributor.execute();
        
        // Todos los slots deberían recibir el mensaje porque todas las condiciones son verdaderas
        Message msg1 = output1.getMessage();
        Message msg2 = output2.getMessage();
        Message msg3 = output3.getMessage();
        
        assertNotNull(msg1, "Output1 should receive message");
        assertNotNull(msg2, "Output2 should receive message");
        assertNotNull(msg3, "Output3 should receive message");
        
        assertEquals("msg-123", msg1.getId());
        assertEquals("order", msg1.getDocument().getDocumentElement().getNodeName());
    }
    
    @Test
    public void testDistributesOnlyToMatchingSlots() throws Exception {
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        Slot input = new Slot("in");
        Slot output1 = new Slot("out1");
        Slot output2 = new Slot("out2");
        Slot output3 = new Slot("out3");
        
        input.setMessage(new Message("msg-456", doc));
        
        // Solo la primera expresión debería evaluar a true
        List<String> xpaths = Arrays.asList(
            "count(//order) = 1",                      // TRUE: Hay 1 elemento order
            "count(//item) = 5",                       // FALSE: No hay 5 items
            "count(//price[text()='500.00']) = 1"      // FALSE: No existe ese precio
        );
        
        List<Slot> outputs = Arrays.asList(output1, output2, output3);
        
        Distributor distributor = new Distributor("dist-2", input, outputs, xpaths);
        distributor.execute();
        
        // Solo el primer slot debería recibir el mensaje
        Message msg1 = output1.getMessage();
        Message msg2 = output2.getMessage();
        Message msg3 = output3.getMessage();
        
        assertNotNull(msg1, "Output1 should receive message when condition is true");
        assertNull(msg2, "Output2 should not receive message when condition is false");
        assertNull(msg3, "Output3 should not receive message when condition is false");
    }
    
    @Test
    public void testNoDistributionWhenNoConditionsMatch() throws Exception {
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        Slot input = new Slot("in");
        Slot output1 = new Slot("out1");
        Slot output2 = new Slot("out2");
        
        input.setMessage(new Message("msg-789", doc));
        
        // Ninguna expresión debería evaluar a true
        List<String> xpaths = Arrays.asList(
            "count(//order) = 10",          // FALSE
            "count(//item) = 100"           // FALSE
        );
        
        List<Slot> outputs = Arrays.asList(output1, output2);
        
        Distributor distributor = new Distributor("dist-3", input, outputs, xpaths);
        distributor.execute();
        
        // Ningún slot debería recibir el mensaje
        assertNull(output1.getMessage(), "Output1 should not receive message");
        assertNull(output2.getMessage(), "Output2 should not receive message");
    }
    
    @Test
    public void testThrowsExceptionWhenXPathAndOutputSizeMismatch() {
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        Slot input = new Slot("in");
        Slot output1 = new Slot("out1");
        Slot output2 = new Slot("out2");
        
        input.setMessage(new Message("msg-error", doc));
        
        // 3 xpaths pero solo 2 outputs
        List<String> xpaths = Arrays.asList(
            "count(//order) = 1",
            "count(//item) = 2",
            "count(//price) > 0"
        );
        
        List<Slot> outputs = Arrays.asList(output1, output2);
        
        Distributor distributor = new Distributor("dist-4", input, outputs, xpaths);
        
        Exception ex = assertThrows(Exception.class, () -> {
            distributor.execute();
        });
        
        assertTrue(ex.getMessage().contains("Los slots no son correctos"), 
                   "Exception should mention incorrect slots");
    }
}