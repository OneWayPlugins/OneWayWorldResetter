package net.onewaycraft.owwr.plugin;

import net.onewaycraft.owwr.api.*;
import net.onewaycraft.owwr.core.persistence.HistoryRepository;
import net.onewaycraft.owwr.core.persistence.StateRepository;
import net.onewaycraft.owwr.core.reset.ResetService;
import net.onewaycraft.owwr.core.schedule.CronEvaluator;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

public final class OwwrServiceImpl implements OwwrService {

    private final OwwrConfig config;
    private final ResetService reset;
    private final StateRepository state;
    private final HistoryRepository history;
    private final ZoneId zone;

    public OwwrServiceImpl(OwwrConfig config, ResetService reset,
                           StateRepository state, HistoryRepository history, ZoneId zone) {
        this.config = config;
        this.reset = reset;
        this.state = state;
        this.history = history;
        this.zone = zone;
    }

    @Override public List<ResourceWorld> worlds() { return config.worlds(); }

    @Override
    public Optional<Instant> nextResetOf(String worldId) {
        return config.worlds().stream()
            .filter(w -> w.id().equalsIgnoreCase(worldId))
            .findFirst()
            .map(w -> CronEvaluator.nextReset(w.reset().schedule(), Instant.now(), zone));
    }

    @Override
    public void requestReset(String worldId, boolean dryRun) {
        reset.request(worldId, dryRun);
    }

    @Override
    public Optional<ResetState> currentState(String worldId) {
        return state.load(worldId);
    }

    @Override
    public List<ResetRecord> recentHistory(int limit) {
        return history.recent(limit);
    }
}
