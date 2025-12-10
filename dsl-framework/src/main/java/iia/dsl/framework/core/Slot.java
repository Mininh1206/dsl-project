package iia.dsl.framework.core;

// import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Canal de comunicación asíncrono entre Elementos Ejecutables.
 * Actúa como una cola de mensajes y notifica a los listeners (Observers) cuando
 * hay nuevos datos disponibles.
 */
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
     * Coloca un nuevo mensaje en el Slot y notifica inmediatamente a todos
     * los listeners registrados.
     * 
     * @param message El mensaje a depositar.
     */
    public void setMessage(Message message) {
        this.messages.add(message);
        notifyListeners();
    }

    /**
     * Extrae y devuelve el siguiente mensaje disponible en el Slot.
     * Operación thread-safe.
     * 
     * @return El mensaje extraído o null si está vacío.
     */
    public Message getMessage() {
        return messages.poll();
    }

    public int getMessageCount() {
        return messages.size();
    }

    /**
     * Comprueba si hay mensajes pendientes en el Slot.
     * 
     * @return true si la cola no está vacía.
     */
    public boolean hasMessage() {
        return !messages.isEmpty();
    }

    // -- OBSERVER METHODS --

    /**
     * Registra un listener para ser notificado cuando lleguen mensajes.
     * Si el slot ya tiene mensajes, notifica al listener inmediatamente.
     * 
     * @param listener El objeto que implementa SlotListener.
     */
    public void addListener(SlotListener listener) {
        listeners.add(listener);
        // Si ya hay mensajes al registrarse, notificamos inmediatamente
        // para que no se queden estancados si el registro ocurre tarde.
        if (!messages.isEmpty()) {
            listener.onMessageAvailable(this);
        }
    }

    /**
     * Elimina un listener de la lista de notificaciones.
     * 
     * @param listener El listener a remover.
     */
    public void removeListener(SlotListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (SlotListener listener : listeners) {
            listener.onMessageAvailable(this);
        }
    }
}
