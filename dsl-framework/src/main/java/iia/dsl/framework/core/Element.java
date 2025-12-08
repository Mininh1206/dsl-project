package iia.dsl.framework.core;

import java.util.UUID;

public abstract class Element {
    protected final String id;

    public Element(String id) {
        this.id = id;
    }

    public Element() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }
}
