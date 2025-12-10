package iia.dsl.framework.core;

/**
 * Interfaz observador para componentes que desean reaccionar a la llegada de
 * mensajes
 * en un Slot.
 */
public interface SlotListener {
    /**
     * Método de callback invocado cuando un Slot tiene al menos un mensaje
     * disponible.
     * 
     * @param slot El slot que originó la notificación.
     */
    void onMessageAvailable(Slot slot);
}
