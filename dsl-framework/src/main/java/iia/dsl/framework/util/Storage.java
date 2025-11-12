package iia.dsl.framework.util;

import org.w3c.dom.Document;
import java.util.HashMap;
import java.util.Map;

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
        return documentStore.get(key);
    }
   
    public Document getDocument(String key) {
        return retrieveDocument(key);
    }

    public void removeDocument(String key) {
        documentStore.remove(key);
    }
}