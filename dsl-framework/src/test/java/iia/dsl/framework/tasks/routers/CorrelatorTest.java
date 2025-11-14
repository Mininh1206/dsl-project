package iia.dsl.framework.tasks.routers;

import java.util.ArrayList;
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
 * Tests unitarios para Correlator.
 */
public class CorrelatorTest {

    @Test
    public void testCorrelatesMessagesWithSameId() throws Exception {
        // Crear documentos de prueba
        Document doc1 = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        Document doc2 = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        Document doc3 = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);

        // Crear slots de entrada y salida
        List<Slot> inputs = new ArrayList<>();
        List<Slot> outputs = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            inputs.add(new Slot("in-" + i));
            outputs.add(new Slot("out-" + i));
        }

        // Crear mensajes con el mismo correlation-id
        Message msg1 = new Message("msg-1", doc1);
        msg1.addHeader(Message.CORRELATION_ID, "100001");
        
        Message msg2 = new Message("msg-2", doc2);
        msg2.addHeader(Message.CORRELATION_ID, "100001");
        
        Message msg3 = new Message("msg-3", doc3);
        msg3.addHeader(Message.CORRELATION_ID, "100001");

        // Poner mensajes en los slots de entrada
        inputs.get(0).setMessage(msg1);
        inputs.get(1).setMessage(msg2);
        inputs.get(2).setMessage(msg3);

        // Crear y ejecutar correlator
        Correlator correlator = new Correlator("corr-1", inputs, outputs);
        correlator.execute();

        // Verificar que los mensajes salieron correctamente
        Message out0 = outputs.get(0).getMessage();
        Message out1 = outputs.get(1).getMessage();
        Message out2 = outputs.get(2).getMessage();
        
        assertNotNull(out0, "Output 0 should have a message");
        assertNotNull(out1, "Output 1 should have a message");
        assertNotNull(out2, "Output 2 should have a message");
        
        assertEquals("msg-1", out0.getId());
        assertEquals("msg-2", out1.getId());
        assertEquals("msg-3", out2.getId());
    }

    @Test
    public void testWaitsForAllMessagesBeforeSending() throws Exception {
        Document doc1 = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        Document doc2 = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);

        List<Slot> inputs = new ArrayList<>();
        List<Slot> outputs = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            inputs.add(new Slot("in-" + i));
            outputs.add(new Slot("out-" + i));
        }

        // Solo crear 2 mensajes de 3
        Message msg1 = new Message("msg-1", doc1);
        msg1.addHeader(Message.CORRELATION_ID, "000002");
        
        Message msg2 = new Message("msg-2", doc2);
        msg2.addHeader(Message.CORRELATION_ID, "000002");

        inputs.get(0).setMessage(msg1);
        inputs.get(1).setMessage(msg2);
        // inputs.get(2) no tiene mensaje

        Correlator correlator = new Correlator("corr-2", inputs, outputs);
        correlator.execute();

        // No debería enviar nada porque falta el tercer mensaje
        assertNull(outputs.get(0).getMessage(), "Output 0 should be null - waiting for all messages");
        assertNull(outputs.get(1).getMessage(), "Output 1 should be null - waiting for all messages");
        assertNull(outputs.get(2).getMessage(), "Output 2 should be null - waiting for all messages");
    }

    @Test
    public void testHandlesMultipleCorrelationGroups() throws Exception {
        Document doc1 = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        Document doc2 = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        Document doc3 = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);

        List<Slot> inputs = new ArrayList<>();
        List<Slot> outputs = new ArrayList<>();
        
        for (int i = 0; i < 2; i++) {
            inputs.add(new Slot("in-" + i));
            outputs.add(new Slot("out-" + i));
        }

        Correlator correlator = new Correlator("corr-3", inputs, outputs);

        // Primer grupo (incompleto)
        Message msg1 = new Message("msg-1", doc1);
        msg1.addHeader(Message.CORRELATION_ID, "000003");
        inputs.get(0).setMessage(msg1);
        correlator.execute();

        assertNull(outputs.get(0).getMessage(), "Should wait for second message");

        // Segundo grupo (completo)
        Message msg2 = new Message("msg-2", doc2);
        msg2.addHeader(Message.CORRELATION_ID, "000004");
        Message msg3 = new Message("msg-3", doc3);
        msg3.addHeader(Message.CORRELATION_ID, "000004");
        
        inputs.get(0).setMessage(msg2);
        inputs.get(1).setMessage(msg3);
        correlator.execute();

        // El segundo grupo debería salir
        Message out0 = outputs.get(0).getMessage();
        Message out1 = outputs.get(1).getMessage();
        
        assertNotNull(out0, "Second group should be sent");
        assertEquals("msg-2", out0.getId());
        assertEquals("msg-3", out1.getId());
    }

    @Test
    public void testThrowsWhenInsufficientSlots() {
        List<Slot> inputs = new ArrayList<>();
        List<Slot> outputs = new ArrayList<>();
        
        // Solo 1 slot (necesita al menos 2)
        inputs.add(new Slot("in-0"));
        outputs.add(new Slot("out-0"));

        Correlator correlator = new Correlator("corr-5", inputs, outputs);

        Exception ex = assertThrows(Exception.class, () -> {
            correlator.execute();
        });

        assertTrue(ex.getMessage().contains("slots no son correctos"), 
            "Exception should mention incorrect slots");
    }

    @Test
    public void testThrowsWhenMismatchedSlotCounts() {
        List<Slot> inputs = new ArrayList<>();
        List<Slot> outputs = new ArrayList<>();
        
        inputs.add(new Slot("in-0"));
        inputs.add(new Slot("in-1"));
        inputs.add(new Slot("in-2"));
        outputs.add(new Slot("out-0"));
        outputs.add(new Slot("out-1"));
        // Falta un output

        Correlator correlator = new Correlator("corr-6", inputs, outputs);

        Exception ex = assertThrows(Exception.class, () -> {
            correlator.execute();
        });

        assertTrue(ex.getMessage().contains("slots no son correctos"), 
            "Exception should mention incorrect slots");
    }

    @Test
    public void testCorrelatesWithXPath() throws Exception {
        // XML con orderId que usaremos como correlation-id
        String xml1 = """
            <?xml version="1.0" encoding="UTF-8"?>
            <context>
                <header>
                    <orderId>12345</orderId>
                </header>
            </context>
            """;
        
        String xml2 = """
            <?xml version="1.0" encoding="UTF-8"?>
            <context>
                <header>
                    <orderId>12345</orderId>
                </header>
            </context>
            """;

        Document doc1 = TestUtils.createXMLDocument(xml1);
        Document doc2 = TestUtils.createXMLDocument(xml2);

        List<Slot> inputs = new ArrayList<>();
        List<Slot> outputs = new ArrayList<>();
        
        for (int i = 0; i < 2; i++) {
            inputs.add(new Slot("in-" + i));
            outputs.add(new Slot("out-" + i));
        }

        // Mensajes con correlation-id en header (pero usaremos XPath)
        Message msg1 = new Message("msg-1", doc1);
        msg1.addHeader(Message.CORRELATION_ID, "999999"); // Este se ignora
        
        Message msg2 = new Message("msg-2", doc2);
        msg2.addHeader(Message.CORRELATION_ID, "999999"); // Este se ignora

        inputs.get(0).setMessage(msg1);
        inputs.get(1).setMessage(msg2);

        // Correlator con XPath
        Correlator correlator = new Correlator("corr-7", inputs, outputs, "//orderId");
        correlator.execute();

        // Deberían correlacionarse por el orderId (12345) del XML, no por el header
        Message out0 = outputs.get(0).getMessage();
        Message out1 = outputs.get(1).getMessage();
        
        assertNotNull(out0, "Output 0 should have a message");
        assertNotNull(out1, "Output 1 should have a message");
        assertEquals("msg-1", out0.getId());
        assertEquals("msg-2", out1.getId());
    }
}
