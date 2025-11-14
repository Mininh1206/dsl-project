package iia.dsl.framework.tasks.transformers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.Storage;
import iia.dsl.framework.util.TestUtils;

public class AggregatorTest {
    
    private Storage storage;
    
    @BeforeEach
    public void setUp() {
        storage = Storage.getInstance();
    }
    
    @Test
    public void testAggregatesFragmentsSuccessfully() throws Exception {
        // Crear documento base sin items
        String baseXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <order>
                <header>
                    <orderId>12345</orderId>
                    <customer>John Doe</customer>
                </header>
                <items>
                </items>
            </order>
            """;
        
        Document baseDoc = TestUtils.createXMLDocument(baseXml);
        String messageId = "msg-001";
        
        // Almacenar documento base en Storage
        storage.storeDocument(messageId, baseDoc);
        
        // Crear fragmentos
        String fragment1Xml = """
            <item>
                <productId>P001</productId>
                <name>Laptop</name>
            </item>
            """;
        
        String fragment2Xml = """
            <item>
                <productId>P002</productId>
                <name>Mouse</name>
            </item>
            """;
        
        String fragment3Xml = """
            <item>
                <productId>P003</productId>
                <name>Keyboard</name>
            </item>
            """;
        
        Document frag1Doc = TestUtils.createXMLDocument(fragment1Xml);
        Document frag2Doc = TestUtils.createXMLDocument(fragment2Xml);
        Document frag3Doc = TestUtils.createXMLDocument(fragment3Xml);
        
        // Crear mensajes con fragmentos (usando solo el elemento raíz del documento)
        Message msg1 = new Message(messageId, frag1Doc);
        msg1.addHeader(Message.NUM_FRAG, "0");
        msg1.addHeader(Message.TOTAL_FRAG, "3");
        
        Message msg2 = new Message(messageId, frag2Doc);
        msg2.addHeader(Message.NUM_FRAG, "1");
        msg2.addHeader(Message.TOTAL_FRAG, "3");
        
        Message msg3 = new Message(messageId, frag3Doc);
        msg3.addHeader(Message.NUM_FRAG, "2");
        msg3.addHeader(Message.TOTAL_FRAG, "3");
        
        Slot input = new Slot("in");
        Slot output = new Slot("out");
        
        Aggregator aggregator = new Aggregator("agg-1", input, output, "//items");
        
        // Enviar primer fragmento
        input.setMessage(msg1);
        aggregator.execute();
        assertNull(output.getMessage(), "No debe haber mensaje de salida hasta tener todos los fragmentos");
        
        // Enviar segundo fragmento
        input.setMessage(msg2);
        aggregator.execute();
        assertNull(output.getMessage(), "No debe haber mensaje de salida hasta tener todos los fragmentos");
        
        // Enviar tercer fragmento
        input.setMessage(msg3);
        aggregator.execute();
        
        // Verificar resultado
        Message result = output.getMessage();
        assertNotNull(result, "Debe haber un mensaje de salida después de recibir todos los fragmentos");
        assertNotNull(result.getDocument(), "El mensaje debe tener un documento");
        
        NodeList items = result.getDocument().getElementsByTagName("item");
        assertEquals(3, items.getLength(), "El documento reconstruido debe tener 3 items");
        
        // Verificar que los items están presentes
        boolean hasP001 = false, hasP002 = false, hasP003 = false;
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String productId = item.getElementsByTagName("productId").item(0).getTextContent();
            if ("P001".equals(productId)) hasP001 = true;
            if ("P002".equals(productId)) hasP002 = true;
            if ("P003".equals(productId)) hasP003 = true;
        }
        
        assertTrue(hasP001, "Debe contener el producto P001");
        assertTrue(hasP002, "Debe contener el producto P002");
        assertTrue(hasP003, "Debe contener el producto P003");
    }
    
    @Test
    public void testThrowsWhenSlotEmpty() {
        Slot input = new Slot("in");
        Slot output = new Slot("out");
        
        Aggregator aggregator = new Aggregator("agg-2", input, output, "//items");
        
        Exception ex = assertThrows(Exception.class, () -> {
            aggregator.execute();
        });
        
        assertTrue(ex.getMessage().contains("No hay Mensaje"), "Debe lanzar excepción cuando no hay mensaje");
    }
    
    @Test
    public void testThrowsWhenNoDocument() {
        Slot input = new Slot("in");
        Slot output = new Slot("out");
        
        Message msg = new Message("msg-002", null);
        input.setMessage(msg);
        
        Aggregator aggregator = new Aggregator("agg-3", input, output, "//items");
        
        Exception ex = assertThrows(Exception.class, () -> {
            aggregator.execute();
        });
        
        assertTrue(ex.getMessage().contains("No hay Documento"), "Debe lanzar excepción cuando no hay documento");
    }
    
    @Test
    public void testThrowsWhenMissingHeaders() {
        String fragmentXml = "<item><productId>P001</productId></item>";
        Document fragDoc = TestUtils.createXMLDocument(fragmentXml);
        
        Slot input = new Slot("in");
        Slot output = new Slot("out");
        
        Message msg = new Message("msg-003", fragDoc);
        // No agregamos headers NUM_FRAG y TOTAL_FRAG
        input.setMessage(msg);
        
        Aggregator aggregator = new Aggregator("agg-4", input, output, "//items");
        
        Exception ex = assertThrows(Exception.class, () -> {
            aggregator.execute();
        });
        
        assertTrue(ex.getMessage().contains("headers necesarios"), 
            "Debe lanzar excepción cuando faltan headers de fragmentación");
    }
    
    @Test
    public void testThrowsWhenDocumentNotInStorage() throws Exception {
        String fragmentXml = "<item><productId>P001</productId></item>";
        Document fragDoc = TestUtils.createXMLDocument(fragmentXml);
        
        Slot input = new Slot("in");
        Slot output = new Slot("out");
        
        String messageId = "msg-004";
        Message msg1 = new Message(messageId, fragDoc);
        msg1.addHeader(Message.NUM_FRAG, "0");
        msg1.addHeader(Message.TOTAL_FRAG, "1");
        
        input.setMessage(msg1);
        
        Aggregator aggregator = new Aggregator("agg-5", input, output, "//items");
        
        Exception ex = assertThrows(Exception.class, () -> {
            aggregator.execute();
        });
        
        assertTrue(ex.getMessage().contains("No se encontró el documento original"), 
            "Debe lanzar excepción cuando no encuentra el documento en Storage");
    }
    
    @Test
    public void testFragmentsReceivedInDifferentOrder() throws Exception {
        // Crear documento base
        String baseXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <order>
                <items></items>
            </order>
            """;
        
        Document baseDoc = TestUtils.createXMLDocument(baseXml);
        String messageId = "msg-005";
        storage.storeDocument(messageId, baseDoc);
        
        // Crear fragmentos
        String fragment1Xml = "<item><id>1</id></item>";
        String fragment2Xml = "<item><id>2</id></item>";
        
        Document frag1Doc = TestUtils.createXMLDocument(fragment1Xml);
        Document frag2Doc = TestUtils.createXMLDocument(fragment2Xml);
        
        Message msg1 = new Message(messageId, frag1Doc);
        msg1.addHeader(Message.NUM_FRAG, "0");
        msg1.addHeader(Message.TOTAL_FRAG, "2");
        
        Message msg2 = new Message(messageId, frag2Doc);
        msg2.addHeader(Message.NUM_FRAG, "1");
        msg2.addHeader(Message.TOTAL_FRAG, "2");
        
        Slot input = new Slot("in");
        Slot output = new Slot("out");
        Aggregator aggregator = new Aggregator("agg-6", input, output, "//items");
        
        // Enviar fragmentos en orden inverso
        input.setMessage(msg2); // Fragmento 1 primero
        aggregator.execute();
        assertNull(output.getMessage(), "No debe haber salida con fragmento parcial");
        
        input.setMessage(msg1); // Fragmento 0 después
        aggregator.execute();
        
        Message result = output.getMessage();
        assertNotNull(result, "Debe reconstruir el mensaje independientemente del orden de llegada");
        
        NodeList items = result.getDocument().getElementsByTagName("item");
        assertEquals(2, items.getLength(), "Debe tener 2 items reconstruidos");
    }
}
