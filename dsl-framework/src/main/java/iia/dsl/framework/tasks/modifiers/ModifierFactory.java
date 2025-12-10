package iia.dsl.framework.tasks.modifiers;

import iia.dsl.framework.core.Slot;

/**
 * Factoría para la creación simplificada de tareas de Modificación (Modifiers).
 * Centraliza la instanciación de Slimmers, ContextSlimmers, ContextEnrichers y
 * CorrelationIdSetters.
 */
public class ModifierFactory {
    public Slimmer createSlimmerTask(String id, Slot inputSlot, Slot outputSlot, String xpath) {
        return new Slimmer(id, inputSlot, outputSlot, xpath);
    }

    public ContextSlimmer createContextSlimmerTask(String id, Slot inputSlot, Slot contextSlot, Slot outputSlot) {
        return new ContextSlimmer(id, inputSlot, contextSlot, outputSlot);
    }

    public ContextEnricher createContextEnricherTask(String id, Slot inputSlot, Slot contextSlot, Slot outputSlot) {
        return new ContextEnricher(id, inputSlot, contextSlot, outputSlot);
    }

    public CorrelationIdSetter createCorrelationIdSetterTask(String id, Slot inputSlot, Slot outputSlot) {
        return new CorrelationIdSetter(id, inputSlot, outputSlot);
    }
}
