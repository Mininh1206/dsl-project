// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package iia.dsl.framework.tasks.modifiers;

import java.util.concurrent.atomic.AtomicInteger;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.Task;
import iia.dsl.framework.tasks.TaskType;

/**
 * CorrelationIdSetter - asigna un ID de correlación (secuencial) al Message
 * presente en el slot de entrada y lo publica en el slot de salida.
 *
 */
public class CorrelationIdSetter extends Task {

   private static final AtomicInteger COUNTER = new AtomicInteger(0);

   public CorrelationIdSetter(String id, Slot inputSlot, Slot outputSlot) {
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

      if (!inSlot.hasMessage())
         throw new Exception("No hay mensaje en el slot de entrada para CorrelationIdSetter '" + id + "'");

      // Intentar compatibilidad con uso por documentos individuales
      var msg = inSlot.getMessage();
      
      if (!msg.hasDocument()) {
         throw new Exception("No hay mensaje/documento en el slot de entrada para CorrelationIdSetter '" + id + "'");
      }
      
      msg.addHeader("correlation-id", generateId());

      outSlot.setMessage(msg);
   }

   private String generateId() {
      int next = COUNTER.incrementAndGet();
      return String.format("%06d", next);
   }
}
