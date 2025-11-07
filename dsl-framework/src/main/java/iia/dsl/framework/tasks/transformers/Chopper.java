package iia.dsl.framework.tasks.transformers;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import iia.dsl.framework.Message;
import iia.dsl.framework.Slot;
import iia.dsl.framework.Task;
import iia.dsl.framework.TaskType;

/**
 * Chopper Task - Transformer que divide documentos grandes en fragmentos más pequeños.
 * 
 * Utiliza una expresión XPath para identificar elementos que deben ser extraídos
 * como fragmentos independientes. Cada fragmento se empaqueta en un nuevo documento
 * con un wrapper común, preservando el contexto necesario.
 * 
 * Útil para:
 * - Dividir documentos grandes para procesamiento paralelo
 * - Extraer elementos individuales de colecciones
 * - Crear mensajes más manejables desde documentos bulk
 * 
 * @author Javi
 */
public class Chopper extends Task {
    private final String xpath;
    private final String wrapperElementName;

    /**
     * Constructor del Chopper.
     * 
     * @param id Identificador único de la tarea
     * @param inputSlot Slot de entrada con el documento a fragmentar
     * @param outputSlot Slot de salida donde se escribirán los fragmentos
     * @param xpath Expresión XPath para identificar elementos a extraer
     * @param wrapperElementName Nombre del elemento raíz para cada fragmento
     */
    public Chopper(String id, Slot inputSlot, Slot outputSlot, String xpath, String wrapperElementName) {
        super(id, TaskType.TRANSFORMER);

        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);

        this.xpath = xpath;
        this.wrapperElementName = wrapperElementName;
    }
    
    /**
     * Constructor simplificado que usa "fragment" como nombre del wrapper.
     */
    public Chopper(String id, Slot inputSlot, Slot outputSlot, String xpath) {
        this(id, inputSlot, outputSlot, xpath, "fragment");
    }
    
    @Override
    public void execute() throws Exception {
        var d = inputSlots.get(0).getDocument();
        
        if (d == null) {
            throw new Exception("No hay ningún documento para fragmentar");
        }
        
        var xf = XPathFactory.newInstance();
        var x = xf.newXPath();
        
        var ce = x.compile(xpath);
        var nodeList = ce.evaluate(d, XPathConstants.NODESET);
        
        if (nodeList != null && nodeList instanceof NodeList) {
            NodeList nodes = (NodeList) nodeList;
            
            // Obtener mensaje original para preservar ID base
            Message originalMessage = inputSlots.get(0).getMessage();
            String baseMessageId = originalMessage != null ? originalMessage.getId() : "chop";
            
            // Crear un documento fragmento por cada nodo encontrado
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                
                // Crear nuevo documento para este fragmento
                Document fragmentDoc = builder.newDocument();
                Element wrapper = fragmentDoc.createElement(wrapperElementName);
                fragmentDoc.appendChild(wrapper);
                
                // Importar el nodo al nuevo documento
                Node importedNode = fragmentDoc.importNode(node, true);
                wrapper.appendChild(importedNode);
                
                // Añadir metadatos de fragmentación
                Element metadata = fragmentDoc.createElement("choppedMetadata");
                metadata.setAttribute("fragmentIndex", String.valueOf(i));
                metadata.setAttribute("totalFragments", String.valueOf(nodes.getLength()));
                metadata.setAttribute("originalMessageId", baseMessageId);
                wrapper.insertBefore(metadata, wrapper.getFirstChild());
                
                // Crear mensaje con ID único para el fragmento
                String fragmentId = baseMessageId + "-frag-" + i;
                Message fragmentMessage = new Message(fragmentId, fragmentDoc);
                
                // Escribir el fragmento al output slot
                outputSlots.get(0).setMessage(fragmentMessage);
            }
        }
    }
}
