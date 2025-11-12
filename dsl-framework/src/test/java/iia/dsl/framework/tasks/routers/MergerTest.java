package iia.dsl.framework.tasks.routers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.util.TestUtils;

/**
 * Test unitario para la tarea Merger.
 * 
 * Merger combina múltiples flujos de entrada en uno de salida,
 * preservando los documentos sin modificar su contenido.
 */
public class MergerTest {
    
    @Test
    public void testMergerWithMultipleInputSlots() throws Exception {
        // Arrange - Crear documentos diferentes para cada input
        String xml1 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <message>
                    <source>Input1</source>
                    <content>First message</content>
                </message>
                """;
        
        String xml2 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <message>
                    <source>Input2</source>
                    <content>Second message</content>
                </message>
                """;
        
        String xml3 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <message>
                    <source>Input3</source>
                    <content>Third message</content>
                </message>
                """;
        
        Document doc1 = TestUtils.createXMLDocument(xml1);
        Document doc2 = TestUtils.createXMLDocument(xml2);
        Document doc3 = TestUtils.createXMLDocument(xml3);
        
        Slot input1 = new Slot("input1");
        Slot input2 = new Slot("input2");
        Slot input3 = new Slot("input3");
        Slot output = new Slot("output");
        
        input1.setDocument(doc1);
        input2.setDocument(doc2);
        input3.setDocument(doc3);
        
        Merger merger = new Merger("test-merger", 
            List.of(input1, input2, input3), 
            output);
        
        // Act
        merger.execute();
        
        Document result = output.getDocument();
        assertNotNull(result, "Output document should not be null");

        assertEquals(3, output.getMessageCount(), "Output should contain the three merged messages");
    }
    
    @Test
    public void testMergerWithEmptyInputSlots() throws Exception {
        // Arrange - Algunos slots vacíos, otros con contenido
        String xml1 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <message>
                    <id>MSG001</id>
                    <data>Valid data</data>
                </message>
                """;
        
        Document doc1 = TestUtils.createXMLDocument(xml1);
        
        Slot input1 = new Slot("input1");
        Slot input2 = new Slot("input2"); // Vacío
        Slot input3 = new Slot("input3"); // Vacío
        Slot output = new Slot("output");
        
        input1.setDocument(doc1);
        // input2 e input3 quedan sin documento
        
        Merger merger = new Merger("test-merger", 
            List.of(input1, input2, input3), 
            output);
        
        // Act
        merger.execute();
        
        // Assert
        Document result = output.getDocument();
        assertNotNull(result, "Output should contain the document from input1");
        
        String data = result.getElementsByTagName("data").item(0).getTextContent();
        assertEquals("Valid data", data);
    }
    
    @Test
    public void testMergerWithAllEmptyInputSlots() throws Exception {
        // Arrange - Todos los slots vacíos
        Slot input1 = new Slot("input1");
        Slot input2 = new Slot("input2");
        Slot output = new Slot("output");
        
        // No establecemos documentos en ningún input
        
        Merger merger = new Merger("test-merger", 
            List.of(input1, input2), 
            output);
        
        // Act
        merger.execute();
        
        // Assert
        // El output debería permanecer vacío
        Document result = output.getDocument();
        assertNull(result, "Output should be null when all inputs are empty");
    }
    
    @Test
    public void testMergerWithSingleInputSlot() throws Exception {
        // Arrange - Solo un input slot (caso degenerado)
        Document doc = TestUtils.createXMLDocument(TestUtils.SAMPLE_XML);
        
        Slot input = new Slot("input");
        Slot output = new Slot("output");
        
        input.setDocument(doc);
        
        Merger merger = new Merger("test-merger", 
            List.of(input), 
            output);
        
        // Act
        merger.execute();
        
        // Assert
        Document result = output.getDocument();
        assertNotNull(result, "Output should contain the document");
        assertEquals("order", result.getDocumentElement().getNodeName());
    }
    
    @Test
    public void testMergerInPipeline() throws Exception {
        // Arrange - Simular un pipeline con Filter -> Merger
        String xml1 = TestUtils.SAMPLE_XML;
        String xml2 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <order>
                    <header>
                        <orderId>67890</orderId>
                        <customer>Jane Smith</customer>
                    </header>
                    <items>
                        <item>
                            <name>Tablet</name>
                            <quantity>1</quantity>
                        </item>
                    </items>
                </order>
                """;
        
        Document doc1 = TestUtils.createXMLDocument(xml1);
        Document doc2 = TestUtils.createXMLDocument(xml2);
        
        // Pipeline: Input1 -> Filter1 -> Branch1 --\
        //                                            Merger -> Output
        //           Input2 -> Filter2 -> Branch2 --/
        
        Slot input1 = new Slot("input1");
        Slot input2 = new Slot("input2");
        Slot branch1 = new Slot("branch1");
        Slot branch2 = new Slot("branch2");
        Slot output = new Slot("output");
        
        input1.setDocument(doc1);
        input2.setDocument(doc2);
        
        // Filtros que aceptan órdenes con al menos 1 item
        Filter filter1 = new Filter("filter1", input1, branch1, 
            "count(/order/items/item) >= 1");
        Filter filter2 = new Filter("filter2", input2, branch2, 
            "count(/order/items/item) >= 1");
        
        // Merger combina ambas ramas
        Merger merger = new Merger("merger", List.of(branch1, branch2), output);
        
        // Act
        filter1.execute();
        filter2.execute();
        merger.execute();
        
        // Assert
        Document result = output.getDocument();
        assertNotNull(result, "Output should contain merged document");
        
        // Verificar que es un documento de pedido válido
        assertEquals("order", result.getDocumentElement().getNodeName());
    }
}
