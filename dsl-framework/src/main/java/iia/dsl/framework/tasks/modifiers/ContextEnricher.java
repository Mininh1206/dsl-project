package iia.dsl.framework.tasks.modifiers;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;


public class ContextEnricher extends Task {
    
    public ContextEnricher(String id, Slot input, Slot context, Slot output) {
        super(id, TaskType.MODIFIER);
        
        if (input != null) addInputSlot(input);
        if (context != null) addInputSlot(context);
        if (output != null) addOutputSlot(output);
    }

    @Override
    public void execute() throws Exception {
        var in = inputSlots.get(0);
        var context = inputSlots.get(1);

        if (!in.hasMessage() || !context.hasMessage()) {
            throw new Exception("No hay Mensaje en el slot de entrada para ContextEnricher");
        }

        var m = in.getMessage();
        var contextMessage = context.getMessage();

        if (!m.hasDocument() || !contextMessage.hasDocument()) {
            throw new Exception("No hay Documento en el slot de entrada para ContextEnricher");
        }

        // Saca el xpath del cuerpo del mensaje de contexto
        var xpath = "/xpath";
        var xpathFactory = XPathFactory.newInstance();
        var xpathExpr = xpathFactory.newXPath().compile(xpath);
        var contextNode = (Node) xpathExpr.evaluate(contextMessage.getDocument(), XPathConstants.NODE);
        if (contextNode == null) {
            throw new Exception("No se encontró el nodo de XPath en el mensaje de contexto para ContextEnricher");
        }

        // Saca el cuerpo con el cual enriquecer el mensaje
        xpath = "/body";
        xpathExpr = xpathFactory.newXPath().compile(xpath);
        var bodyNode = (Node) xpathExpr.evaluate(m.getDocument(), XPathConstants.NODE);
        if (bodyNode == null) {
            throw new Exception("No se encontró el nodo de cuerpo en el mensaje para ContextEnricher");
        }

        // Saca el nodo a enriquecer usando el xpath del mensaje de contexto
        xpath = contextNode.getTextContent();
        xpathExpr = xpathFactory.newXPath().compile(xpath);
        var enrichNode = (Node) xpathExpr.evaluate(m.getDocument(), XPathConstants.NODE);
        if (enrichNode == null) {
            throw new Exception("No se encontró el nodo a enriquecer en el mensaje para ContextEnricher");
        }

        // Enriquece el nodo
        enrichNode.appendChild(bodyNode.cloneNode(true));
        m.setDocument(m.getDocument());
        outputSlots.get(0).setMessage(m);
    }
}