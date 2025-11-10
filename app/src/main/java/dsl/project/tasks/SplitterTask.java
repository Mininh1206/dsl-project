package dsl.project.tasks;

import dsl.project.model.Document;
import dsl.project.model.Slot;
import dsl.project.model.Storage;
import dsl.project.model.TaskType;

import java.util.UUID;

/**
 * Example SplitterTask: takes a document and duplicates or splits its content into multiple outputs.
 */
public class SplitterTask extends Task {

    public SplitterTask() {
        super(TaskType.Mixer);
    }

    @Override
    public void execute() {
        Storage storage = Storage.getInstance();
        for (Slot in : inputSlots) {
            Document d = in.read();
            if (d != null) {
                Object c = d.getContent();
                if (c instanceof String) {
                    String[] parts = ((String) c).split(",");
                    for (String p : parts) {
                        Document out = new Document(UUID.randomUUID().toString(), p.trim());
                        storage.storeDocument(out.getId(), out);
                        for (Slot outSlot : outputSlots) {
                            outSlot.write(out);
                        }
                    }
                } else {
                    Document outDoc = new Document(UUID.randomUUID().toString(), d.getContent());
                    storage.storeDocument(outDoc.getId(), outDoc);
                    for (Slot outSlot : outputSlots) {
                        outSlot.write(outDoc);
                    }
                }
            }
        }
    }
}
