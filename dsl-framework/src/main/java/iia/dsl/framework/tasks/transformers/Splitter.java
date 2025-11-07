package iia.dsl.framework.tasks.transformers;

import java.io.StringReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import iia.dsl.framework.Slot;
import iia.dsl.framework.Task;
import iia.dsl.framework.TaskType;

public class Splitter extends Task {
    private final String xslt;

    public Splitter(String id, Slot inputSlot, Slot outputSlot, String xslt) {
        super(id, TaskType.MODIFIER);

        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);

        this.xslt = xslt;
    }
    
    @Override
    public void execute() throws Exception {
        // Validar slots y documento de entrada
        if (inputSlots.isEmpty()) {
            throw new Exception("No hay ningún slot de entrada configurado");
        }

        var inputSlot = inputSlots.get(0);
        var d = inputSlot.getDocument();

        if (d == null) {
            throw new Exception("No hay ningún documento para transformar");
        }

        // Preparar transformer a partir del XSLT en memoria
        TransformerFactory factory = TransformerFactory.newInstance();
        StreamSource xsltSource = new StreamSource(new StringReader(xslt));
        Transformer transformer = factory.newTransformer(xsltSource);

        // Transformar
        DOMSource source = new DOMSource(d);
        DOMResult result = new DOMResult();
        transformer.transform(source, result);

        // Obtener nodo resultado y convertir a Document si es necesario
        Node outNode = result.getNode();
        if (outNode == null) {
            throw new Exception("La transformación no produjo ningún resultado");
        }

        Document outDoc;
        if (outNode instanceof Document) {
            outDoc = (Document) outNode;
        } else {
            // Crear un nuevo Document e importar el nodo transformado
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            outDoc = db.newDocument();
            Node imported = outDoc.importNode(outNode, true);
            outDoc.appendChild(imported);
        }

        if (outputSlots.isEmpty()) {
            throw new Exception("No hay ningún slot de salida configurado");
        }

        outputSlots.get(0).setDocument(outDoc);
    }
}