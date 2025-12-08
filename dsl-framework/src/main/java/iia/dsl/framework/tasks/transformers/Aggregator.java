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
 * Transformer que reconstruye un documento original a partir de sus fragmentos.
 * 
 * Funciona en conjunto con Splitter. Utiliza los headers 'NUM_FRAG' y
 * 'TOTAL_FRAG' para recolectar
 * todas las piezas de un mensaje original (identificado por su ID) y las
 * reinserta en el documento
 * original (recuperado de una unidad de almacenamiento 'Storage') en la
 * ubicación indicada por 'itemXPath'.
 *
 * @author javi
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
                throw new Exception("No hay Documento en el slot de entrada para Aggregator");
            }

            if (!m.hasHeader(Message.NUM_FRAG) || !m.hasHeader(Message.TOTAL_FRAG)) {
                throw new Exception("El mensaje no contiene los headers necesarios para la agregación");
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
