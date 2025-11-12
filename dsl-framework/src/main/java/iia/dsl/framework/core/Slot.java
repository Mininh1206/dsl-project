package iia.dsl.framework.core;

import java.util.LinkedList;
import java.util.Queue;

import org.w3c.dom.Document;

public class Slot extends Element {
    private final Queue<Message> messages;
    
    public Slot(String id) {
        super(id);
        this.messages = new LinkedList<>();
    }

    public Slot() {
        super();
        this.messages = new LinkedList<>();
    }
    
    // -- MÉTODOS PARA MENSAJE --
    
    /**
     * Establece el mensaje en el slot.
     */
    public void setMessage(Message message) {
        this.messages.add(message);
    }
    
    /**
     * Obtiene el mensaje del slot.
     */
    public Message getMessage() {
        return messages.peek();
    }

    public Message getAndRemoveMessage() {
        return messages.poll();
    }

    public int getMessageCount() {
        return messages.size();
    }
    
    // --- MÉTODOS DE RETROCOMPATIBILIDAD PARA DOCUMENT ---
    
    /**
     * Obtiene el documento del mensaje actual.
     */
    public Document getDocument() {
        return messages.peek() != null ? messages.peek().getDocument() : null;
    }

    /**
     * Establece el documento en el mensaje actual.
     */
    public void setDocument(Document document) {
        if (!messages.isEmpty()) {
            messages.peek().setDocument(document);
        } else {
            messages.add(new Message(document));
        }
    }
    
    /**
     * Obtiene el ID del mensaje actual.
     */
    public String getMessageId() {
        return messages.peek() != null ? messages.peek().getId() : null;
    }
    
    /**
     * Verifica si el slot tiene un mensaje.
     */
    public boolean hasMessage() {
        return !messages.isEmpty();
    }
    
    /**
     * Limpia el mensaje del slot.
     */
    public void clear() {
        messages.clear();
    }
}
