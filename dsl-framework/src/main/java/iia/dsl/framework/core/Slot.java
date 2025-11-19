package iia.dsl.framework.core;

import java.util.LinkedList;
import java.util.Queue;

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
        return messages.poll();
    }

    /**
     * Devuelve el siguiente mensaje sin consumirlo.
     */
    public Message peekMessage() {
        return messages.peek();
    }

    public int getMessageCount() {
        return messages.size();
    }
    
    /**
     * Verifica si el slot tiene un mensaje.
     */
    public boolean hasMessage() {
        return !messages.isEmpty();
    }
}
