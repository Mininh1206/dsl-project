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

public class Splitter extends Task {
    private final String itemXPath;

    public Splitter(String id, Slot inputSlot, Slot outputSlot, String itemXPath) {
        super(id, TaskType.TRANSFORMER);
        this.itemXPath = itemXPath;
        this.addInputSlot(inputSlot);
        this.addOutputSlot(outputSlot);
    }

    @Override
    public void execute() throws Exception {
        var in = inputSlots.get(0);
        var d = in.getDocument();
        if (d == null) {
            throw new Exception("No hay ningun documento para leer");
        }

        var xf = XPathFactory.newInstance();
        var x = xf.newXPath();

        var ce = x.compile(itemXPath);
        var nodes = (NodeList) ce.evaluate(d, XPathConstants.NODESET);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        if (nodes != null) {
            int totalNodes = nodes.getLength();
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

                    Message msg = new Message(in.getMessageId(), dr);
                    msg.addHeader(Message.NUM_FRAG, "" + i);
                    msg.addHeader(Message.TOTAL_FRAG, "" + totalNodes);
                    outputSlots.get(0).setMessage(msg);
                }
            }
        }

        Storage.getInstance().storeDocument(in.getMessageId(), d);

    }
}