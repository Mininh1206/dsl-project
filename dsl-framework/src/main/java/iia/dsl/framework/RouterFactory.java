package iia.dsl.framework;

import iia.dsl.framework.tasks.routers.*;

public class RouterFactory extends TaskFactory {
    @Override
    public Task createTask(String id, String taskName) {
        return switch (taskName.toLowerCase()) {
            case "filter" -> new Filter(id, null, null, ""); // Los parámetros se configurarán después
            case "correlator" -> throw new UnsupportedOperationException("Correlator not implemented yet");
            case "merger" -> throw new UnsupportedOperationException("Merger not implemented yet");
            case "distributor" -> throw new UnsupportedOperationException("Distributor not implemented yet");
            case "replicator" -> throw new UnsupportedOperationException("Replicator not implemented yet");
            case "threader" -> throw new UnsupportedOperationException("Threader not implemented yet");
            default -> throw new IllegalArgumentException("Unknown router task: " + taskName);
        };
    }
}