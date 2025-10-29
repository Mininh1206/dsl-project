package dsl.project.tasks;

import dsl.project.model.Document;
import dsl.project.model.Slot;
import dsl.project.model.TaskType;

import java.util.UUID;

/**
 * Example concrete Task that correlates inputs and writes a combined output.
 */
public class CorrelatorTask extends Task {

    public CorrelatorTask() {
        super(TaskType.Transformer);
    }

    @Override
    public void execute() {
        // simple correlation: read all available input documents and combine their contents into one
        StringBuilder sb = new StringBuilder();
        for (Slot s : inputSlots) {
            Document d = s.read();
            if (d != null) {
                sb.append(d.getContent()).append("|");
            }
        }
        if (sb.length() > 0) {
            Document out = new Document(UUID.randomUUID().toString(), sb.toString());
            for (Slot outSlot : outputSlots) {
                outSlot.write(out);
            }
        }
    }
}
