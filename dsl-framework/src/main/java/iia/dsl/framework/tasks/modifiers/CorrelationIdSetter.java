// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package iia.dsl.framework.tasks.modifiers;

import java.util.concurrent.atomic.AtomicInteger;

import iia.dsl.framework.core.Message;
import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

/**
 * Tarea modificadora que inyecta un identificador de correlación único al
 * mensaje.
 * 
 * <p>
 * Genera un ID secuencial ({@code 000001}, {@code 000002}...) y lo añade como
 * header {@code CORRELATION_ID}.
 * Esto es fundamental para tareas posteriores que requieren agrupar mensajes
 * relacionados,
 * como {@link iia.dsl.framework.tasks.routers.Correlator} o
 * {@link iia.dsl.framework.tasks.transformers.Aggregator}.
 */
public class CorrelationIdSetter extends Task {

   private static final AtomicInteger COUNTER = new AtomicInteger(0);

   CorrelationIdSetter(String id, Slot inputSlot, Slot outputSlot) {
      super(id, TaskType.MODIFIER);
      addInputSlot(inputSlot);
      addOutputSlot(outputSlot);
   }

   @Override
   public void execute() throws Exception {
      if (inputSlots.isEmpty() || outputSlots.isEmpty()) {
         throw new IllegalArgumentException("CorrelationIdSetter requiere 1 input y 1 output slot");
      }

      var inSlot = inputSlots.get(0);
      var outSlot = outputSlots.get(0);

      while (inSlot.hasMessage()) {
         // Intentar compatibilidad con uso por documentos individuales
         var msg = inSlot.getMessage();

         if (!msg.hasDocument()) {
            throw new Exception("No hay mensaje/documento en el slot de entrada para CorrelationIdSetter '" + id + "'");
         }

         msg.addHeader(Message.CORRELATION_ID, generateId());

         outSlot.setMessage(msg);
      }
   }

   private String generateId() {
      int next = COUNTER.incrementAndGet();
      return String.format("%06d", next);
   }
}
