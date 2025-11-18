package dsl.project.tasks;

import dsl.project.model.Document;
import dsl.project.model.Slot;
import dsl.project.model.Storage;
import dsl.project.model.TaskType;
import java.time.Instant;

/**
 * Enriquecedor de contexto: añade metadatos al documento si existe.
 */
public class ContextEnricherTask extends Task {
    public ContextEnricherTask(String id, Slot input, Slot output) {
        super(id, TaskType.Modifier);
        if (input != null) addInputSlot(input);
        if (output != null) addOutputSlot(output);
    }

    @Override
    public void execute() {
        Storage storage = Storage.getInstance();
        for (Slot in : inputSlots) {
            Document d = in.read();
            if (d != null && d.getContent() instanceof String) {
                String content = (String) d.getContent();
                String enriched = content + " | context: enrichedBy=" + getClass().getSimpleName() + ", timestamp=" + java.time.Instant.now();
                Document outDoc = new Document(d.getId(), enriched);
                storage.storeDocument(outDoc.getId(), outDoc);
                for (Slot out : outputSlots) {
                    out.write(outDoc);
                }
            } else if (d != null) {
                storage.storeDocument(d.getId(), d);
                for (Slot out : outputSlots) {
                    out.write(d);
                }
            }
        }
    }
}
