package iia.dsl.framework;

import iia.dsl.framework.tasks.modifiers.*;

public class ModifierFactory extends TaskFactory {
    @Override
    public Task createTask(String id, String taskName) {
        return switch (taskName.toLowerCase()) {
            case "slimmer" -> new Slimmer(id, null, null, ""); // Los parámetros se configurarán después
            case "contextslimmer" -> throw new UnsupportedOperationException("ContextSlimmer not implemented yet");
            case "contextenricher" -> throw new UnsupportedOperationException("ContextEnricher not implemented yet");
            case "headerpromoter" -> throw new UnsupportedOperationException("HeaderPromoter not implemented yet");
            case "headerdemoter" -> throw new UnsupportedOperationException("HeaderDemoter not implemented yet");
            case "correlationidsetter" -> throw new UnsupportedOperationException("CorrelationIdSetter not implemented yet");
            case "returnaddresssetter" -> throw new UnsupportedOperationException("ReturnAddressSetter not implemented yet");
            default -> throw new IllegalArgumentException("Unknown modifier task: " + taskName);
        };
    }
}