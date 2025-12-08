package iia.dsl.framework.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ExecutableElement extends Element implements Runnable, SlotListener {
    // Counts the number of pending execution requests (messages)
    protected final AtomicInteger workCount = new AtomicInteger(0);
    protected boolean concurrent = false;

    public ExecutableElement() {
        super();
    }

    public ExecutableElement(String id) {
        super(id);
    }

    public abstract void execute() throws Exception;

    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    public boolean isConcurrent() {
        return concurrent;
    }

    @Override
    public void onMessageAvailable(Slot slot) {
        if (concurrent) {
            scheduleExecution();
        }
    }

    protected void scheduleExecution() {
        // Increment work count. If we were at 0, we need to kickstart the thread.
        // If we were > 0, the running thread will pick it up on its next loop.
        if (workCount.getAndIncrement() == 0) {
            ExecutionEnvironment.getInstance().submit(this);
        }
    }

    @Override
    public void run() {
        try {
            do {
                try {
                    execute();
                } catch (Exception ex) {
                    System.err.println("[ERROR] Error executing element " + (id != null ? id : "unnamed") + ": "
                            + ex.getMessage());
                    ex.printStackTrace();
                    Logger.getLogger(ExecutableElement.class.getName()).log(Level.SEVERE,
                            "Error executing element " + id, ex);
                }
                // Decrement and check if execution needed again.
            } while (workCount.decrementAndGet() > 0);

        } catch (Exception e) {
            System.err.println("[CRITICAL] Catastrophic failure in element " + (id != null ? id : "unnamed"));
            e.printStackTrace();
            // Reset in case of catastrophic failure to avoid stuck state
            workCount.set(0);
        }
    }
}
