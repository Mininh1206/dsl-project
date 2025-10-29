package dsl.project.ports;

import dsl.project.model.Document;
import dsl.project.model.Slot;

import java.util.UUID;

/**
 * RequestPort reads from one slot (requestSlot) and optionally writes into another (responseSlot).
 */
public class RequestPort extends Port {
    private final Slot responseSlot; // where to write results

    public RequestPort(Slot requestSlot, Slot responseSlot) {
        super(requestSlot);
        this.responseSlot = responseSlot;
    }

    // read from request slot and possibly produce a response Document
    public void execute() {
        Document req = slot.read();
        if (req != null) {
            // simple echo/transform behavior
            Document resp = processInfo(req.getContent());
            if (responseSlot != null && resp != null) {
                responseSlot.write(resp);
            }
        }
    }

    public Document processInfo(Object data) {
        return new Document(UUID.randomUUID().toString(), data);
    }
}
