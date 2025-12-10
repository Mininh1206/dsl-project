package iia.dsl.framework.ports;

import java.util.Optional;

import iia.dsl.framework.core.Element;

/**
 * Clase abstracta que define un puerto en el framework.
 * Los puertos son los puntos de entrada y salida para los conectores,
 * permitiendo
 * la interacción con el flujo de mensajes.
 * Pueden tener una transformación XSLT opcional asociada.
 */
public abstract class Port extends Element {
    protected final Optional<String> xslt;

    /**
     * Constructor por defecto. Inicializa sin XSLT.
     */
    public Port() {
        super();
        this.xslt = Optional.empty();
    }

    /**
     * Constructor con ID y XSLT.
     * 
     * @param id   Identificador del puerto.
     * @param xslt Transformación XSLT a aplicar (puede ser null).
     */
    public Port(String id, String xslt) {
        super(id);
        this.xslt = Optional.ofNullable(xslt);
    }

    /**
     * Constructor con ID.
     * 
     * @param id Identificador del puerto.
     */
    public Port(String id) {
        super(id);
        this.xslt = Optional.empty();
    }

    /**
     * Obtiene la transformación XSLT asociada.
     * 
     * @return Un Optional con el string XSLT si existe.
     */
    public Optional<String> getXslt() {
        return xslt;
    }
}
