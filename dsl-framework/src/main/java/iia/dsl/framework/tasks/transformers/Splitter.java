package iia.dsl.framework.tasks.transformers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
        // TODO
    }

    @Override
    public void execute() throws Exception {
        // TODO
    }
}