package iia.dsl.framework.tasks.transformers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;
import iia.dsl.framework.util.Storage;

/**
 * Tarea de transformación que reconstruye un documento original a partir de sus
 * fragmentos.
 * 
 * <p>
 * Complemento del {@link Splitter}. Funciona recolectando fragmentos de
 * mensajes que comparten un mismo ID.
 * Utiliza los headers {@code NUM_FRAG} y {@code TOTAL_FRAG} para determinar
 * cuándo se han recibido todas las piezas.
 * 
 * <p>
 * Una vez completado el conjunto:
 * <ol>
 * <li>Recupera el documento "esqueleto" original desde el {@link Storage}.</li>
 * <li>Reinserta cada fragmento en la ubicación especificada por
 * {@code itemXPath}.</li>
 * <li>Publica el documento reconstruido completo en el slot de salida.</li>
 * </ol>
 */
public class Aggregator extends Task {

    private final String itemXPath;

    private final Map<String, Message[]> messages;

    Aggregator(String id, Slot inputSlot, Slot outputSlot, String itemXPath) {
        super(id, TaskType.TRANSFORMER);

        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);
        messages = new ConcurrentHashMap<>();
        this.itemXPath = itemXPath;
    }

    @Override
    public void execute() throws Exception {
        var in = inputSlots.get(0);

        while (in.hasMessage()) {
            var m = in.getMessage();

            if (!m.hasDocument()) {
                throw new Exception("Error en Aggregator: El mensaje recibido no contiene un documento XML.");
            }

            if (!m.hasHeader(Message.NUM_FRAG) || !m.hasHeader(Message.TOTAL_FRAG)) {
                throw new Exception("Error en Aggregator: Falta metadata de fragmentación (NUM_FRAG, TOTAL_FRAG).");
            }

            var numFrag = Integer.parseInt(m.getHeader(Message.NUM_FRAG));
            var totalFrag = Integer.parseInt(m.getHeader(Message.TOTAL_FRAG));

            Message[] fragments;
            // Atomic initialization of the array if absent
            synchronized (messages) {
                if (!messages.containsKey(m.getId())) {
                    messages.put(m.getId(), new Message[totalFrag]);
                }
                fragments = messages.get(m.getId());
            }

            // Sync on the specific fragment array to ensure consistent updates and checks
            boolean allReceived = false;
            synchronized (fragments) {
                fragments[numFrag] = m;

                allReceived = true;
                for (int i = 0; i < fragments.length; i++) {
                    if (fragments[i] == null) {
                        allReceived = false;
                        break;
                    }
                }
            }

            if (allReceived) {
                messages.remove(m.getId()); // Clean up map

                var storage = Storage.getInstance();

                // Reconstruir el documento completo con el documento almacenado y los
                // fragmentos recibidos en el xpath
                var doc = storage.retrieveDocument(m.getId());

                if (doc == null) {
                    throw new Exception("No se encontró el documento original almacenado para el mensaje ID: "
                            + m.getId());
                }

                var xf = XPathFactory.newInstance();
                var x = xf.newXPath();
                var ce = x.compile(itemXPath);
                var nodeOfList = (Node) ce.evaluate(doc, XPathConstants.NODE);

                for (Message msg : fragments) {
                    var itemNode = doc.importNode(
                            msg.getDocument().getDocumentElement(), true);
                    nodeOfList.appendChild(itemNode);
                }

                outputSlots.get(0).setMessage(new Message(m.getId(), doc, m.getHeaders()));
            }
        }
    }
}
