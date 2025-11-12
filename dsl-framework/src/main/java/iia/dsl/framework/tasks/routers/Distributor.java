package iia.dsl.framework.tasks.routers;

import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathFactory;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

public class Distributor extends Task {

    private final Slot inputSlot;
    private final List<Slot> outputSlot;
    private final List<String> xPath;

    public Distributor(String id, Slot inputSlot, List<Slot> outputSlot, List<String> xPath) {
        super(id, TaskType.ROUTER);

        this.xPath = xPath;
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;

    }

    @Override
    public void execute() throws Exception {

        if (xPath.size() != outputSlot.size()) {
            throw new Exception("Los slots no son correctos");
        }

        var in = inputSlots.get(0);
        var d = in.getDocument();

        var xf = XPathFactory.newInstance();
        var x = xf.newXPath();

        for (int i = 0; i < xPath.size(); i++) {

            var ce = x.compile(xPath.get(i));
            var result = ce.evaluate(d, javax.xml.xpath.XPathConstants.NUMBER);

            if (result instanceof Number && ((Number) result).doubleValue() == 1.0) {
                outputSlots.get(i).setMessage(new Message(in.getMessageId(), d));
            }
        }
    }
}