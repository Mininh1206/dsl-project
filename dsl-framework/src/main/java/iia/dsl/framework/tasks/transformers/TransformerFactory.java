package iia.dsl.framework.tasks.transformers;

import iia.dsl.framework.core.Slot;

/**
 * Factoría para la creación simplificada de tareas de Transformación
 * (Transformers).
 * Centraliza la instanciación de Translators, Splitters y Aggregators.
 */
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
