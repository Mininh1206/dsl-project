package iia.dsl.framework.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.w3c.dom.Document;
/**
 * Clase que encapsula Document con id para permitir identificación de splitter
 * @author javi
 */
public class Message extends Element {
    private Document document;
    private final Map<String, String> headers;
    public static final String CORRELATION_ID = "correlation-id";
    public static final String NUM_FRAG = "num-frag";
    public static final String TOTAL_FRAG = "total-frag";
    
    public Message(String id, Document document, Map<String, String> headers) {
        super(id);
        this.document = document;
        this.headers = new HashMap<>(headers);
    }

    public Message(String id, Document document) {
        super(id);
        this.document = document;
        this.headers = new HashMap<>();
    }
    
    public Message(Document doc) {
        super(generateId());
        this.document = doc;
        this.headers = new HashMap<>();
    }

    public Message(Message other) {
        super(other.id);
        this.document = other.document;
        this.headers = new HashMap<>(other.headers);
    }

    public Document getDocument() {
        return document;
    }
    
    public void setDocument(Document document) {
        this.document = document;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public void removeHeader(String key) {
        headers.remove(key);
    }

    public boolean hasHeader(String key) {
        return headers.containsKey(key);
    }

    public void clearHeaders() {
        headers.clear();
    }

    public boolean hasDocument() {
        return document != null;
    }

    private static String generateId() {
        return "msg-" + UUID.randomUUID().toString();
    }
}