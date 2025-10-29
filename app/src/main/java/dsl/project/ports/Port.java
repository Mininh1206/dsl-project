package dsl.project.ports;

import dsl.project.model.Slot;

public abstract class Port {
    protected Slot slot;

    public Port(Slot slot) {
        this.slot = slot;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }
}
