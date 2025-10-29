package dsl.project.ports;

import dsl.project.model.Slot;

public class PortFactory {

    public InputPort createInputPort(Slot slot) {
        return new InputPort(slot);
    }

    public OutputPort createOutputPort(Slot slot) {
        return new OutputPort(slot);
    }

    public RequestPort createRequestPort(Slot readSlot, Slot writeSlot) {
        return new RequestPort(readSlot, writeSlot);
    }
}
