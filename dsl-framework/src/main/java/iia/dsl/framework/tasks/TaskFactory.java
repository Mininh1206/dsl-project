package iia.dsl.framework.tasks;

public abstract class TaskFactory {
    public static TaskFactory getFactory(TaskType type) {
        return switch (type) {
            case ROUTER -> new RouterFactory();
            case MODIFIER -> new ModifierFactory();
            case TRANSFORMER -> new TransformerFactory();
        };
    }
}
