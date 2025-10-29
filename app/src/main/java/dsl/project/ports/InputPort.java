package dsl.project.ports;

import dsl.project.model.Document;
import dsl.project.model.Slot;

import java.util.UUID;

public class InputPort extends Port {

    public InputPort(Slot slot) {
        super(slot);
    }

    // execute with data to write into the associated slot
    public void execute(Object data) {
        Document doc = new Document(UUID.randomUUID().toString(), data);
        slot.write(doc);
    }

    // process information from external source and return as Document
    public Document processInfo(Object data) {
        return new Document(UUID.randomUUID().toString(), data);
    }
}
