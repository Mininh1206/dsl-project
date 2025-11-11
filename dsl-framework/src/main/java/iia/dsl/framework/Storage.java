package iia.dsl.framework;

import org.w3c.dom.Document;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton para almacenamiento en memoria de Documentos XML y listas de Secuencia.
 * Simula un almacén global accesible por todas las tareas del Flow.
 */
public class Storage {
    
    private static Storage instance;
    private final Map<String, Document> documentStore;
    private final Map<String, List<Integer>> sequenceStore;

    
    private Storage() {
        this.documentStore = Collections.synchronizedMap(new HashMap<>());
        this.sequenceStore = Collections.synchronizedMap(new HashMap<>());
        System.out.println("Storage Singleton inicializado.");
    }
    
 
    public static synchronized Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

   
    public void storeDocument(String key, Document document) {
        if (key == null || document == null) return;
        documentStore.put(key, document);
    }

   
    public Document retrieveDocument(String key) {
        return documentStore.get(key);
    }
   
    public Document getDocument(String key) {
        return retrieveDocument(key);
    }

    public void removeDocument(String key) {
        documentStore.remove(key);
    }

    public void storePartSequence(String sequenceKey, List<Integer> partIndices) {
        if (sequenceKey == null || partIndices == null) return;
        sequenceStore.put(sequenceKey, partIndices);
    }
    public List<Integer> retrievePartSequence(String sequenceKey) {
        return sequenceStore.get(sequenceKey);
    }
    
    public void removePartSequence(String sequenceKey) {
        sequenceStore.remove(sequenceKey);
    }
}