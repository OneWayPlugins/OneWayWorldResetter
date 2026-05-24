package net.onewaycraft.owwr.folia.schedule;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.onewaycraft.owwr.core.schedule.RegionRef;
import net.onewaycraft.owwr.core.schedule.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @brief Scheduler para Folia (Global/Region/Async).
 *
 * Importante: world ops devem usar runGlobal (GlobalRegionScheduler) ou
 * delegar a um world-manager Folia-nativo; entidades/blocos em runAtRegion.
 */
public final class FoliaScheduler implements Scheduler {

    private final Plugin plugin;
    private final Set<ScheduledTask> tasks = ConcurrentHashMap.newKeySet();

    public FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Cancellable runGlobal(Runnable task) {
        return track(Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run()));
    }

    @Override
    public Cancellable runAsync(Runnable task) {
        return track(Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run()));
    }

    @Override
    public Cancellable runDelayedGlobal(Duration delay, Runnable task) {
        return track(Bukkit.getGlobalRegionScheduler()
            .runDelayed(plugin, t -> task.run(), Math.max(1L, delay.toMillis() / 50L)));
    }

    @Override
    public Cancellable runRepeatingGlobal(Duration initialDelay, Duration period, Runnable task) {
        long initTicks = Math.max(1L, initialDelay.toMillis() / 50L);
        long periodTicks = Math.max(1L, period.toMillis() / 50L);
        return track(Bukkit.getGlobalRegionScheduler()
            .runAtFixedRate(plugin, t -> task.run(), initTicks, periodTicks));
    }

    @Override
    public Cancellable runAtRegion(RegionRef region, Runnable task) {
        FoliaRegionRef ref = (FoliaRegionRef) region;
        return track(Bukkit.getRegionScheduler()
            .run(plugin, ref.resolveWorld(), ref.chunkX(), ref.chunkZ(), t -> task.run()));
    }

    @Override
    public void shutdown() {
        tasks.forEach(t -> {
            try { t.cancel(); } catch (Throwable ignored) {}
        });
        tasks.clear();
    }

    private Cancellable track(ScheduledTask t) {
        tasks.add(t);
        return new Cancellable() {
            @Override public void cancel() { t.cancel(); tasks.remove(t); }
            @Override public boolean isCancelled() { return t.isCancelled(); }
        };
    }
}
