package iia.dsl.framework.tasks;

import iia.dsl.framework.tasks.transformers.Translator;

public class TransformerFactory extends TaskFactory {
    @Override
    public Task createTask(String id, String taskName) {
        return switch (taskName.toLowerCase()) {
            case "translator" -> new Translator(id, null, null, ""); // Los parámetros se configurarán después
            case "splitter" -> throw new UnsupportedOperationException("Splitter not implemented yet");
            case "aggregator" -> throw new UnsupportedOperationException("Aggregator not implemented yet");
            default -> throw new IllegalArgumentException("Unknown transformer task: " + taskName);
        };
    }
}
