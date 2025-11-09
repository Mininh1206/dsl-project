package iia.dsl.framework.tasks.transformers;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import iia.dsl.framework.Message;
import iia.dsl.framework.Slot;
import iia.dsl.framework.Task;
import iia.dsl.framework.TaskType;

/**
 * Aggregator Task - Transformer que reconstruye mensajes divididos previamente.
 * 
 * Reconstruye un mensaje dividido previamente por una tarea Splitter/Chopper.
 * Agrupa los mensajes por su ID de conjunto y los combina en un solo documento.
 * 
 * @author javi
 */
public class Aggregator extends Task {

    private final String wrapperElementName;

    /**
     * Constructor del Aggregator.
     * 
     * @param id Identificador único de la tarea
     * @param inputSlot Slot de entrada con los fragmentos a agregar
     * @param outputSlot Slot de salida donde se escribirá el documento agregado
     * @param wrapperElementName Nombre del elemento raíz para el documento agregado
     */
    public Aggregator(String id, Slot inputSlot, Slot outputSlot, String wrapperElementName) {
        super(id, TaskType.TRANSFORMER);
        
        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);
        
        this.wrapperElementName = wrapperElementName;
    }
    
    /**
     * Constructor simplificado que usa "aggregated" como nombre del wrapper.
     */
    public Aggregator(String id, Slot inputSlot, Slot outputSlot) {
        this(id, inputSlot, outputSlot, "aggregated");
    }

    @Override
    public void execute() throws Exception {
        var inputMessage = inputSlots.get(0).getMessage();
        
        if (inputMessage == null) {
            throw new Exception("No hay mensaje en el slot de entrada");
        }
        
        var inputDoc = inputMessage.getDocument();
        
        if (inputDoc == null) {
            throw new Exception("No hay documento en el mensaje de entrada");
        }
        
        // Crear un nuevo documento para el resultado agregado
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document aggregatedDoc = db.newDocument();
        
        // Crear el elemento raíz
        Element root = aggregatedDoc.createElement(wrapperElementName);
        aggregatedDoc.appendChild(root);
        
        // Copiar todos los elementos del documento de entrada al nuevo documento
        Node rootNode = inputDoc.getDocumentElement();
        if (rootNode != null && rootNode.hasChildNodes()) {
            var childNodes = rootNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Node importedNode = aggregatedDoc.importNode(child, true);
                    root.appendChild(importedNode);
                }
            }
        }
        
        // Crear mensaje de salida con el documento agregado
        Message outputMessage = new Message(inputMessage.getId(), aggregatedDoc);
        outputSlots.get(0).setMessage(outputMessage);
    }
}

