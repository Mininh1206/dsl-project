
package iia.dsl.framework.tasks.routers;

import java.util.concurrent.ConcurrentHashMap;
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
 * Tarea de enrutamiento que sincroniza y agrupa mensajes provenientes de
 * múltiples entradas
 * basándose en un ID de correlación compartido.
 * 
 * <p>
 * Funcionamiento:
 * <ol>
 * <li>Espera a recibir un mensaje en <b>cada uno</b> de los slots de entrada (N
 * entradas).</li>
 * <li>Los agrupa verificando que tengan el mismo Correlation ID.</li>
 * <li>Una vez completado el grupo, libera los mensajes simultáneamente por los
 * slots de salida respectivos.</li>
 * </ol>
 * 
 * <p>
 * El ID de correlación se extrae por defecto del header {@code CORRELATION_ID}.
 * Opcionalmente, se puede configurar una expresión XPath personalizada para
 * extraer el ID
 * del contenido del mensaje.
 */
public class Correlator extends Task {
    private final Map<String, Message[]> messages;
    private final Optional<String> xPath;

    Correlator(String id, List<Slot> inputSlots, List<Slot> outputSlots) {
        super(id, TaskType.ROUTER);
        for (Slot slot : inputSlots) {
            addInputSlot(slot);
        }
        this.outputSlots.addAll(outputSlots);
        this.messages = new ConcurrentHashMap<>();
        this.xPath = Optional.empty();
    }

    Correlator(String id, List<Slot> inputSlots, List<Slot> outputSlots, String xPath) {
        super(id, TaskType.ROUTER);
        for (Slot slot : inputSlots) {
            addInputSlot(slot);
        }
        this.outputSlots.addAll(outputSlots);
        this.messages = new ConcurrentHashMap<>();
        this.xPath = Optional.of(xPath);
    }

    @Override
    public void execute() throws Exception {
        // Validación básica de la configuración
        if (inputSlots.size() < 2 || inputSlots.size() != outputSlots.size()) {
            throw new Exception("Configuración inválida en Correlator '" + id
                    + "': Se requieren al menos 2 entradas y misma cantidad de salidas.");
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
                    if (correlationId == null) {
                        correlationId = m.getId();
                    }
                }

                Message[] msgs;
                synchronized (messages) {
                    if (!messages.containsKey(correlationId)) {
                        messages.put(correlationId, new Message[outputSlots.size()]);
                    }
                    msgs = messages.get(correlationId);
                }

                boolean allReceived = false;
                synchronized (msgs) {
                    msgs[i] = m;

                    allReceived = true;
                    for (int k = 0; k < msgs.length; k++) {
                        if (msgs[k] == null) {
                            allReceived = false;
                            break;
                        }
                    }
                }

                if (allReceived) {
                    synchronized (messages) {
                        messages.remove(correlationId);
                    }
                    for (int j = 0; j < outputSlots.size(); j++) {
                        outputSlots.get(j).setMessage(new Message(msgs[j]));
                    }
                }
            }
        }

    }

}
