package iia.dsl.framework.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;

/**
 * Singleton para almacenamiento temporal en memoria.
 * 
 * <p>
 * Actúa como una "memoria compartida" accesible por todas las tareas del
 * framework.
 * Es crucial para componentes que necesitan persistir estado intermedio, como
 * el {@link iia.dsl.framework.tasks.transformers.Splitter}
 * almacenando el documento esqueleto para el
 * {@link iia.dsl.framework.tasks.transformers.Aggregator}.
 * 
 * <p>
 * Es thread-safe gracias al uso de mapas concurrentes.
 */
public class Storage {

    private static Storage instance;
    private final Map<String, Document> documentStore;

    private Storage() {
        this.documentStore = new ConcurrentHashMap<>();
        System.out.println("Storage Singleton inicializado.");
    }

    public static synchronized Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    public void storeDocument(String key, Document document) {
        if (key == null || document == null)
            return;
        documentStore.put(key, document);
    }

    public Document retrieveDocument(String key) {
        if (key == null)
            return null;

        var doc = documentStore.get(key);
        documentStore.remove(key);

        return doc;
    }

    public void removeDocument(String key) {
        documentStore.remove(key);
    }
}