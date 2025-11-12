package iia.dsl.framework.tasks;

import java.util.List;

import iia.dsl.framework.core.Slot;
import iia.dsl.framework.tasks.routers.Correlator;
import iia.dsl.framework.tasks.routers.Filter;
import iia.dsl.framework.tasks.routers.Merger;

public class RouterFactory extends TaskFactory {
    public Task createFilterTask(String id, Slot inputSlot, Slot outputSlot, String xpath) {
        return new Filter(id, inputSlot, outputSlot, xpath);
    }

    public Task createCorrelatorTask(String id, List<Slot> inputSlot, List<Slot> outputSlot, String correlationXPath) {
        return new Correlator(id, inputSlot, outputSlot, correlationXPath);
    }

    public Task createCorrelatorTask(String id, List<Slot> inputSlot, List<Slot> outputSlot) {
        return new Correlator(id, inputSlot, outputSlot);
    }

    public Task createMergerTask(String id, java.util.List<Slot> inputSlots, Slot outputSlot) {
        return new Merger(id, inputSlots, outputSlot);
    }

    public Task createDistributorTask(String id, Slot inputSlot, java.util.List<Slot> outputSlots) {
        throw new UnsupportedOperationException("Distributor not implemented yet");
    }

    public Task createReplicatorTask(String id, Slot inputSlot, java.util.List<Slot> outputSlots) {
        throw new UnsupportedOperationException("Replicator not implemented yet");
    }

    public Task createThreaderTask(String id, Slot inputSlot, Slot outputSlot) {
        throw new UnsupportedOperationException("Threader not implemented yet");
    }
}
