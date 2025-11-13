package iia.dsl.framework.tasks.transformers;

import iia.dsl.framework.core.Slot;

public class TransformerFactory {
    public Translator createTranslatorTask(String id, Slot inputSlot, Slot outputSlot, String translationMap) {
        return new Translator(id, inputSlot, outputSlot, translationMap);
    }

    public Splitter createSplitterTask(String id, Slot inputSlot, Slot outputSlot, String itemXPath) {
        return new Splitter(id, inputSlot, outputSlot, itemXPath);
    }

    public Aggregator createAggregatorTask(String id, Slot inputSlot, Slot outputSlot, String itemXPath) {
        return new Aggregator(id, inputSlot, outputSlot, itemXPath);
    }
}
