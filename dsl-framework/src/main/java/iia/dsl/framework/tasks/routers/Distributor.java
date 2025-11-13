package iia.dsl.framework.tasks.routers;

import java.util.List;

import javax.xml.xpath.XPathFactory;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

public class Distributor extends Task {

   
    private final List<String> xPath;

    Distributor(String id, Slot inputSlot, List<Slot> outputSlots, List<String> xPath) {
        super(id, TaskType.ROUTER);

        this.xPath = xPath;
        addInputSlot(inputSlot);

        addOutputSlots(outputSlots);

    }

    

    @Override
    public void execute() throws Exception {
        if (xPath.size() != outputSlots.size()) {
            throw new Exception("Los slots no son correctos");
        }

        var in = inputSlots.get(0);

        if (!in.hasMessage()) {
            return;
        }

        var m = in.getMessage();
        
        if (!m.hasDocument()) {
            throw new Exception("No hay Documento en el slot de entrada para Distributor '" + id + "'");
        }

        var d = m.getDocument();

        var xf = XPathFactory.newInstance();
        var x = xf.newXPath();

        for (int i = 0; i < xPath.size(); i++) {

            var ce = x.compile(xPath.get(i));
            var result = (Boolean) ce.evaluate(d, javax.xml.xpath.XPathConstants.BOOLEAN);

            if (result != null && result) {
                outputSlots.get(i).setMessage(new Message(m.getId(), d, m.getHeaders()));
            }
        }
    }
}