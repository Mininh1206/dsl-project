package iia.dsl.framework.tasks.transformers;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import iia.dsl.framework.Slot;
import iia.dsl.framework.Task;
import iia.dsl.framework.TaskType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Assembler Task - Transformer que ensambla fragmentos en un documento completo.
 * 
 * Es la operación inversa de Chopper. Recibe múltiples fragmentos y los combina
 * en un único documento, usando los metadatos de fragmentación para reconstruir
 * correctamente el documento original.
 * 
 * Características:
 * - Agrupa fragmentos por originalMessageId
 * - Ordena fragmentos por fragmentIndex
 * - Extrae el contenido de cada fragmento
 * - Los combina en un documento con la estructura especificada
 * 
 * @author Javi
 */
public class Assembler extends Task {
    private final String rootElementName;
    private final String contentXPath;
    
    // Buffer interno para acumular fragmentos de múltiples ejecuciones
    private final Map<String, List<FragmentInfo>> fragmentBuffer;

    /**
     * Constructor del Assembler.
     * 
     * @param id Identificador único de la tarea
     * @param inputSlot Slot de entrada con fragmentos
     * @param outputSlot Slot de salida donde se escribirá el documento ensamblado
     * @param rootElementName Nombre del elemento raíz del documento ensamblado
     * @param contentXPath XPath para extraer contenido del fragmento (relativo al wrapper)
     */
    public Assembler(String id, Slot inputSlot, Slot outputSlot, 
                     String rootElementName, String contentXPath) {
        super(id, TaskType.TRANSFORMER);

        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);

        this.rootElementName = rootElementName;
        this.contentXPath = contentXPath;
        this.fragmentBuffer = new HashMap<>();
    }
    
    /**
     * Constructor simplificado que extrae el primer hijo del wrapper.
     */
    public Assembler(String id, Slot inputSlot, Slot outputSlot, String rootElementName) {
        this(id, inputSlot, outputSlot, rootElementName, null);
    }
    
    @Override
    public void execute() throws Exception {
        var d = inputSlots.get(0).getDocument();
        
        if (d == null) {
            throw new Exception("No hay ningún documento fragmento para ensamblar");
        }
        
        // Extraer metadatos del fragmento
        NodeList metadataNodes = d.getElementsByTagName("choppedMetadata");
        
        if (metadataNodes.getLength() == 0) {
            throw new Exception("El documento no contiene metadatos de fragmentación (choppedMetadata)");
        }
        
        Element metadata = (Element) metadataNodes.item(0);
        String originalMsgId = metadata.getAttribute("originalMessageId");
        int fragmentIndex = Integer.parseInt(metadata.getAttribute("fragmentIndex"));
        int totalFragments = Integer.parseInt(metadata.getAttribute("totalFragments"));
        
        // Extraer contenido del fragmento (saltando metadata)
        Node contentNode = extractContent(d);
        
        // Almacenar fragmento en buffer
        FragmentInfo fragInfo = new FragmentInfo(fragmentIndex, totalFragments, contentNode);
        fragmentBuffer.computeIfAbsent(originalMsgId, k -> new ArrayList<>()).add(fragInfo);
        
        // Verificar si tenemos todos los fragmentos para este mensaje
        List<FragmentInfo> fragments = fragmentBuffer.get(originalMsgId);
        
        if (fragments.size() == totalFragments) {
            // Tenemos todos los fragmentos, ensamblar documento
            Document assembledDoc = assembleDocument(fragments);
            
            // Escribir documento ensamblado
            outputSlots.get(0).setDocument(assembledDoc);
            
            // Limpiar buffer para este mensaje
            fragmentBuffer.remove(originalMsgId);
        }
        // Si no tenemos todos los fragmentos aún, no escribimos nada al output
    }
    
    /**
     * Extrae el contenido relevante del fragmento.
     */
    private Node extractContent(Document fragmentDoc) {
        // Si hay contentXPath, usarlo
        if (contentXPath != null && !contentXPath.isEmpty()) {
            // TODO: Implementar XPath extraction
            // Por ahora, usar estrategia simple
        }
        
        // Estrategia simple: obtener el primer hijo que no sea metadata
        Element root = fragmentDoc.getDocumentElement();
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if (child.getNodeType() == Node.ELEMENT_NODE && 
                !child.getNodeName().equals("choppedMetadata")) {
                return child;
            }
        }
        
        return null;
    }
    
    /**
     * Ensambla todos los fragmentos en un documento completo.
     */
    private Document assembleDocument(List<FragmentInfo> fragments) throws Exception {
        // Ordenar fragmentos por índice
        fragments.sort((a, b) -> Integer.compare(a.index, b.index));
        
        // Crear documento nuevo
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        // Crear elemento raíz
        Element root = doc.createElement(rootElementName);
        doc.appendChild(root);
        
        // Añadir cada fragmento
        for (FragmentInfo fragment : fragments) {
            if (fragment.content != null) {
                Node importedNode = doc.importNode(fragment.content, true);
                root.appendChild(importedNode);
            }
        }
        
        return doc;
    }
    
    /**
     * Clase interna para almacenar información de fragmentos.
     */
    private static class FragmentInfo {
        final int index;
        final Node content;
        
        FragmentInfo(int index, int total, Node content) {
            this.index = index;
            this.content = content;
            // total se usa solo para validación, no necesita almacenarse
        }
    }
}
