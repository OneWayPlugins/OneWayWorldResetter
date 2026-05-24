package net.onewaycraft.owwr.core.reset;

import net.onewaycraft.owwr.api.ChunkyConfig;
import net.onewaycraft.owwr.api.ResetPhase;
import net.onewaycraft.owwr.api.ResetResult;
import net.onewaycraft.owwr.api.ResourceWorld;
import net.onewaycraft.owwr.core.pregen.PregenObserver;
import net.onewaycraft.owwr.core.pregen.PregenService;
import net.onewaycraft.owwr.core.pregen.PregenSpec;
import net.onewaycraft.owwr.core.preflight.ResetContext;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @brief Decorator que adiciona a fase PREGEN entre o wrapped strategy.execute e o COMPLETE final.
 *
 * Padrão Decorator: respeita OCP, não modifica InPlace/DoubleBuffered.
 *
 * Comportamento:
 * - Se ResetConfig.chunky == null → pass-through (retorna o ResetResult do wrapped).
 * - Se ResetContext.dryRun == true → pass-through (não roda pre-gen em dry-run).
 * - Se wrapped retornou failed → pass-through.
 * - Caso contrário, inicia pre-gen e aguarda awaitCompletion(maxDuration).
 *   - Sucesso: retorna ResetResult com finalPhase=COMPLETE.
 *   - Timeout + failure-behavior=warning: success=true, failureReason="pregen-timed-out".
 *   - Timeout + failure-behavior=critical: success=false, finalPhase=PREGEN, reason="pregen timeout".
 *   - Falha hard (observer.onFailed disparado): success=false, finalPhase=PREGEN, reason=<motivo>.
 */
public final class PregenResetStrategyDecorator implements ResetStrategy {

    private final ResetStrategy wrapped;
    private final PregenService pregen;

    public PregenResetStrategyDecorator(ResetStrategy wrapped, PregenService pregen) {
        this.wrapped = Objects.requireNonNull(wrapped, "wrapped");
        this.pregen = Objects.requireNonNull(pregen, "pregen");
    }

    @Override
    public String id() { return wrapped.id(); }

    @Override
    public ResetResult execute(ResetContext ctx, PhaseExecutor exec) {
        ResetResult inner = wrapped.execute(ctx, exec);
        if (inner == null) return null;

        ChunkyConfig chunky = ctx.world().reset().chunky();
        if (chunky == null) return inner;
        if (ctx.dryRun()) return inner;
        if (!inner.success()) return inner;

        return runPregenAndFinalize(inner, ctx, chunky);
    }

    private ResetResult runPregenAndFinalize(ResetResult inner, ResetContext ctx, ChunkyConfig chunky) {
        ResourceWorld rw = ctx.world();
        PregenSpec spec = new PregenSpec(
            rw.worldName(),
            chunky.shape(),
            chunky.centerX(),
            chunky.centerZ(),
            chunky.radius(),
            Duration.ofMinutes(chunky.maxDurationMinutes()),
            chunky.notifications());

        AtomicReference<String> failureReason = new AtomicReference<>();
        PregenObserver observer = new PregenObserver() {
            @Override public void onFailed(String reason) {
                failureReason.compareAndSet(null, reason);
            }
        };

        PregenService.PregenHandle handle = pregen.start(spec, observer);
        boolean completed = handle.awaitCompletion(Duration.ofMinutes(chunky.maxDurationMinutes()));

        String hardFailure = failureReason.get();
        if (hardFailure != null) {
            return failResult(inner, "pregen failed: " + hardFailure);
        }
        if (!completed) {
            handle.cancel();
            if ("warning".equals(chunky.failureBehavior())) {
                return warningResult(inner, "pregen-timed-out");
            }
            return failResult(inner, "pregen timeout after " + chunky.maxDurationMinutes() + " minutes");
        }

        // Success: rebuild result with PREGEN phase duration recorded (unknown, so omit).
        return inner;
    }

    private ResetResult failResult(ResetResult inner, String reason) {
        return new ResetResult(
            inner.worldId(),
            false,
            ResetPhase.PREGEN,
            inner.totalDuration(),
            inner.phaseDurations(),
            inner.affectedPlayers(),
            reason);
    }

    private ResetResult warningResult(ResetResult inner, String warningReason) {
        return new ResetResult(
            inner.worldId(),
            true,
            ResetPhase.COMPLETE,
            inner.totalDuration(),
            inner.phaseDurations(),
            inner.affectedPlayers(),
            warningReason);   // success=true but failureReason carries the warning text
    }
}
