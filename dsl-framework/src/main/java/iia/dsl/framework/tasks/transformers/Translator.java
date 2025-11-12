package iia.dsl.framework.tasks.transformers;

import java.io.StringReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

public class Translator extends Task {
    private final String xslt;

    public Translator(String id, Slot inputSlot, Slot outputSlot, String xslt) {
        super(id, TaskType.MODIFIER);

        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);

        this.xslt = xslt;
    }
    
    @Override
    public void execute() throws Exception {
        var d = inputSlots.get(0).getDocument();
        
        if (d == null) {
            throw new Exception("No hay ningún documento para transformar");
        }
        
        TransformerFactory factory = TransformerFactory.newInstance();
        StreamSource xsltSource = new StreamSource(new StringReader(xslt));
        Transformer transformer = factory.newTransformer(xsltSource);
        
        DOMSource source = new DOMSource(d);
        DOMResult result = new DOMResult();
        
        transformer.transform(source, result);
        
        outputSlots.get(0).setDocument((Document) result.getNode());
    }
}