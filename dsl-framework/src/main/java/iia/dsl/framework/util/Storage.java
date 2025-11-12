package iia.dsl.framework.util;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * Singleton para almacenamiento en memoria de Documentos XML y listas de Secuencia.
 * Simula un almacén global accesible por todas las tareas del Flow.
 */
public class Storage {
    
    private static Storage instance;
    private final Map<String, Document> documentStore;

    
    private Storage() {
        this.documentStore = new HashMap<>();
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
        if (key == null) return null;

        var doc = documentStore.get(key);
        documentStore.remove(key);
        
        return doc;
    }

    public void removeDocument(String key) {
        documentStore.remove(key);
    }
}