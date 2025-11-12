package iia.dsl.framework.tasks;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.transformers.Translator;

public class TransformerFactory extends TaskFactory {
    public Task createTranslatorTask(String id, Slot inputSlot, Slot outputSlot, String translationMap) {
        return new Translator(id, inputSlot, outputSlot, translationMap);
    }

    public Task createSplitterTask(String id, Slot inputSlot, Slot outputSlot1, Slot outputSlot2) {
        throw new UnsupportedOperationException("Splitter not implemented yet");
    }

    public Task createAggregatorTask(String id, java.util.List<Slot> inputSlots, Slot outputSlot) {
        throw new UnsupportedOperationException("Aggregator not implemented yet");
    }
}
