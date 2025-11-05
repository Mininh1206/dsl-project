package iia.dsl.framework.tasks.modifiers;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import iia.dsl.framework.Slot;
import iia.dsl.framework.Task;
import iia.dsl.framework.TaskType;

public class Slimmer extends Task {
    private final String xpath;

    public Slimmer(String id, Slot inputSlot, Slot outputSlot, String xpath) {
        super(id, TaskType.MODIFIER);

        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);

        this.xpath = xpath;
    }
    
    @Override
    public void execute() throws Exception {
        var d = inputSlots.get(0).getDocument();
        
        if (d == null) {
            throw new Exception("No hay ningun documento para leer");
        }
        
        var xf = XPathFactory.newInstance();
        var x = xf.newXPath();
        
        var ce = x.compile(xpath);
        var node = ce.evaluate(d, XPathConstants.NODE);
        
        if (node != null) {
            var dr = (Document) d.cloneNode(true);

            var nodeToRemove = ce.evaluate(dr, XPathConstants.NODE);

            if (nodeToRemove != null && nodeToRemove instanceof Node) {
                ((Node)nodeToRemove).getParentNode().removeChild((Node)nodeToRemove);
                outputSlots.get(0).setDocument(dr);
            }
        }
    }
}
