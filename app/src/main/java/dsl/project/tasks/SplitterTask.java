package dsl.project.tasks;

import dsl.project.model.Document;
import dsl.project.model.Slot;
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
        for (Slot in : inputSlots) {
            Document d = in.read();
            if (d != null) {
                // naive splitting: if content is a string with commas, split into parts
                Object c = d.getContent();
                if (c instanceof String) {
                    String[] parts = ((String) c).split(",");
                    for (String p : parts) {
                        Document out = new Document(UUID.randomUUID().toString(), p.trim());
                        for (Slot outSlot : outputSlots) {
                            outSlot.write(out);
                        }
                    }
                } else {
                    // otherwise, duplicate the document to all outputs
                    for (Slot outSlot : outputSlots) {
                        outSlot.write(new Document(UUID.randomUUID().toString(), d.getContent()));
                    }
                }
            }
        }
    }
}
