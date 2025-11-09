package iia.dsl.framework.tasks.routers;

import javax.xml.xpath.XPathFactory;

import iia.dsl.framework.Slot;
import iia.dsl.framework.Task;
import iia.dsl.framework.TaskType;

/**
 * Correlator Task - Router que correlaciona mensajes de múltiples entradas.
 * 
 * Correlaciona los mensajes de sus múltiples entradas (normalmente usando un id)
 * y los saca al mismo tiempo por sus múltiples salidas.
 */
public class Correlator extends Task {

    private final String correlationXPath;

    /**
     * Constructor del Correlator.
     * 
     * @param id Identificador único de la tarea
     * @param inputSlot1 Primer slot de entrada
     * @param inputSlot2 Segundo slot de entrada
     * @param outputSlot1 Primer slot de salida
     * @param outputSlot2 Segundo slot de salida
     * @param correlationXPath XPath para extraer el valor de correlación
     */
    public Correlator(String id, Slot inputSlot1, Slot inputSlot2, Slot outputSlot1, Slot outputSlot2, String correlationXPath) {
        super(id, TaskType.ROUTER);
        
        addInputSlot(inputSlot1);
        addInputSlot(inputSlot2);
        addOutputSlot(outputSlot1);
        addOutputSlot(outputSlot2);
        
        this.correlationXPath = correlationXPath;
    }

    @Override
    public void execute() throws Exception {
        // Validar que hay al menos dos entradas y dos salidas
        if (inputSlots.size() < 2 || outputSlots.size() < 2) {
            throw new IllegalArgumentException("Debe haber al menos 2 entradas y 2 salidas.");
        }

        var doc1 = inputSlots.get(0).getDocument();
        var doc2 = inputSlots.get(1).getDocument();
        
        if (doc1 == null || doc2 == null) {
            throw new Exception("No hay documentos en los slots de entrada");
        }
        
        var xf = XPathFactory.newInstance();
        var x = xf.newXPath();
        var ce = x.compile(correlationXPath);
        
        // Extraer valores de correlación
        String value1 = ce.evaluate(doc1);
        String value2 = ce.evaluate(doc2);
        
        // Si los valores coinciden, enviar a las salidas correspondientes
        if (value1 != null && value1.equals(value2)) {
            outputSlots.get(0).setDocument(doc1);
            outputSlots.get(1).setDocument(doc2);
        }
    }
}
