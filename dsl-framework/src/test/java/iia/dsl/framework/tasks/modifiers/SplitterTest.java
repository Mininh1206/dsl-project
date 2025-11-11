package iia.dsl.framework.tasks.modifiers;

import iia.dsl.framework.Slot;
import iia.dsl.framework.Storage;
import iia.dsl.framework.tasks.transformers.Splitter;
import iia.dsl.framework.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SplitterTest {

    private Storage storage;
    private Slot inputSlot;
    private Slot outputSlot;
    private String parentId;
    
    // Un XML de ejemplo que tiene un nodo padre (<items>) y varios nodos hijos (<item>)
    private static final String SPLIT_SAMPLE_XML = """
            <order id="MSG-12345">
                <header>...</header>
                <items>
                    <item id="P001">Laptop</item>
                    <item id="P002">Mouse</item>
                    <item id="P003">Keyboard</item>
                </items>
            </order>
            """;

    @BeforeEach
    void setUp() throws Exception {
        storage = Storage.getInstance();
        
        // Simular un mensaje con un ID único
        parentId = "TEST_SPLIT_" + UUID.randomUUID().toString();
        Document doc = TestUtils.createXMLDocument(SPLIT_SAMPLE_XML);
        
        // El framework necesita un Message en el slot para obtener el ID
        inputSlot = new Slot("input");
        inputSlot.setDocument(doc); 
        // Forzar el ID del mensaje, ya que setDocument usa UUID, y el Splitter usa getMessageId()
        inputSlot.getMessage().setId(parentId); 
        
        outputSlot = new Slot("output");
    }

    @AfterEach
    void tearDown() {
        // Limpiar el Storage después de cada test
        storage.removeDocument(parentId + "-part-0");
        storage.removeDocument(parentId + "-part-1");
        storage.removeDocument(parentId + "-part-2");
        storage.removePartSequence(parentId);
    }

    @Test
    void testSplitterFragmentationAndStorage() throws Exception {
        // Arrange
        String itemXPath = "/order/items/item"; // XPath para los nodos a dividir
        Splitter splitter = new Splitter("test-splitter", inputSlot, outputSlot, itemXPath);

        // Act
        splitter.execute();

        // Assert 1: Verificar que la secuencia de índices se haya guardado
        List<Integer> sequence = storage.retrievePartSequence(parentId);
        assertNotNull(sequence, "La secuencia de índices debe estar guardada bajo el ParentId.");
        assertEquals(3, sequence.size(), "La secuencia debe contener 3 índices (0, 1, 2).");
        assertEquals(0, sequence.get(0));
        assertEquals(1, sequence.get(1));
        
        // Assert 2: Verificar que las partes individuales estén guardadas en el Storage
        Document part0 = storage.retrieveDocument(parentId + "-part-0");
        Document part1 = storage.retrieveDocument(parentId + "-part-1");
        Document part2 = storage.retrieveDocument(parentId + "-part-2");
        
        assertNotNull(part0, "La Parte 0 debe existir en el Storage.");
        assertNotNull(part1, "La Parte 1 debe existir en el Storage.");
        assertNotNull(part2, "La Parte 2 debe existir en el Storage.");
        
        // Assert 3: Verificar el contenido de una de las partes
        Element rootPart0 = part0.getDocumentElement();
        assertEquals("item", rootPart0.getNodeName(), "El nodo raíz de la parte debe ser <item>.");
        assertEquals("P001", rootPart0.getAttribute("id"), "La parte 0 debe contener el ID correcto.");

        // Assert 4: Verificar que el outputSlot contenga la última parte
        // Esto es una limitación del framework, pero verificamos que haya algo.
        assertNotNull(outputSlot.getDocument(), "El Slot de salida debe contener el último documento procesado.");
    }
    
    @Test
    void testSplitterNoNodesFound() throws Exception {
        // Arrange
        String invalidXPath = "/order/nonExistentNode";
        Splitter splitter = new Splitter("test-splitter", inputSlot, outputSlot, invalidXPath);

        // Act
        splitter.execute();

        // Assert: El storage debe estar vacío y no debe haber secuencia
        assertNull(storage.retrievePartSequence(parentId), "No debe haber secuencia si no se encontraron nodos.");
        assertNull(outputSlot.getDocument(), "El outputSlot debe estar vacío si no se encontraron nodos (se escribió null).");
    }
}