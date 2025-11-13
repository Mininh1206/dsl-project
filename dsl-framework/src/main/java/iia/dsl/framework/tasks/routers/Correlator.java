
package iia.dsl.framework.tasks.routers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

/**
 * Correlator Task - Router que correlaciona mensajes de múltiples entradas.
 * 
 * Correlaciona los mensajes de sus múltiples entradas (normalmente usando un
 * id)
 * y los saca al mismo tiempo por sus múltiples salidas.
 */
public class Correlator extends Task {

    private final List<Slot> inputSlot;
    private final List<Slot> outputSlot;
    private final Map<String, Message[]> messages;
    private final Optional<String> xPath;

    Correlator(String id, List<Slot> inputSlot, List<Slot> outputSlot) {
        super(id, TaskType.ROUTER);
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        this.messages = new HashMap<>();
        this.xPath = Optional.empty();
    }

    Correlator(String id, List<Slot> inputSlot, List<Slot> outputSlot, String xPath) {
        super(id, TaskType.ROUTER);
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        this.messages = new HashMap<>();
        this.xPath = Optional.of(xPath);
    }

    @Override
    public void execute() throws Exception {
        if (inputSlot.size() < 2 || inputSlot.size() != outputSlot.size()) {
            throw new Exception("Los slots no son correctos");
        }

        for (int i = 0; i < inputSlot.size(); i++) {

            var in = inputSlot.get(i);

            if (!in.hasMessage()) {
                continue;
            }

            Message m = in.getMessage();

            if (!m.hasHeader(Message.CORRELATION_ID)) {
                throw new Exception("El mensaje no contiene los headers necesarios");
            }

            var correlationId = "";
            if (xPath.isPresent()) {
                XPathFactory xf = XPathFactory.newInstance();
                var x = xf.newXPath();

                var path = x.compile(xPath.get());

                var correlationIdNode = (Node) path.evaluate(m.getDocument(), XPathConstants.NODE);

                correlationId = correlationIdNode.getTextContent();

            } else {
                correlationId = m.getHeader(Message.CORRELATION_ID);
            }
            if (!messages.containsKey(correlationId)) {
                messages.put(correlationId, new Message[outputSlot.size()]);
            }
            messages.get(correlationId)[i] = m;

            boolean allReceived = true;
            for (Message msg : messages.get(correlationId)) {
                if (msg == null) {
                    allReceived = false;
                    break;
                }
            }

            if(allReceived){
                for (int j = 0; j < outputSlot.size(); j++) {
                    outputSlot.get(j).setMessage(messages.get(correlationId)[j]);
                }
                messages.remove(correlationId);
            }
        }

    }

}
