package iia.dsl.framework.core;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * Encapsula un documento XML y sus metadatos (headers) para ser transportado a
 * través de los Slots.
 * Es la unidad básica de información que fluye por el sistema.
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
        super();
        this.document = doc;
        this.headers = new HashMap<>();
    }

    public Message(Document doc, Map<String, String> headers) {
        super();
        this.document = doc;
        this.headers = new HashMap<>(headers);
    }

    public Message(Message other) {
        super(other.id);
        this.document = other.document;
        this.headers = new HashMap<>(other.headers);
    }

    /**
     * Obtiene el documento XML contenido en el mensaje.
     * 
     * @return El objeto Document.
     */
    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Añade o actualiza una cabecera (metadato) en el mensaje.
     * 
     * @param key   Clave de la cabecera.
     * @param value Valor de la cabecera.
     */
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * Obtiene el valor de una cabecera específica.
     * 
     * @param key La clave de la cabecera.
     * @return El valor asociado o null si no existe.
     */
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
}