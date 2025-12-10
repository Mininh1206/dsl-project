package iia.dsl.framework.core;

import java.util.UUID;

/**
 * Clase base abstracta para todos los elementos del framework DSL.
 * Proporciona una identificación única (UUID) para cada componente.
 */
public abstract class Element {
    protected final String id;

    public Element(String id) {
        this.id = id;
    }

    public Element() {
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Obtiene el identificador único del elemento.
     * 
     * @return El ID del elemento.
     */
    public String getId() {
        return id;
    }
}
