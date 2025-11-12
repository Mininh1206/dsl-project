package iia.dsl.framework.tasks;

import iia.dsl.framework.tasks.routers.Correlator;
import iia.dsl.framework.tasks.routers.Filter;
import iia.dsl.framework.tasks.routers.Merger;

public class RouterFactory extends TaskFactory {
    @Override
    public Task createTask(String id, String taskName) {
        return switch (taskName.toLowerCase()) {
            case "filter" -> new Filter(id, null, null, ""); // Los parámetros se configurarán después
            case "correlator" -> new Correlator(id, null, null, null, null, "");
            case "merger" -> new Merger(id, java.util.List.of(), null); // Los parámetros se configurarán después
            case "distributor" -> throw new UnsupportedOperationException("Distributor not implemented yet");
            case "replicator" -> throw new UnsupportedOperationException("Replicator not implemented yet");
            case "threader" -> throw new UnsupportedOperationException("Threader not implemented yet");
            default -> throw new IllegalArgumentException("Unknown router task: " + taskName);
        };
    }

    
}
