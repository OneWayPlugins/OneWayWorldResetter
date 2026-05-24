package net.onewaycraft.owwr.core.reset;

import net.onewaycraft.owwr.api.*;
import net.onewaycraft.owwr.core.pregen.PregenObserver;
import net.onewaycraft.owwr.core.pregen.PregenService;
import net.onewaycraft.owwr.core.pregen.PregenSpec;
import net.onewaycraft.owwr.core.preflight.ResetContext;
import net.onewaycraft.owwr.core.preflight.ServerSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PregenResetStrategyDecoratorTest {

    private ResourceWorld worldWith(ChunkyConfig chunky) {
        ResetConfig rc = new ResetConfig(
            "in-place",
            Schedule.daily(java.time.LocalTime.NOON),
            List.of(),
            Map.of(),
            true, true,
            chunky
        );
        return new ResourceWorld("mining", "world_resource", Environment.NORMAL, true, true,
            new SeedConfig(SeedStrategy.RANDOM, List.of()),
            rc,
            new TeleportConfig("spawn", true, "world"),
            new WorldSettings("NORMAL", Map.of(), Optional.empty()),
            new ResourceWorld.RegionConfig(false, List.of()),
            Map.of(), Map.of());
    }

    private ChunkyConfig chunkyEnabled(String failureBehavior, int maxMin) {
        return new ChunkyConfig(true, "square", 0, 0, 1000, maxMin, true, failureBehavior, Map.of());
    }

    private ResetResult success(String worldId, ResetPhase finalPhase) {
        return new ResetResult(worldId, true, finalPhase, Duration.ofSeconds(5),
            Map.of(finalPhase, Duration.ofSeconds(5)), 0, null);
    }

    /** Fake strategy that simply returns a canned ResetResult. */
    private static final class FakeStrategy implements ResetStrategy {
        private final ResetResult result;
        FakeStrategy(ResetResult result) { this.result = result; }
        @Override public String id() { return "fake"; }
        @Override public ResetResult execute(ResetContext ctx, PhaseExecutor exec) { return result; }
    }

    /** Fake PregenService whose handle completes/fails based on configuration. */
    private static final class FakePregenService implements PregenService {
        boolean completes; boolean throwsHard; boolean timesOut;
        @Override public PregenHandle start(PregenSpec spec, PregenObserver observer) {
            return new PregenHandle() {
                @Override public boolean awaitCompletion(Duration timeout) {
                    if (throwsHard) { observer.onFailed("simulated"); return true; }  // hard fail returns true (completed with failure)
                    if (timesOut) return false;
                    observer.onComplete(null);
                    return true;
                }
                @Override public void cancel() {}
            };
        }
        @Override public void cancel(String w) {}
        @Override public void pause(String w) {}
        @Override public void resume(String w) {}
        @Override public Optional<net.onewaycraft.owwr.api.PregenProgress> progressOf(String w) { return Optional.empty(); }
    }

    @Test
    void whenChunkyIsNullDelegateResultIsReturnedUnchanged() {
        ResourceWorld w = worldWith(null);
        ResetResult wrapped = success(w.id(), ResetPhase.COMPLETE);
        PregenResetStrategyDecorator dec = new PregenResetStrategyDecorator(
            new FakeStrategy(wrapped), new FakePregenService());

        ResetResult out = dec.execute(new ResetContext(w, snap(), false),
            new PhaseExecutor(noopRepo(), w.id(), 1, Instant.now()));

        assertThat(out).isSameAs(wrapped);  // pass-through
    }

    @Test
    void whenWrappedStrategyFailedDecoratorReturnsItVerbatim() {
        ResourceWorld w = worldWith(chunkyEnabled("critical", 1));
        ResetResult failed = new ResetResult(w.id(), false, ResetPhase.DELETE,
            Duration.ofSeconds(2), Map.of(), 0, "delete failed");
        PregenResetStrategyDecorator dec = new PregenResetStrategyDecorator(
            new FakeStrategy(failed), new FakePregenService());

        ResetResult out = dec.execute(new ResetContext(w, snap(), false),
            new PhaseExecutor(noopRepo(), w.id(), 1, Instant.now()));

        assertThat(out).isSameAs(failed);
    }

    @Test
    void successPathRunsPregenAndReturnsCompleteResult() {
        ResourceWorld w = worldWith(chunkyEnabled("critical", 1));
        ResetResult wrapped = success(w.id(), ResetPhase.COMPLETE);
        FakePregenService pregen = new FakePregenService();
        pregen.completes = true;

        PregenResetStrategyDecorator dec = new PregenResetStrategyDecorator(
            new FakeStrategy(wrapped), pregen);
        ResetResult out = dec.execute(new ResetContext(w, snap(), false),
            new PhaseExecutor(noopRepo(), w.id(), 1, Instant.now()));

        assertThat(out.success()).isTrue();
        assertThat(out.finalPhase()).isEqualTo(ResetPhase.COMPLETE);
        assertThat(out.failureReason()).isNull();
    }

    @Test
    void hardFailureFromPregenProducesFailedResult() {
        ResourceWorld w = worldWith(chunkyEnabled("critical", 1));
        ResetResult wrapped = success(w.id(), ResetPhase.COMPLETE);
        FakePregenService pregen = new FakePregenService();
        pregen.throwsHard = true;

        PregenResetStrategyDecorator dec = new PregenResetStrategyDecorator(
            new FakeStrategy(wrapped), pregen);
        ResetResult out = dec.execute(new ResetContext(w, snap(), false),
            new PhaseExecutor(noopRepo(), w.id(), 1, Instant.now()));

        assertThat(out.success()).isFalse();
        assertThat(out.finalPhase()).isEqualTo(ResetPhase.PREGEN);
        assertThat(out.failureReason()).contains("simulated");
    }

    @Test
    void timeoutWithFailureBehaviorWarningProducesPartialSuccess() {
        ResourceWorld w = worldWith(chunkyEnabled("warning", 1));
        ResetResult wrapped = success(w.id(), ResetPhase.COMPLETE);
        FakePregenService pregen = new FakePregenService();
        pregen.timesOut = true;

        PregenResetStrategyDecorator dec = new PregenResetStrategyDecorator(
            new FakeStrategy(wrapped), pregen);
        ResetResult out = dec.execute(new ResetContext(w, snap(), false),
            new PhaseExecutor(noopRepo(), w.id(), 1, Instant.now()));

        assertThat(out.success()).isTrue();
        assertThat(out.finalPhase()).isEqualTo(ResetPhase.COMPLETE);
        assertThat(out.failureReason()).contains("pregen-timed-out");
    }

    @Test
    void timeoutWithFailureBehaviorCriticalProducesFailedResult() {
        ResourceWorld w = worldWith(chunkyEnabled("critical", 1));
        ResetResult wrapped = success(w.id(), ResetPhase.COMPLETE);
        FakePregenService pregen = new FakePregenService();
        pregen.timesOut = true;

        PregenResetStrategyDecorator dec = new PregenResetStrategyDecorator(
            new FakeStrategy(wrapped), pregen);
        ResetResult out = dec.execute(new ResetContext(w, snap(), false),
            new PhaseExecutor(noopRepo(), w.id(), 1, Instant.now()));

        assertThat(out.success()).isFalse();
        assertThat(out.finalPhase()).isEqualTo(ResetPhase.PREGEN);
        assertThat(out.failureReason()).contains("timeout");
    }

    @Test
    void dryRunSkipsPregen() {
        ResourceWorld w = worldWith(chunkyEnabled("critical", 1));
        ResetResult wrapped = success(w.id(), ResetPhase.COMPLETE);
        FakePregenService pregen = new FakePregenService();
        // pregen.completes = false; would block — we expect decorator to NOT call start at all on dry-run.

        PregenResetStrategyDecorator dec = new PregenResetStrategyDecorator(
            new FakeStrategy(wrapped), pregen);
        ResetResult out = dec.execute(new ResetContext(w, snap(), true /* dryRun */),
            new PhaseExecutor(noopRepo(), w.id(), 1, Instant.now()));

        assertThat(out).isSameAs(wrapped);  // dry-run skips pregen
    }

    private ServerSnapshot snap() {
        return new ServerSnapshot(20.0, 0, Long.MAX_VALUE);
    }

    private net.onewaycraft.owwr.core.persistence.StateRepository noopRepo() {
        // Minimal repo that doesn't actually write to disk.
        // PhaseExecutor calls repo.save(...) before each phase action. We need a temp dir.
        try {
            return new net.onewaycraft.owwr.core.persistence.StateRepository(
                java.nio.file.Files.createTempDirectory("owwr-test"));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
