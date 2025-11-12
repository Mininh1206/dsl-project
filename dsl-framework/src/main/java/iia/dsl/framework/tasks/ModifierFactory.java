package iia.dsl.framework.tasks;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.modifiers.ContextEnricher;
import iia.dsl.framework.tasks.modifiers.ContextSlimmer;
import iia.dsl.framework.tasks.modifiers.CorrelationIdSetter;
import iia.dsl.framework.tasks.modifiers.Slimmer;

public class ModifierFactory extends TaskFactory {
    public Task createSlimmerTask(String id, Slot inputSlot, Slot outputSlot, String xpath) {
        return new Slimmer(id, inputSlot, outputSlot, xpath);
    }

    public Task createContextSlimmerTask(String id, Slot inputSlot, Slot contextSlot, Slot outputSlot) {
        return new ContextSlimmer(id, inputSlot, contextSlot, outputSlot);
    }

    public Task createContextEnricherTask(String id, Slot inputSlot, Slot contextSlot, Slot outputSlot) {
        return new ContextEnricher(id, inputSlot, contextSlot, outputSlot);
    }

    public Task createCorrelationIdSetterTask(String id, Slot inputSlot, Slot outputSlot) {
        return new CorrelationIdSetter(id, inputSlot, outputSlot);
    }

    public Task createHeaderPromoterTask(String id, Slot inputSlot, Slot outputSlot, String headerName) {
        throw new UnsupportedOperationException("HeaderPromoter not implemented yet");
    }

    public Task createHeaderDemoterTask(String id, Slot inputSlot, Slot outputSlot, String headerName) {
        throw new UnsupportedOperationException("HeaderDemoter not implemented yet");
    }

    public Task createReturnAddressSetterTask(String id, Slot inputSlot, Slot outputSlot, String returnAddress) {
        throw new UnsupportedOperationException("ReturnAddressSetter not implemented yet");
    }
}
