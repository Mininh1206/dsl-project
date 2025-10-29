package dsl.project.connectors;

import dsl.project.ports.InputPort;
import dsl.project.ports.OutputPort;
import dsl.project.ports.RequestPort;
import dsl.project.ports.Port;

import java.util.ArrayList;
import java.util.List;

public class Connector {
    private final List<Port> ports = new ArrayList<>();

    public void addPort(Port p) {
        ports.add(p);
    }

    public List<Port> getPorts() {
        return ports;
    }

    // Execute all ports managed by this connector
    public void execute() {
        for (Port p : ports) {
            if (p instanceof InputPort) {
                // input ports expect external data; nothing to do by default
            } else if (p instanceof OutputPort) {
                ((OutputPort)p).execute();
            } else if (p instanceof RequestPort) {
                ((RequestPort)p).execute();
            }
        }
    }
}
