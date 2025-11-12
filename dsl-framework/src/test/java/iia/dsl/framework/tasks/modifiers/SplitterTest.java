package iia.dsl.framework.tasks.modifiers;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.transformers.Splitter;
import iia.dsl.framework.util.Storage;
import iia.dsl.framework.util.TestUtils;

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
        // TODO
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