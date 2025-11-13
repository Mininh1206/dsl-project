package iia.dsl.framework.tasks.routers;

import java.util.List;

import iia.dsl.framework.core.Slot;

public class RouterFactory {
    public Filter createFilterTask(String id, Slot inputSlot, Slot outputSlot, String xpath) {
        return new Filter(id, inputSlot, outputSlot, xpath);
    }

    public Correlator createCorrelatorTask(String id, List<Slot> inputSlot, List<Slot> outputSlot, String correlationXPath) {
        return new Correlator(id, inputSlot, outputSlot, correlationXPath);
    }

    public Correlator createCorrelatorTask(String id, List<Slot> inputSlot, List<Slot> outputSlot) {
        return new Correlator(id, inputSlot, outputSlot);
    }

    public Merger createMergerTask(String id, java.util.List<Slot> inputSlots, Slot outputSlot) {
        return new Merger(id, inputSlots, outputSlot);
    }

    public Distributor createDistributorTask(String id, Slot inputSlot, java.util.List<Slot> outputSlots) {
        throw new UnsupportedOperationException("Distributor not implemented yet");
    }

    public Replicator createReplicatorTask(String id, Slot inputSlot, java.util.List<Slot> outputSlots) {
        throw new UnsupportedOperationException("Replicator not implemented yet");
    }
}
