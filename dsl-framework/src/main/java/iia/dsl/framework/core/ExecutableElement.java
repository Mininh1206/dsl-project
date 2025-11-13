package iia.dsl.framework.core;

public abstract class ExecutableElement extends Element {
    public ExecutableElement() {
        super();
    }

    public ExecutableElement(String id) {
        super(id);
    }

    public abstract void execute() throws Exception;
}
