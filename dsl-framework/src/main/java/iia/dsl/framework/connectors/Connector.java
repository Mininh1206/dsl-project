package iia.dsl.framework.connectors;

import iia.dsl.framework.core.ExecutableElement;
import iia.dsl.framework.ports.Port;

/**
 * Clase base abstracta para todos los conectores.
 * Un conector actúa como puente entre el framework DSL y sistemas externos
 * (archivos, BD, HTTP, consola).
 * Se asocia a un Puerto (Port) específico para entrada, salida o
 * petición-respuesta.
 */
public abstract class Connector extends ExecutableElement {
    protected Port port;

    /**
     * Verifica si este conector actúa como una fuente de datos (Input).
     * 
     * @return true si el puerto asociado es un InputPort.
     */
    public boolean isSource() {
        return port instanceof iia.dsl.framework.ports.InputPort;
    }

    /**
     * Constructor para inicializar un conector con un ID y un puerto.
     * 
     * @param id   Identificador único del conector.
     * @param port El puerto a través del cual fluyen los datos.
     * @throws IllegalArgumentException Si el puerto es null.
     */
    public Connector(String id, Port port) {
        super(id);

        if (port == null) {
            throw new IllegalArgumentException("Port no puede ser null");
        }

        this.port = port;
        // Registro automático como listener si es un puerto de salida o request
        // para reaccionar cuando lleguen datos.
        if (port instanceof iia.dsl.framework.ports.OutputPort) {
            ((iia.dsl.framework.ports.OutputPort) port).getInputSlot().addListener(this);
        } else if (port instanceof iia.dsl.framework.ports.RequestPort) {
            ((iia.dsl.framework.ports.RequestPort) port).getInputSlot().addListener(this);
        }
    }

    public Connector(Port port) {
        super();

        if (port == null) {
            throw new IllegalArgumentException("Port no puede ser null");
        }

        this.port = port;
        if (port instanceof iia.dsl.framework.ports.OutputPort) {
            ((iia.dsl.framework.ports.OutputPort) port).getInputSlot().addListener(this);
        } else if (port instanceof iia.dsl.framework.ports.RequestPort) {
            ((iia.dsl.framework.ports.RequestPort) port).getInputSlot().addListener(this);
        }
    }

    /**
     * Ejecuta la lógica específica del conector.
     * - Si es Input: Obtiene datos externos y los inyecta en el puerto.
     * - Si es Output: Toma datos del puerto y los envía al sistema externo.
     * 
     * @throws Exception Si ocurre un error de comunicación o procesamiento.
     */
    @Override
    public abstract void execute() throws Exception;
}
