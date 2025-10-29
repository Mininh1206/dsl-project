package dsl.project.model;

import java.util.LinkedList;
import java.util.Queue;

public class Slot {
    private final Queue<Document> buffer = new LinkedList<>();

    public synchronized void write(Document doc) {
        buffer.add(doc);
    }

    public synchronized Document read() {
        return buffer.poll();
    }

    public synchronized boolean isEmpty() {
        return buffer.isEmpty();
    }
}
