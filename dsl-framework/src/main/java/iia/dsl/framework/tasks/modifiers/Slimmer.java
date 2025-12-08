package iia.dsl.framework.tasks.modifiers;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

/**
 * Tarea que elimina un nodo específico del documento del mensaje.
 * El nodo a eliminar se determina mediante una expresión XPath configurada en
 * la construcción de la tarea.
 */
public class Slimmer extends Task {

    private final String xpath;

    Slimmer(String id, Slot inputSlot, Slot outputSlot, String xpath) {
        super(id, TaskType.MODIFIER);

        addInputSlot(inputSlot);
        addOutputSlot(outputSlot);

        this.xpath = xpath;
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

            var ce = x.compile(xpath);
            var node = ce.evaluate(d, XPathConstants.NODE);

            if (node != null) {
                var dr = (Document) d.cloneNode(true);

                var nodeToRemove = ce.evaluate(dr, XPathConstants.NODE);

                if (nodeToRemove != null && nodeToRemove instanceof Node) {
                    ((Node) nodeToRemove).getParentNode().removeChild((Node) nodeToRemove);

                    outputSlots.get(0).setMessage(new Message(m.getId(), dr, m.getHeaders()));
                }
            }
        }
    }
}
