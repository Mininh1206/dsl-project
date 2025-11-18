package dsl.project;

import dsl.project.model.Document;
import dsl.project.model.Slot;
import dsl.project.model.Storage;
import dsl.project.tasks.ContextEnricherTask;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ContextEnricherTaskTest {
    @Test
    public void testEnricherStoresEnrichedDocument() {
        Storage.getInstance().clear();
        Slot input = new Slot();
        Slot output = new Slot();
        String content = "original content";
        Document doc = new Document("doc2", content);
        input.write(doc);
        ContextEnricherTask enricher = new ContextEnricherTask();
        enricher.addInputSlot(input);
        enricher.addOutputSlot(output);
        enricher.execute();
        Document enriched = Storage.getInstance().getDocument(doc.getId());
        assertNotNull(enriched);
        assertTrue(enriched.getContent().toString().contains("context: enrichedBy=ContextEnricherTask"));
    }
}
