package iia.dsl.framework.tasks.transformers;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;
import iia.dsl.framework.util.Storage;

/**
 * Tarea de transformación que divide un documento XML grande en múltiples
 * mensajes más pequeños (fragmentos).
 * 
 * <p>
 * Proceso:
 * <ol>
 * <li>Extrae una lista de nodos del documento original basándose en
 * {@code itemXPath}.</li>
 * <li>Guarda el documento original (menos los nodos extraídos) en el
 * {@link Storage} para su posterior reconstrucción.</li>
 * <li>Crea un nuevo mensaje por cada nodo extraído.</li>
 * <li>Añade headers {@code NUM_FRAG} y {@code TOTAL_FRAG} a cda mensaje para
 * facilitar la agregación.</li>
 * </ol>
 */
public class Splitter extends Task {

    private final String itemXPath;

    Splitter(String id, Slot inputSlot, Slot outputSlot, String itemXPath) {
        super(id, TaskType.TRANSFORMER);
        this.itemXPath = itemXPath;
        this.addInputSlot(inputSlot);
        this.addOutputSlot(outputSlot);
    }

    @Override
    public void execute() throws Exception {
        var in = inputSlots.get(0);

        while (in.hasMessage()) {
            var m = in.getMessage();

            if (!m.hasDocument()) {
                throw new Exception("No hay ningun documento para leer");
            }

            var d = m.getDocument();

            var xf = XPathFactory.newInstance();
            var x = xf.newXPath();

            var ce = x.compile(itemXPath);
            var nodes = (NodeList) ce.evaluate(d, XPathConstants.NODESET);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            if (nodes != null) {
                int totalNodes = nodes.getLength();
                java.util.List<Message> bufferedMessages = new java.util.ArrayList<>();

                for (int i = 0; i < totalNodes; i++) {
                    Node node = nodes.item(i);
                    if (node != null) {
                        // Eliminar el nodo de su padre real, no del documento
                        Node parent = node.getParentNode();
                        if (parent != null) {
                            parent.removeChild(node);
                        }

                        Document dr = builder.newDocument();
                        Node importedNode = dr.importNode(node, true);
                        dr.appendChild(importedNode);

                        Message msg = new Message(m.getId(), dr, m.getHeaders());
                        msg.addHeader(Message.NUM_FRAG, "" + i);
                        msg.addHeader(Message.TOTAL_FRAG, "" + totalNodes);

                        bufferedMessages.add(msg);
                    }
                }

                // CRITICAL: Store the document BEFORE emitting messages to ensure
                // Aggregator can find it when fragments arrive.
                Storage.getInstance().storeDocument(m.getId(), d);

                for (Message msg : bufferedMessages) {
                    outputSlots.get(0).setMessage(msg);
                }
            } else {
                // Even if no nodes, we should probably store the document?
                Storage.getInstance().storeDocument(m.getId(), d);
            }
        }
    }
}
