package iia.dsl.framework.tasks.routers;

import java.util.List;

import javax.xml.xpath.XPathFactory;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

/**
 * Tarea de enrutamiento condicional. Distribuye un mensaje entrante hacia
 * <b>una</b> de las múltiples salidas posibles.
 * 
 * <p>
 * Utiliza una lista ordenada de expresiones XPath booleanas, correspondientes
 * una a una con los slots de salida.
 * Evalúa las expresiones secuencialmente; el mensaje se envía por el slot
 * asociado a la primera expresión
 * que evalúe a {@code true}.
 */
public class Distributor extends Task {

    private final List<String> xPath;

    Distributor(String id, Slot inputSlot, List<Slot> outputSlots, List<String> xPath) {
        super(id, TaskType.ROUTER);

        this.xPath = xPath;
        addInputSlot(inputSlot);

        addOutputSlots(outputSlots);

    }

    @Override
    public void execute() throws Exception {
        if (xPath.size() != outputSlots.size()) {
            throw new Exception("Configuración inválida en Distributor '" + id
                    + "': El número de expresiones XPath debe coincidir con el número de salidas.");
        }

        var in = inputSlots.get(0);

        while (in.hasMessage()) {
            var m = in.getMessage();

            if (!m.hasDocument()) {
                throw new Exception("No hay Documento en el slot de entrada para Distributor '" + id + "'");
            }

            var d = m.getDocument();

            var xf = XPathFactory.newInstance();
            var x = xf.newXPath();

            for (int i = 0; i < xPath.size(); i++) {

                var ce = x.compile(xPath.get(i));
                var result = (Boolean) ce.evaluate(d, javax.xml.xpath.XPathConstants.BOOLEAN);

                if (result != null && result) {
                    outputSlots.get(i).setMessage(new Message(m.getId(), d, m.getHeaders()));
                }
            }
        }
    }
}