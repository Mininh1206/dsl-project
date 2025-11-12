package iia.dsl.framework.tasks.routers;

import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

public class Distributor extends Task {
    private final Map<String, Slot> routingRules;
    private final Slot defaultOutputSlot; 

    public Distributor(String id, Slot inputSlot, Map<String, Slot> rules, Slot defaultSlot) {
        super(id, TaskType.ROUTER);
        
        addInputSlot(inputSlot);
        
        this.routingRules = rules;
        this.defaultOutputSlot = defaultSlot;

        rules.values().forEach(this::addOutputSlot);
        if (defaultSlot != null && !rules.containsValue(defaultSlot)) {
            addOutputSlot(defaultSlot);
        }
    }

    @Override
    public void execute() throws Exception {
        Document d = inputSlots.get(0).getDocument();
        
        if (d == null) {
            System.out.println("Distributor '" + id + "' no tiene documento para rutear. Terminando.");
            return;
        }

        Slot destinationSlot = null;
        var xf = XPathFactory.newInstance();
        var x = xf.newXPath();
        
        for (Map.Entry<String, Slot> entry : routingRules.entrySet()) {
            String xpathCondition = entry.getKey();
            Slot targetSlot = entry.getValue();
            
            // Evalúa la condición XPath
            boolean result = (Boolean) x.compile(xpathCondition).evaluate(d, XPathConstants.BOOLEAN);
            
            if (result) {
                destinationSlot = targetSlot;
                break; 
            }
        }

        if (destinationSlot == null) {
            destinationSlot = defaultOutputSlot;
        }
        
        if (destinationSlot != null) {

            Document docCopy = (Document) d.cloneNode(true); 
            destinationSlot.setDocument(docCopy);
        }
    }
}