package dsl.project.tasks;

import dsl.project.model.Document;
import dsl.project.model.Slot;
import dsl.project.model.TaskType;

import java.util.UUID;

/**
 * Example AggregatorTask: collects multiple documents and combines them into a list-like string.
 */
public class AggregatorTask extends Task {

    public AggregatorTask() {
        super(TaskType.Transformer);
    }

    @Override
    public void execute() {
        StringBuilder sb = new StringBuilder();
        for (Slot s : inputSlots) {
            Document d = s.read();
            if (d != null) {
                sb.append(d.getContent()).append(",");
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
