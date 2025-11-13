package iia.dsl.framework.tasks.transformers;

import iia.dsl.framework.core.Slot;

public class TransformerFactory {
    public Translator createTranslatorTask(String id, Slot inputSlot, Slot outputSlot, String translationMap) {
        return new Translator(id, inputSlot, outputSlot, translationMap);
    }

    public Splitter createSplitterTask(String id, Slot inputSlot, Slot outputSlot1, Slot outputSlot2) {
        throw new UnsupportedOperationException("Splitter not implemented yet");
    }

    public Aggregator createAggregatorTask(String id, java.util.List<Slot> inputSlots, Slot outputSlot) {
        throw new UnsupportedOperationException("Aggregator not implemented yet");
    }
}
