package net.onewaycraft.owwr.integrations.chunky;

import net.onewaycraft.owwr.api.PregenProgress;
import net.onewaycraft.owwr.api.PregenState;
import net.onewaycraft.owwr.core.pregen.PregenObserver;
import net.onewaycraft.owwr.core.pregen.PregenService;
import net.onewaycraft.owwr.core.pregen.PregenSpec;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;
import org.popcraft.chunky.api.event.task.GenerationCompleteEvent;
import org.popcraft.chunky.api.event.task.GenerationProgressEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @brief Implementacao real do PregenService delegando ao Chunky 1.4.36.
 *
 * Carregada apenas quando o plugin Chunky esta presente no servidor (ver
 * {@link ChunkyAdapterFactory#tryCreate}).
 *
 * <p><b>Desvios em relacao ao spec original:</b>
 * <ul>
 *   <li>A API publica do Chunky (ChunkyAPI) usa {@code String worldName} ao
 *       inves de {@code org.bukkit.World} em {@code startTask/cancelTask/etc}.
 *   </li>
 *   <li>O listener de progresso e {@link java.util.function.Consumer}, nao
 *       {@code BiConsumer}; o nome do mundo vem dentro do proprio evento.
 *   </li>
 *   <li>{@link GenerationProgressEvent} expoe somente {@code chunks()} (done)
 *       e {@code progress()} em percent 0..100 — nao ha campo {@code chunksTotal}.
 *       Calculamos o total estimado como {@code chunks / (progress/100)}.
 *   </li>
 *   <li>Conclusao chega num evento separado ({@link GenerationCompleteEvent}),
 *       nao pela flag {@code complete} de progresso.
 *   </li>
 *   <li>A instancia da API e obtida via {@link ChunkyProvider#get()} +
 *       {@code .getApi()} (Chunky nao se registra como Bukkit service).
 *   </li>
 * </ul>
 */
public final class ChunkyPregenService implements PregenService {

    private final Plugin owwrPlugin;
    private final Logger logger;
    private final ChunkyAPI api;
    private final Map<String, TaskState> tasks = new ConcurrentHashMap<>();

    ChunkyPregenService(Plugin owwrPlugin, Logger logger) {
        this.owwrPlugin = owwrPlugin;
        this.logger = logger;
        if (ChunkyProvider.get() == null) {
            throw new IllegalStateException("ChunkyProvider has no instance");
        }
        ChunkyAPI located = ChunkyProvider.get().getApi();
        if (located == null) {
            throw new IllegalStateException("ChunkyProvider.getApi() returned null");
        }
        this.api = located;
        this.api.onGenerationProgress(this::onProgressEvent);
        this.api.onGenerationComplete(this::onCompleteEvent);
    }

    @Override
    public PregenHandle start(PregenSpec spec, PregenObserver observer) {
        World world = Bukkit.getWorld(spec.worldName());
        if (world == null) {
            observer.onFailed("world not loaded: " + spec.worldName());
            return new CompletedHandle();
        }
        TaskState state = new TaskState(observer, Instant.now());
        tasks.put(spec.worldName(), state);

        boolean ok = api.startTask(
            spec.worldName(),
            spec.shape(),
            spec.centerX(), spec.centerZ(),
            spec.radius(), spec.radius(),
            "concentric"
        );
        if (!ok) {
            tasks.remove(spec.worldName());
            observer.onFailed("Chunky.startTask returned false (already running?)");
            return new CompletedHandle();
        }
        return new ActiveHandle(spec.worldName(), state);
    }

    @Override
    public void cancel(String worldName) {
        api.cancelTask(worldName);
        TaskState s = tasks.remove(worldName);
        if (s != null) s.completion.countDown();
    }

    @Override public void pause(String worldName) { api.pauseTask(worldName); }
    @Override public void resume(String worldName) { api.continueTask(worldName); }

    @Override
    public Optional<PregenProgress> progressOf(String worldName) {
        TaskState s = tasks.get(worldName);
        if (s == null) return Optional.empty();
        return Optional.of(s.lastProgress);
    }

    private void onProgressEvent(GenerationProgressEvent event) {
        TaskState s = tasks.get(event.world());
        if (s == null) return;
        PregenProgress prog = toProgress(event, s.startedAt, false);
        s.lastProgress = prog;
        s.observer.onProgress(prog);
        if (event.complete()) {
            // GenerationProgressEvent.complete() can fire before the dedicated
            // GenerationCompleteEvent; treat either as terminal.
            finishTask(event.world(), s, prog);
        }
    }

    private void onCompleteEvent(GenerationCompleteEvent event) {
        TaskState s = tasks.get(event.world());
        if (s == null) return;
        PregenProgress prog = new PregenProgress(
            PregenState.COMPLETED,
            s.lastProgress.chunksDone(),
            s.lastProgress.chunksTotal(),
            Duration.between(s.startedAt, Instant.now()),
            Optional.of(Duration.ZERO)
        );
        finishTask(event.world(), s, prog);
    }

    private void finishTask(String worldName, TaskState state, PregenProgress finalProgress) {
        // Guard against double-firing (progress complete + dedicated complete event).
        if (tasks.remove(worldName) == null) {
            return;
        }
        state.lastProgress = finalProgress;
        state.observer.onComplete(finalProgress);
        state.completion.countDown();
    }

    private PregenProgress toProgress(GenerationProgressEvent event, Instant startedAt, boolean forceComplete) {
        long done = event.chunks();
        // Chunky's API does not expose total chunks directly; derive from
        // percentual progress when meaningful.
        long total = 0L;
        float percent = event.progress();
        if (percent > 0f && done > 0L) {
            total = Math.round(done * 100.0 / percent);
            if (total < done) total = done;
        }
        Duration elapsed = Duration.between(startedAt, Instant.now());
        PregenState state = (forceComplete || event.complete())
            ? PregenState.COMPLETED : PregenState.RUNNING;
        Optional<Duration> eta = Optional.empty();
        long etaSeconds = event.hours() * 3600 + event.minutes() * 60 + event.seconds();
        if (etaSeconds > 0) {
            eta = Optional.of(Duration.ofSeconds(etaSeconds));
        } else if (done > 0 && total > done) {
            long etaMillis = elapsed.toMillis() * (total - done) / done;
            eta = Optional.of(Duration.ofMillis(etaMillis));
        }
        return new PregenProgress(state, done, total, elapsed, eta);
    }

    private static final class TaskState {
        final PregenObserver observer;
        final Instant startedAt;
        final CountDownLatch completion = new CountDownLatch(1);
        volatile PregenProgress lastProgress;

        TaskState(PregenObserver observer, Instant startedAt) {
            this.observer = observer;
            this.startedAt = startedAt;
            this.lastProgress = new PregenProgress(
                PregenState.RUNNING, 0, 0, Duration.ZERO, Optional.empty());
        }
    }

    private static final class CompletedHandle implements PregenHandle {
        @Override public boolean awaitCompletion(Duration timeout) { return true; }
        @Override public void cancel() { /* nothing */ }
    }

    private final class ActiveHandle implements PregenHandle {
        private final String worldName;
        private final TaskState state;
        ActiveHandle(String worldName, TaskState state) {
            this.worldName = worldName;
            this.state = state;
        }
        @Override
        public boolean awaitCompletion(Duration timeout) {
            try {
                return state.completion.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        @Override
        public void cancel() {
            api.cancelTask(worldName);
            state.completion.countDown();
            tasks.remove(worldName);
        }
    }
}
