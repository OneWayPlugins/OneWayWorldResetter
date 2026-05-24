package net.onewaycraft.owwr.core.reset;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @brief Fila FIFO com limite de execuções concorrentes.
 */
public final class ResetQueue {

    private final int maxConcurrent;
    private final Queue<String> pending = new LinkedList<>();
    private final AtomicInteger running = new AtomicInteger();

    public ResetQueue(int maxConcurrent) {
        this.maxConcurrent = Math.max(1, maxConcurrent);
    }

    public synchronized boolean enqueue(String worldId) {
        if (pending.contains(worldId)) return false;
        pending.add(worldId);
        return true;
    }

    public synchronized String pollIfSlotAvailable() {
        if (running.get() >= maxConcurrent) return null;
        return pending.poll();
    }

    public void markStarted() { running.incrementAndGet(); }
    public void markFinished() { running.decrementAndGet(); }
    public int pendingSize() { return pending.size(); }
    public int runningCount() { return running.get(); }
}
