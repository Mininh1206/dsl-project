package iia.dsl.framework.tasks.transformers;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Translator Tests")
class TranslatorTest {

    private Slot inputSlot;
    private Slot outputSlot;
    private Document testDocument;
    private String simpleXslt;

    @BeforeEach
    void setUp() throws Exception {
        inputSlot = new Slot("inputSlot");
        outputSlot = new Slot("outputSlot");
        
        // XSLT de identidad que copia el documento tal cual
        simpleXslt = "<?xml version='1.0'?>" +
                "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>" +
                "  <xsl:template match='@*|node()'>" +
                "    <xsl:copy>" +
                "      <xsl:apply-templates select='@*|node()'/>" +
                "    </xsl:copy>" +
                "  </xsl:template>" +
                "</xsl:stylesheet>";
        
        // Crear documento de prueba
        var factory = DocumentBuilderFactory.newInstance();
        var builder = factory.newDocumentBuilder();
        testDocument = builder.newDocument();
        var root = testDocument.createElement("root");
        root.setTextContent("test content");
        testDocument.appendChild(root);
    }

    @Test
    @DisplayName("Debe transformar correctamente un mensaje con documento")
    void testExecuteWithValidMessage() throws Exception {
        // Arrange
        var translator = new Translator("translator1", inputSlot, outputSlot, simpleXslt);
        var message = new Message("msg1", testDocument, new HashMap<>());
        inputSlot.setMessage(message);
        
        // Act
        translator.execute();
        
        // Assert
        assertTrue(outputSlot.hasMessage());
        var outputMessage = outputSlot.getMessage();
        assertNotNull(outputMessage);
        assertEquals("msg1", outputMessage.getId());
        assertNotNull(outputMessage.getDocument());
        assertTrue(outputMessage.hasDocument());
    }

    @Test
    @DisplayName("Debe procesar múltiples mensajes en el slot de entrada")
    void testExecuteWithMultipleMessages() throws Exception {
        // Arrange
        var translator = new Translator("translator1", inputSlot, outputSlot, simpleXslt);
        var message1 = new Message("msg1", testDocument, new HashMap<>());
        var message2 = new Message("msg2", testDocument, new HashMap<>());
        inputSlot.setMessage(message1);
        inputSlot.setMessage(message2);
        
        // Act
        translator.execute();
        
        // Assert
        assertTrue(outputSlot.hasMessage());
        assertNotNull(outputSlot.getMessage());
    }

    @Test
    @DisplayName("Debe ejecutarse sin error cuando no hay mensajes iniciales")
    void testExecuteWithNoMessages() throws Exception {
        // Arrange
        var translator = new Translator("translator1", inputSlot, outputSlot, simpleXslt);
        // No se añade ningún mensaje al inputSlot
        
        // Act - El comportamiento real es que NO lanza excepción si no hay mensajes
        translator.execute();
        
        // Assert - Verifica que el slot de salida tampoco tiene mensajes
        assertFalse(outputSlot.hasMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el mensaje no tiene documento")
    void testExecuteWithMessageWithoutDocument() {
        // Arrange
        var translator = new Translator("translator1", inputSlot, outputSlot, simpleXslt);
        var messageWithoutDoc = new Message("msg1", null, new HashMap<>());
        inputSlot.setMessage(messageWithoutDoc);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            translator.execute();
        });
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("No hay Documento"));
        assertTrue(exception.getMessage().contains("translator1"));
    }

    @Test
    @DisplayName("Debe preservar headers del mensaje original")
    void testExecutePreservesHeaders() throws Exception {
        // Arrange
        var translator = new Translator("translator1", inputSlot, outputSlot, simpleXslt);
        var headers = new HashMap<String, String>();
        headers.put("key1", "value1");
        headers.put("key2", "value2");
        var message = new Message("msg1", testDocument, headers);
        inputSlot.setMessage(message);
        
        // Act
        translator.execute();
        
        // Assert
        var outputMessage = outputSlot.getMessage();
        assertNotNull(outputMessage.getHeaders());
        assertEquals("value1", outputMessage.getHeaders().get("key1"));
        assertEquals("value2", outputMessage.getHeaders().get("key2"));
        assertEquals(2, outputMessage.getHeaders().size());
    }

    @Test
    @DisplayName("Debe mantener el ID del mensaje original")
    void testExecutePreservesMessageId() throws Exception {
        // Arrange
        var translator = new Translator("translator1", inputSlot, outputSlot, simpleXslt);
        var message = new Message("uniqueId123", testDocument, new HashMap<>());
        inputSlot.setMessage(message);
        
        // Act
        translator.execute();
        
        // Assert
        var outputMessage = outputSlot.getMessage();
        assertEquals("uniqueId123", outputMessage.getId());
    }

    @Test
    @DisplayName("Debe aplicar la transformación XSLT al documento")
    void testXsltTransformation() throws Exception {
        // Arrange
        var translator = new Translator("translator1", inputSlot, outputSlot, simpleXslt);
        var factory = DocumentBuilderFactory.newInstance();
        var builder = factory.newDocumentBuilder();
        var doc = builder.newDocument();
        var element = doc.createElement("original");
        element.setTextContent("data");
        doc.appendChild(element);
        
        var message = new Message("msg1", doc, new HashMap<>());
        inputSlot.setMessage(message);
        
        // Act
        translator.execute();
        
        // Assert
        var outputMessage = outputSlot.getMessage();
        var transformedDoc = outputMessage.getDocument();
        assertNotNull(transformedDoc);
        // Con XSLT de identidad, el elemento raíz sigue siendo "original"
        assertEquals("original", transformedDoc.getDocumentElement().getNodeName());
    }

    @Test
    @DisplayName("Debe procesar documentos vacíos correctamente")
    void testExecuteWithEmptyDocument() throws Exception {
        // Arrange
        var translator = new Translator("translator1", inputSlot, outputSlot, simpleXslt);
        var factory = DocumentBuilderFactory.newInstance();
        var builder = factory.newDocumentBuilder();
        var emptyDoc = builder.newDocument();
        emptyDoc.appendChild(emptyDoc.createElement("empty"));
        
        var message = new Message("msg1", emptyDoc, new HashMap<>());
        inputSlot.setMessage(message);
        
        // Act
        translator.execute();
        
        // Assert
        assertTrue(outputSlot.hasMessage());
        assertNotNull(outputSlot.getMessage().getDocument());
    }

    @Test
    @DisplayName("Debe manejar headers vacíos correctamente")
    void testExecuteWithEmptyHeaders() throws Exception {
        // Arrange
        var translator = new Translator("translator1", inputSlot, outputSlot, simpleXslt);
        var message = new Message("msg1", testDocument, new HashMap<>());
        inputSlot.setMessage(message);
        
        // Act
        translator.execute();
        
        // Assert
        var outputMessage = outputSlot.getMessage();
        assertNotNull(outputMessage.getHeaders());
        assertTrue(outputMessage.getHeaders().isEmpty());
    }

    @Test
    @DisplayName("Debe verificar que el documento transformado existe")
    void testTransformedDocumentExists() throws Exception {
        // Arrange
        var translator = new Translator("translator1", inputSlot, outputSlot, simpleXslt);
        var message = new Message("msg1", testDocument, new HashMap<>());
        inputSlot.setMessage(message);
        
        // Act
        translator.execute();
        
        // Assert
        var outputMessage = outputSlot.getMessage();
        var transformedDoc = outputMessage.getDocument();
        
        assertNotNull(transformedDoc);
        assertNotNull(transformedDoc.getDocumentElement());
    }

    @Test
    @DisplayName("Debe establecer el mensaje en el slot de salida")
    void testOutputSlotHasMessage() throws Exception {
        // Arrange
        var translator = new Translator("translator1", inputSlot, outputSlot, simpleXslt);
        var message = new Message("msg1", testDocument, new HashMap<>());
        inputSlot.setMessage(message);
        
        // Verificar que el slot de salida está vacío antes
        assertFalse(outputSlot.hasMessage());
        
        // Act
        translator.execute();
        
        // Assert
        assertTrue(outputSlot.hasMessage());
    }

    @Test
    @DisplayName("Debe procesar correctamente documentos XML complejos")
    void testComplexXmlDocument() throws Exception {
        // Arrange
        var translator = new Translator("translator1", inputSlot, outputSlot, simpleXslt);
        var factory = DocumentBuilderFactory.newInstance();
        var builder = factory.newDocumentBuilder();
        var doc = builder.newDocument();
        
        var root = doc.createElement("root");
        var child1 = doc.createElement("child1");
        child1.setTextContent("content1");
        var child2 = doc.createElement("child2");
        child2.setTextContent("content2");
        
        root.appendChild(child1);
        root.appendChild(child2);
        doc.appendChild(root);
        
        var message = new Message("msg1", doc, new HashMap<>());
        inputSlot.setMessage(message);
        
        // Act
        translator.execute();
        
        // Assert
        var outputMessage = outputSlot.getMessage();
        var transformedDoc = outputMessage.getDocument();
        assertNotNull(transformedDoc);
        assertEquals("root", transformedDoc.getDocumentElement().getNodeName());
    }

    @Test
    @DisplayName("Debe manejar transformación con documento complejo")
    void testTransformationWithComplexStructure() throws Exception {
        // Arrange
        var translator = new Translator("translator1", inputSlot, outputSlot, simpleXslt);
        var factory = DocumentBuilderFactory.newInstance();
        var builder = factory.newDocumentBuilder();
        var doc = builder.newDocument();
        
        var root = doc.createElement("data");
        for (int i = 0; i < 5; i++) {
            var item = doc.createElement("item");
            item.setAttribute("id", String.valueOf(i));
            item.setTextContent("Content " + i);
            root.appendChild(item);
        }
        doc.appendChild(root);
        
        var message = new Message("msg1", doc, new HashMap<>());
        inputSlot.setMessage(message);
        
        // Act
        translator.execute();
        
        // Assert
        assertTrue(outputSlot.hasMessage());
        var result = outputSlot.getMessage();
        assertNotNull(result.getDocument());
        assertEquals("data", result.getDocument().getDocumentElement().getNodeName());
    }
}