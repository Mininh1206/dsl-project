package dsl.project.tasks;

import dsl.project.ports.PortFactory;

public class TaskFactory {
    private final PortFactory portFactory;

    public TaskFactory(PortFactory portFactory) {
        this.portFactory = portFactory;
    }

    public CorrelatorTask createCorrelatorTask() {
        return new CorrelatorTask();
    }

    public FilterTask createFilterTask() {
        return new FilterTask();
    }

    public SplitterTask createSplitterTask() {
        return new SplitterTask();
    }

    public AggregatorTask createAggregatorTask() {
        return new AggregatorTask();
    }

    // ... more factory methods as needed
    public PortFactory getPortFactory() {
        return portFactory;
    }
}
