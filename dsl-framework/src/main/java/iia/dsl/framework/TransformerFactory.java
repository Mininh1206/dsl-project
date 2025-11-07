package iia.dsl.framework;

import iia.dsl.framework.tasks.transformers.*;

public class TransformerFactory extends TaskFactory {
    @Override
    public Task createTask(String id, String taskName) {
        return switch (taskName.toLowerCase()) {
            case "translator" -> new Translator(id, null, null, ""); // Los parámetros se configurarán después
            case "splitter" -> throw new UnsupportedOperationException("Splitter not implemented yet");
            case "aggregator" -> throw new UnsupportedOperationException("Aggregator not implemented yet");
            case "chopper" -> new Chopper(id, null, null, "//item"); // Los parámetros se configurarán después
            case "assembler" -> new Assembler(id, null, null, "assembled"); // Los parámetros se configurarán después
            default -> throw new IllegalArgumentException("Unknown transformer task: " + taskName);
        };
    }
}