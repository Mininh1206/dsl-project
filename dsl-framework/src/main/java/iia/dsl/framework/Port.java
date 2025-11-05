package iia.dsl.framework;

public abstract class Port extends Element {
    protected Connector connector;
    protected Slot slot;
    
    public Port(String id, Connector connector, Slot slot) {
        super(id);
        this.connector = connector;
        this.slot = slot;
    }
    
    public abstract void execute();
}