package iia.dsl.framework;

public abstract class Element {
    protected String id;
    
    public Element(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
}