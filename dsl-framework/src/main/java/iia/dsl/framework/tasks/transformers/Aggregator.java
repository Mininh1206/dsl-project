package iia.dsl.framework.tasks.transformers;

import java.util.HashMap;
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
 * Aggregator Task - Transformer que reconstruye mensajes divididos previamente.
 * 
 * Reconstruye un mensaje dividido previamente por una tarea Splitter/Chopper.
 * Agrupa los mensajes por su ID de conjunto y los combina en un solo documento.
 * 
 * @author javi
 */
public class Aggregator extends Task {

    private final String itemXPath;

    private final Map<String, Message[]> messages;

    public Aggregator(String id, Slot inputSlot, Slot outputSlot, String itemXPath) {
        super(id, TaskType.TRANSFORMER);
        
        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);
        messages = new HashMap<>();
        this.itemXPath = itemXPath;
    }

    @Override
    public void execute() throws Exception {
        var in = inputSlots.get(0);

        if (!in.hasMessage()) {
            throw new Exception("No hay Mensaje en el slot de entrada para Aggregator");
        }

        var m = in.getMessage();

        if (!m.hasDocument()) {
            throw new Exception("No hay Documento en el slot de entrada para Aggregator");
        }

        if (!m.hasHeader(Message.NUM_FRAG) || !m.hasHeader(Message.TOTAL_FRAG)) {
            throw new Exception("El mensaje no contiene los headers necesarios para la agregación");
        }

        var numFrag = Integer.parseInt(m.getHeader(Message.NUM_FRAG));
        var totalFrag = Integer.parseInt(m.getHeader(Message.TOTAL_FRAG));

        if (!messages.containsKey(m.getId())) {
            messages.put(m.getId(), new Message[totalFrag]);
        }

        messages.get(m.getId())[numFrag] = m;

        // Verificar si todos los fragmentos han sido recibidos
        boolean allReceived = true;
        for (Message msg : messages.get(m.getId())) {
            if (msg == null) {
                allReceived = false;
                break;
            }
        }

        if (allReceived) {
            var storage = Storage.getInstance();

            // Reconstruir el documento completo con el documento almacenado y los fragmentos recibidos en el xpath
            
            var doc = storage.retrieveDocument(m.getId());

            if (doc == null) {
                throw new Exception("No se encontró el documento original almacenado para el mensaje ID: "
                        + m.getId());
            }
            
            var xf = XPathFactory.newInstance();
            var x = xf.newXPath();
            var ce = x.compile(itemXPath);
            var nodeOfList = (Node) ce.evaluate(doc, XPathConstants.NODE);

            for (Message msg : messages.get(m.getId())) {
                nodeOfList.appendChild(msg.getDocument());
            }

            outputSlots.get(0).setMessage(new Message(m.getId(), doc));
        }
    }
}

