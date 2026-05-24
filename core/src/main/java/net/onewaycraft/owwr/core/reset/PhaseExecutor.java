package net.onewaycraft.owwr.core.reset;

import net.onewaycraft.owwr.api.ResetPhase;
import net.onewaycraft.owwr.api.ResetState;
import net.onewaycraft.owwr.core.persistence.StateRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * @brief Executa fases sequenciais persistindo ResetState a cada transição.
 *        Permite ao caller medir duração de cada fase e abortar.
 */
public final class PhaseExecutor {

    private final StateRepository repo;
    private final String worldId;
    private final int attempt;
    private final Map<ResetPhase, Duration> phaseDurations = new EnumMap<>(ResetPhase.class);
    private final Instant startedAt;

    public PhaseExecutor(StateRepository repo, String worldId, int attempt, Instant startedAt) {
        this.repo = repo;
        this.worldId = worldId;
        this.attempt = attempt;
        this.startedAt = startedAt;
    }

    /**
     * @brief Executa uma fase persistindo o estado antes; retorna duração.
     */
    public Duration run(ResetPhase phase, BooleanSupplier action) {
        Instant t0 = Instant.now();
        repo.save(new ResetState(worldId, phase, attempt, startedAt, t0));
        boolean ok = action.getAsBoolean();
        Duration d = Duration.between(t0, Instant.now());
        phaseDurations.put(phase, d);
        if (!ok) throw new PhaseFailure(phase, "phase " + phase + " failed");
        return d;
    }

    public Map<ResetPhase, Duration> phaseDurations() {
        return Map.copyOf(phaseDurations);
    }

    public static final class PhaseFailure extends RuntimeException {
        private final ResetPhase phase;
        public PhaseFailure(ResetPhase phase, String msg) { super(msg); this.phase = phase; }
        public ResetPhase phase() { return phase; }
    }
}
