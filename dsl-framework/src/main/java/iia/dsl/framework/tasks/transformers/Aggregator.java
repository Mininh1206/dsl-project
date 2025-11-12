package iia.dsl.framework.tasks.transformers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

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

    private final Map<String, List<Message>> messages;

    /**
     * Constructor del Aggregator.
     * 
     * @param id Identificador único de la tarea
     * @param inputSlot Slot de entrada con los fragmentos a agregar
     * @param outputSlot Slot de salida donde se escribirá el documento agregado
     * @param wrapperElementName Nombre del elemento raíz para el documento agregado
     */
    public Aggregator(String id, Slot inputSlot, Slot outputSlot, String itemXPath) {
        super(id, TaskType.TRANSFORMER);
        
        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);
        messages = new HashMap<>();
        this.itemXPath = itemXPath;
    }
    
    
    public Aggregator(String id, Slot inputSlot, Slot outputSlot) {
        this(id, inputSlot, outputSlot, "aggregated");
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
        var d = m.getDocument();

        if(messages.containsKey(m.getId())) {
            messages.get(m.getId()).add(m);
        } else {
            messages.put(m.getId(), List.of(m));
        }
        
    }
}

