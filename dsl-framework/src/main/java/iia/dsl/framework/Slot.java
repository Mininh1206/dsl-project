package iia.dsl.framework;

import java.util.UUID;

import org.w3c.dom.Document;

public class Slot extends Element {
    private Message message;
    
    public Slot(String id) {
        super(id);
    }
    
    // -- MÉTODOS PARA MENSAJE --
    
    /**
     * Establece el mensaje en el slot.
     */
    public void setMessage(Message message) {
        this.message = message;
    }
    
    /**
     * Obtiene el mensaje del slot.
     */
    public Message getMessage() {
        return message;
    }
    
    // --- MÉTODOS DE RETROCOMPATIBILIDAD PARA DOCUMENT ---
    
    /**
     * Establece un documento con ID generado automáticamente.
     */
    public void setDocument(Document document) {
        if (document != null) {
            this.message = new Message(generateId(), document);
        } else {
            this.message = null;
        }
    }
    
    /**
     * Obtiene el documento del mensaje actual.
     */
    public Document getDocument() {
        return message != null ? message.getDocument() : null;
    }
    
    /**
     * Obtiene el ID del mensaje actual.
     */
    public String getMessageId() {
        return message != null ? message.getId() : null;
    }
    
    /**
     * Verifica si el slot tiene un mensaje.
     */
    public boolean hasMessage() {
        return message != null;
    }
    
    /**
     * Limpia el mensaje del slot.
     */
    public void clear() {
        message = null;
    }
    
    // --- UTILIDADES ---
    
    /**
     * Genera un ID único para mensajes.
     */
    private String generateId() {
        return "msg-" + UUID.randomUUID().toString().substring(0, 8);
    }
}