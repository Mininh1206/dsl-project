package iia.dsl.framework.tasks.transformers;

import iia.dsl.framework.Slot;
import iia.dsl.framework.Task;
import iia.dsl.framework.TaskType;
import iia.dsl.framework.Storage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

public class Splitter extends Task {
    private final String itemXPath;
    private final DocumentBuilder docBuilder;

    public Splitter(String id, Slot inputSlot, Slot outputSlot, String itemXPath) {
        super(id, TaskType.TRANSFORMER);
        
        if (inputSlot != null) addInputSlot(inputSlot);
        if (outputSlot != null) addOutputSlot(outputSlot);
        this.itemXPath = itemXPath;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            this.docBuilder = factory.newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing DocumentBuilder for Splitter", e);
        }
    }

    @Override
    public void execute() throws Exception {
        Storage storage = Storage.getInstance();
        Slot in = inputSlots.get(0);
        Slot out = outputSlots.get(0);
        Document d = in.getDocument();
        
        String parentId = in.getMessageId();
        
        if (d == null) {
            System.out.println("Splitter '" + id + "' no tiene documento para dividir.");
            return;
        }

        NodeList nodesToSplit = (NodeList) XPathFactory.newInstance()
            .newXPath()
            .compile(itemXPath)
            .evaluate(d, XPathConstants.NODESET);

        if (nodesToSplit.getLength() == 0) {
            System.out.println("Splitter '" + id + "': XPath no encontró nodos para dividir.");
            return;
        }
        
        System.out.println("Splitter '" + id + "' dividiendo en " + nodesToSplit.getLength() + " partes. ID común: " + parentId);

        List<Integer> partSequenceIndices = new ArrayList<>();

        for (int i = 0; i < nodesToSplit.getLength(); i++) {
            Node splitNode = nodesToSplit.item(i);
            
            Document newDoc = docBuilder.newDocument();
            Node importedNode = newDoc.importNode(splitNode, true);
            newDoc.appendChild(importedNode);
            
            // La clave de almacenamiento es: ID_PADRE + -part- + ÍNDICE
            String partKey = parentId + "-part-" + i;
            
            storage.storeDocument(partKey, newDoc);
            
            partSequenceIndices.add(i); 
            out.setDocument(newDoc); 
        }
        
        String sequenceKey = parentId;
        storage.storePartSequence(sequenceKey, partSequenceIndices);
        System.out.println("✓ Splitter '" + id + "' completado. Secuencia de " + partSequenceIndices.size() + " partes guardada.");
    }
}