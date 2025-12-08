package iia.dsl.framework.core;

// import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Slot extends Element {
    // Thread-safe queue
    private final Queue<Message> messages;

    // Observer pattern
    private final List<SlotListener> listeners = new CopyOnWriteArrayList<>();

    public Slot(String id) {
        super(id);
        this.messages = new ConcurrentLinkedQueue<>();
    }

    public Slot() {
        super();
        this.messages = new ConcurrentLinkedQueue<>();
    }

    // -- MÉTODOS PARA MENSAJE --

    /**
     * Establece el mensaje en el slot y notifica a los listeners.
     */
    public void setMessage(Message message) {
        this.messages.add(message);
        notifyListeners();
    }

    /**
     * Obtiene el mensaje del slot (thread-safe).
     */
    public Message getMessage() {
        return messages.poll();
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

    // -- OBSERVER METHODS --

    public void addListener(SlotListener listener) {
        listeners.add(listener);
        // Si ya hay mensajes al registrarse, notificamos inmediatamente
        // para que no se queden estancados si el registro ocurre tarde.
        if (!messages.isEmpty()) {
            listener.onMessageAvailable(this);
        }
    }

    public void removeListener(SlotListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (SlotListener listener : listeners) {
            listener.onMessageAvailable(this);
        }
    }
}
