package net.onewaycraft.owwr.core.reset;

import net.onewaycraft.owwr.api.*;
import net.onewaycraft.owwr.api.event.*;
import net.onewaycraft.owwr.core.persistence.HistoryRepository;
import net.onewaycraft.owwr.core.persistence.StateRepository;
import net.onewaycraft.owwr.core.preflight.PreflightGate;
import net.onewaycraft.owwr.core.preflight.ResetContext;
import net.onewaycraft.owwr.core.preflight.ServerSnapshot;
import net.onewaycraft.owwr.core.schedule.Scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * @brief Orquestrador central de resets.
 *
 * Pipeline: PRECHECK → estratégia → registro em histórico → evento POST.
 * Crash recovery: ao construir, examina a StateRepository por estados não-terminais.
 */
public final class ResetService {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration RETRY_BACKOFF = Duration.ofSeconds(30);

    private final ResetQueue queue;
    private final Map<String, ResourceWorld> worlds;
    private final Map<String, ResetStrategy> strategies;
    private final List<PreflightGate> gates;
    private final Supplier<ServerSnapshot> snapshotSupplier;
    private final StateRepository state;
    private final HistoryRepository history;
    private final EventBus events;
    private final Scheduler scheduler;
    private final Logger logger;

    public ResetService(
        ResetQueue queue,
        Map<String, ResourceWorld> worlds,
        Map<String, ResetStrategy> strategies,
        List<PreflightGate> gates,
        Supplier<ServerSnapshot> snapshotSupplier,
        StateRepository state,
        HistoryRepository history,
        EventBus events,
        Scheduler scheduler,
        Logger logger
    ) {
        this.queue = queue;
        this.worlds = worlds;
        this.strategies = strategies;
        this.gates = gates;
        this.snapshotSupplier = snapshotSupplier;
        this.state = state;
        this.history = history;
        this.events = events;
        this.scheduler = scheduler;
        this.logger = logger;
    }

    public void request(String worldId, boolean dryRun) {
        if (!worlds.containsKey(worldId)) {
            logger.warning("unknown world: " + worldId);
            return;
        }
        if (!queue.enqueue(worldId)) {
            logger.info("world " + worldId + " already queued");
            return;
        }
        pump(dryRun);
    }

    public void pump(boolean dryRun) {
        String next;
        while ((next = queue.pollIfSlotAvailable()) != null) {
            String worldId = next;
            queue.markStarted();
            scheduler.runAsync(() -> {
                try { executeAttempt(worldId, 1, dryRun); }
                finally { queue.markFinished(); pump(dryRun); }
            });
        }
    }

    public void resumePending() {
        for (String id : worlds.keySet()) {
            state.load(id).ifPresent(s -> {
                if (s.phase() != ResetPhase.COMPLETE && s.phase() != ResetPhase.FAILED) {
                    logger.warning("resuming pending reset for " + id + " phase=" + s.phase());
                    scheduler.runDelayedGlobal(Duration.ofSeconds(60),
                        () -> request(id, false));
                }
            });
        }
    }

    private void executeAttempt(String worldId, int attempt, boolean dryRun) {
        ResourceWorld world = worlds.get(worldId);
        PreResetEvent pre = new PreResetEvent(worldId);
        if (events.fire(pre)) {
            logger.info("PreResetEvent cancelled for " + worldId);
            return;
        }
        ResetContext ctx = new ResetContext(world, snapshotSupplier.get(), dryRun);

        for (PreflightGate gate : gates) {
            GateResult r = gate.check(ctx);
            if (r instanceof GateResult.Delay d) {
                logger.info("gate " + gate.name() + " delay: " + d.reason());
                scheduler.runDelayedGlobal(Duration.ofMinutes(5), () -> request(worldId, dryRun));
                return;
            }
            if (r instanceof GateResult.Abort a) {
                logger.warning("gate " + gate.name() + " abort: " + a.reason());
                publishFailure(worldId, ResetPhase.PRECHECK, a.reason(), Instant.now(), 0);
                return;
            }
        }

        ResetStrategy strat = strategies.get(world.reset().strategy());
        if (strat == null) {
            publishFailure(worldId, ResetPhase.PRECHECK,
                "unknown strategy: " + world.reset().strategy(), Instant.now(), 0);
            return;
        }

        Instant started = Instant.now();
        PhaseExecutor exec = new PhaseExecutor(state, worldId, attempt, started);

        ResetResult result = strat.execute(ctx, exec);
        state.clear(worldId);
        history.append(new ResetRecord(worldId, started, result.totalDuration(),
            result.success(), result.finalPhase(), result.affectedPlayers(),
            result.failureReason()));
        events.fire(new PostResetEvent(result));

        if (!result.success() && attempt < MAX_ATTEMPTS) {
            logger.warning("reset failed (attempt " + attempt + "), retrying in "
                + RETRY_BACKOFF + ": " + result.failureReason());
            scheduler.runDelayedGlobal(RETRY_BACKOFF,
                () -> executeAttempt(worldId, attempt + 1, dryRun));
        }
    }

    private void publishFailure(String worldId, ResetPhase phase, String reason,
                                Instant started, int affected) {
        ResetResult result = new ResetResult(worldId, false, phase, Duration.ZERO,
            Map.of(), affected, reason);
        history.append(new ResetRecord(worldId, started, Duration.ZERO, false,
            phase, affected, reason));
        events.fire(new PostResetEvent(result));
    }
}
