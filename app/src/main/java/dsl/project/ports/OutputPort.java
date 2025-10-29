package dsl.project.ports;

import dsl.project.model.Document;
import dsl.project.model.Slot;

public class OutputPort extends Port {

    public OutputPort(Slot slot) {
        super(slot);
    }

    // Request the next Document from the associated slot
    public Document requestInfo() {
        return slot.read();
    }

    // Execute behavior for output port (could push to external sink)
    public void execute() {
        Document d = slot.read();
        // default behavior: print to stdout (placeholder for real sink)
        if (d != null) {
            System.out.println("OutputPort sent: " + d);
        }
    }
}
