package iia.dsl.framework;

public abstract class TaskFactory {
    public abstract Task createTask(String id, String taskName);
    
    public static TaskFactory getFactory(TaskType type) {
        return switch (type) {
            case ROUTER -> new RouterFactory();
            case MODIFIER -> new ModifierFactory();
            case TRANSFORMER -> new TransformerFactory();
        };
    }
}