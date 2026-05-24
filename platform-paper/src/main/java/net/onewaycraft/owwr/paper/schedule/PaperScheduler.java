package net.onewaycraft.owwr.paper.schedule;

import net.onewaycraft.owwr.core.schedule.RegionRef;
import net.onewaycraft.owwr.core.schedule.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @brief Scheduler para Paper (single main thread + BukkitScheduler async pool).
 */
public final class PaperScheduler implements Scheduler {

    private final Plugin plugin;
    private final ConcurrentHashMap<Long, BukkitTask> tasks = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public PaperScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Cancellable runGlobal(Runnable task) {
        return track(Bukkit.getScheduler().runTask(plugin, task));
    }

    @Override
    public Cancellable runAsync(Runnable task) {
        return track(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    public Cancellable runDelayedGlobal(Duration delay, Runnable task) {
        return track(Bukkit.getScheduler().runTaskLater(plugin, task, toTicks(delay)));
    }

    @Override
    public Cancellable runRepeatingGlobal(Duration initialDelay, Duration period, Runnable task) {
        return track(Bukkit.getScheduler().runTaskTimer(
            plugin, task, toTicks(initialDelay), toTicks(period)));
    }

    @Override
    public Cancellable runAtRegion(RegionRef region, Runnable task) {
        // Paper não regionaliza: cai no main thread.
        return runGlobal(task);
    }

    @Override
    public void shutdown() {
        tasks.values().forEach(BukkitTask::cancel);
        tasks.clear();
    }

    private long toTicks(Duration d) {
        return Math.max(1L, d.toMillis() / 50L);
    }

    private Cancellable track(BukkitTask task) {
        long id = nextId.getAndIncrement();
        tasks.put(id, task);
        return new Cancellable() {
            @Override public void cancel() {
                BukkitTask t = tasks.remove(id);
                if (t != null) t.cancel();
            }
            @Override public boolean isCancelled() { return task.isCancelled(); }
        };
    }
}
