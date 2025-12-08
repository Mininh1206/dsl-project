package iia.dsl.framework.core;

/**
 * Interfaz para componentes que deben ser notificados cuando un Slot recibe un
 * mensaje.
 */
public interface SlotListener {
    void onMessageAvailable(Slot slot);
}
