package iia.dsl.framework.tasks.modifiers;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

public class ContextSlimmer extends Task {
    ContextSlimmer(String id, Slot inputSlot, Slot contextSlot, Slot outputSlot) {
        super(id, TaskType.MODIFIER);

        addInputSlot(inputSlot);
        addInputSlot(contextSlot);
        addOutputSlot(outputSlot);
    }
    
    @Override
    public void execute() throws Exception {
        var in = inputSlots.get(0);
        var context = inputSlots.get(1);
        
        while (in.hasMessage() && context.hasMessage()) {
            var m = in.getMessage();
            var contextMessage = context.getMessage();
            
            if (!m.hasDocument() || !contextMessage.hasDocument()) {
                throw new Exception("No hay Documento en el slot de entrada para ContextSlimmer");
            }
            
            // Saca el xpath del cuerpo del mensaje de contexto
            var xpath = "/xpath";
            var xpathFactory = XPathFactory.newInstance();
            var xpathExpr = xpathFactory.newXPath().compile(xpath);
            var contextNode = (Node) xpathExpr.evaluate(contextMessage.getDocument(), XPathConstants.NODE);
            if (contextNode == null) {
                throw new Exception("No se encontró el nodo de XPath en el mensaje de contexto para ContextSlimmer");
            }

            // Saca el nodo a eliminar usando el xpath del mensaje de contexto
            xpath = contextNode.getNodeValue();
            xpathExpr = xpathFactory.newXPath().compile(xpath);
            var removeNode = (Node) xpathExpr.evaluate(m.getDocument(), XPathConstants.NODE);
            if (removeNode == null) {
                throw new Exception("No se encontró el nodo a eliminar en el mensaje para ContextSlimmer");
            }

            // Elimina el nodo del mensaje
            removeNode.getParentNode().removeChild(removeNode);
            outputSlots.get(0).setMessage(m);
        }
    }
}
