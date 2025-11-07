package iia.dsl.framework.tasks.modifiers;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import iia.dsl.framework.Slot;
import iia.dsl.framework.Task;
import iia.dsl.framework.TaskType;

/**
 * ContextSlimmer Task - Modifier que elimina información de contexto innecesaria.
 * 
 * Similar a Slimmer, pero diseñado específicamente para eliminar metadatos,
 * información de contexto, headers temporales, o datos de enrutamiento que
 * ya no son necesarios en una etapa posterior del pipeline.
 * 
 * Puede eliminar múltiples nodos que coincidan con el XPath especificado.
 * 
 * @author Javi
 */
public class ContextSlimmer extends Task {
    private final String xpath;

    /**
     * Constructor del ContextSlimmer.
     * 
     * @param id Identificador único de la tarea
     * @param inputSlot Slot de entrada con el documento a procesar
     * @param outputSlot Slot de salida donde se escribirá el documento sin contexto
     * @param xpath Expresión XPath para identificar nodos de contexto a eliminar
     */
    public ContextSlimmer(String id, Slot inputSlot, Slot outputSlot, String xpath) {
        super(id, TaskType.MODIFIER);

        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);

        this.xpath = xpath;
    }
    
    @Override
    public void execute() throws Exception {
        var d = inputSlots.get(0).getDocument();
        
        if (d == null) {
            throw new Exception("No hay ningún documento para procesar");
        }
        
        var xf = XPathFactory.newInstance();
        var x = xf.newXPath();
        
        var ce = x.compile(xpath);
        
        // Clonar el documento para no modificar el original
        var dr = (Document) d.cloneNode(true);
        
        // Evaluar XPath para encontrar todos los nodos que coincidan
        var nodeList = ce.evaluate(dr, XPathConstants.NODESET);
        
        if (nodeList != null && nodeList instanceof NodeList) {
            NodeList nodes = (NodeList) nodeList;
            
            // Eliminar todos los nodos encontrados (iterar en reversa para evitar problemas de índice)
            for (int i = nodes.getLength() - 1; i >= 0; i--) {
                Node node = nodes.item(i);
                if (node != null && node.getParentNode() != null) {
                    node.getParentNode().removeChild(node);
                }
            }
        }
        
        outputSlots.get(0).setDocument(dr);
    }
}
