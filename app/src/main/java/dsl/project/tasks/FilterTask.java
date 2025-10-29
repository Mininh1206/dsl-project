package dsl.project.tasks;

import dsl.project.model.Document;
import dsl.project.model.Slot;
import dsl.project.model.TaskType;

import java.util.UUID;

/**
 * Example FilterTask: passes through documents that are non-null or match a simple predicate.
 */
public class FilterTask extends Task {

    public FilterTask() {
        super(TaskType.Modifier);
    }

    @Override
    public void execute() {
        for (Slot in : inputSlots) {
            Document d = in.read();
            if (d != null) {
                // simple predicate: only pass through documents whose content is not an empty string
                Object c = d.getContent();
                boolean pass = true;
                if (c instanceof String) {
                    pass = !((String) c).trim().isEmpty();
                }
                if (pass) {
                    Document out = new Document(UUID.randomUUID().toString(), d.getContent());
                    for (Slot outSlot : outputSlots) {
                        outSlot.write(out);
                    }
                }
            }
        }
    }
}
