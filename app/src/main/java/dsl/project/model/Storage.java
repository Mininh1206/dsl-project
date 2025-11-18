package dsl.project.model;

import java.util.HashMap;
import java.util.Map;

public class Storage {
    private static Storage instance;
    private final Map<String, Document> documents = new HashMap<>();

    private Storage() {}

    public static Storage getInstance() {
        if (instance == null) instance = new Storage();
        return instance;
    }

    public void storeDocument(String id, Document doc) {
        documents.put(id, doc);
    }

    public Document getDocument(String id) {
        return documents.get(id);
    }

    public boolean hasDocument(String id) {
        return documents.containsKey(id);
    }

    public void clear() {
        documents.clear();
    }
}
