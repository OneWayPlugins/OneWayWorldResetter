package net.onewaycraft.owwr.core.schedule;

import net.onewaycraft.owwr.api.ResourceWorld;
import net.onewaycraft.owwr.core.reset.ResetService;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @brief Loop periódico que dispara resets quando o Schedule de cada mundo chega.
 *
 * Roda no thread global a cada 30s; para cada mundo, calcula nextReset e
 * compara com Instant.now() — se passou, enfileira reset.
 */
public final class ResetSchedulerLoop {

    private final Scheduler scheduler;
    private final ResetService reset;
    private final Map<String, ResourceWorld> worlds;
    private final ZoneId zone;
    private final Logger logger;
    private final Map<String, Instant> lastFired = new ConcurrentHashMap<>();
    private Scheduler.Cancellable task;

    public ResetSchedulerLoop(Scheduler scheduler, ResetService reset,
                              Map<String, ResourceWorld> worlds, ZoneId zone, Logger logger) {
        this.scheduler = scheduler;
        this.reset = reset;
        this.worlds = worlds;
        this.zone = zone;
        this.logger = logger;
    }

    public void start() {
        task = scheduler.runRepeatingGlobal(Duration.ofSeconds(10), Duration.ofSeconds(30), this::tick);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    private void tick() {
        Instant now = Instant.now();
        for (ResourceWorld w : worlds.values()) {
            if (!w.enabled()) continue;
            try {
                Instant next = CronEvaluator.nextReset(w.reset().schedule(), now, zone);
                Instant last = lastFired.get(w.id());
                if (last != null && !next.isAfter(last)) continue;
                if (next.isBefore(now) || next.equals(now)) {
                    lastFired.put(w.id(), next);
                    logger.info("Schedule fired for " + w.id() + " at " + next);
                    reset.request(w.id(), false);
                }
            } catch (Exception e) {
                logger.warning("Schedule check failed for " + w.id() + ": " + e.getMessage());
            }
        }
    }
}
