package dsl.project;

import dsl.project.model.Document;
import dsl.project.model.Slot;
import dsl.project.model.Storage;
import dsl.project.tasks.SplitterTask;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SplitterTaskTest {
    @Test
    public void testSplitterStoresFragments() {
        Storage.getInstance().clear();
        Slot input = new Slot();
        Slot output = new Slot();
        String content = "A,B,C";
        Document doc = new Document("doc1", content);
        input.write(doc);
        SplitterTask splitter = new SplitterTask();
        splitter.addInputSlot(input);
        splitter.addOutputSlot(output);
        splitter.execute();
        // Debe haber 3 fragmentos en Storage
        int count = 0;
        for (String id : new String[] {"A", "B", "C"}) {
            boolean found = Storage.getInstance().documents.values().stream()
                .anyMatch(d -> d.getContent().equals(id));
            if (found) count++;
        }
        assertEquals(3, count);
    }
}
