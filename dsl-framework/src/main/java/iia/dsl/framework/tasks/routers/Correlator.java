
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
 * Router que sincroniza y agrupa mensajes provenientes de múltiples entradas
 * basándose en un ID de correlación.
 * 
 * Espera a recibir mensajes con el mismo ID de correlación en todas sus
 * entradas antes de liberarlos
 * simultáneamente por sus respectivas salidas.
 * 
 * El ID de correlación se obtiene, por defecto, del header 'CorrelationId', o
 * mediante una expresión XPath
 * configurada opcionalmente.
 */
public class Correlator extends Task {
    private final Map<String, Message[]> messages;
    private final Optional<String> xPath;

    Correlator(String id, List<Slot> inputSlots, List<Slot> outputSlots) {
        super(id, TaskType.ROUTER);
        this.inputSlots.addAll(inputSlots);
        this.outputSlots.addAll(outputSlots);
        this.messages = new HashMap<>();
        this.xPath = Optional.empty();
    }

    Correlator(String id, List<Slot> inputSlots, List<Slot> outputSlots, String xPath) {
        super(id, TaskType.ROUTER);
        this.inputSlots.addAll(inputSlots);
        this.outputSlots.addAll(outputSlots);
        this.messages = new HashMap<>();
        this.xPath = Optional.of(xPath);
    }

    @Override
    public void execute() throws Exception {
        if (inputSlots.size() < 2 || inputSlots.size() != outputSlots.size()) {
            throw new Exception("Los slots no son correctos");
        }

        for (int i = 0; i < inputSlots.size(); i++) {

            var in = inputSlots.get(i);

            while (in.hasMessage()) {
                var m = in.getMessage();

                if (!m.hasDocument()) {
                    throw new Exception("No hay Documento en el slot de entrada para Correlator '" + id + "'");
                }

                String correlationId;

                if (xPath.isPresent()) {
                    XPathFactory xf = XPathFactory.newInstance();
                    var x = xf.newXPath();

                    var path = x.compile(xPath.get());

                    var correlationIdNode = (Node) path.evaluate(m.getDocument(), XPathConstants.NODE);

                    correlationId = correlationIdNode.getFirstChild().getNodeValue();

                } else {
                    correlationId = m.getHeader(Message.CORRELATION_ID);
                }

                if (!messages.containsKey(correlationId)) {
                    messages.put(correlationId, new Message[outputSlots.size()]);
                }

                messages.get(correlationId)[i] = m;

                boolean allReceived = true;
                for (Message msg : messages.get(correlationId)) {
                    if (msg == null) {
                        allReceived = false;
                        break;
                    }
                }

                if (allReceived) {
                    for (int j = 0; j < outputSlots.size(); j++) {
                        outputSlots.get(j).setMessage(new Message(messages.get(correlationId)[j]));
                    }
                    messages.remove(correlationId);
                }
            }
        }

    }

}
