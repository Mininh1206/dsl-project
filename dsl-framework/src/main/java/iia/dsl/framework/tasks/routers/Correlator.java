package iia.dsl.framework.tasks.routers;

import iia.dsl.framework.Slot;
import iia.dsl.framework.Task;
import iia.dsl.framework.TaskType;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class Correlator extends Task {
    private final String xpath;

    public Correlator(String id, Slot inputSlot, Slot outputSlot, String xpath) {
        super(id, TaskType.ROUTER);

        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);

        this.xpath = xpath;
    }
    
    @Override
    public void execute() throws XPathExpressionException {
        // Validar slots y documento de entrada
        if (inputSlots.isEmpty()) {
            return; // no hay entrada que correlacionar
        }

        var inputSlot = inputSlots.get(0);
        var d = inputSlot.getDocument();
        if (d == null) {
            return; // nada que procesar
        }

        var xf = XPathFactory.newInstance();
        var x = xf.newXPath();

        // Compilar y evaluar la expresión XPath como booleano.
        var ce = x.compile(xpath);
        Boolean matched = (Boolean) ce.evaluate(d, javax.xml.xpath.XPathConstants.BOOLEAN);

        // Si la expresión coincide, enrutar el documento al primer output (si existe)
        if (Boolean.TRUE.equals(matched) && !outputSlots.isEmpty()) {
            outputSlots.get(0).setDocument(d);
        }
    }
}
