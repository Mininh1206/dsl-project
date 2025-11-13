package iia.dsl.framework.tasks.routers;

import javax.xml.xpath.XPathFactory;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

public class Filter extends Task {

    private final String xpath;

    Filter(String id, Slot inputSlot, Slot outputSlot, String xpath) {
        super(id, TaskType.ROUTER);

        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);

        this.xpath = xpath;
    }

    @Override
    public void execute() throws Exception {
        var in = inputSlots.get(0);

        while (in.hasMessage()) {
            var m = in.getMessage();

            if (!m.hasDocument()) {
                throw new Exception("No hay Documento en el slot de entrada para Filter '" + id + "'");
            }

            var d = m.getDocument();

            var xf = XPathFactory.newInstance();
            var x = xf.newXPath();

            var ce = x.compile(xpath);
            var result = ce.evaluate(d, javax.xml.xpath.XPathConstants.NUMBER);

            if (result instanceof Number && ((Number) result).doubleValue() == 1.0) {
                outputSlots.get(0).setMessage(new Message(m));
            }
        }
    }
}
