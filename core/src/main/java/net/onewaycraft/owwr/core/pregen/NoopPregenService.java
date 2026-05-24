package net.onewaycraft.owwr.core.pregen;

import net.onewaycraft.owwr.api.PregenProgress;
import net.onewaycraft.owwr.api.PregenState;

import java.time.Duration;
import java.util.Optional;

/**
 * @brief Impl que não faz nada — usada quando o plugin Chunky está ausente.
 *
 * start() retorna um handle que conclui imediatamente; demais métodos são no-op.
 */
public final class NoopPregenService implements PregenService {

    @Override
    public PregenHandle start(PregenSpec spec, PregenObserver observer) {
        PregenProgress done = new PregenProgress(
            PregenState.COMPLETED, 0, 0, Duration.ZERO, Optional.empty());
        observer.onComplete(done);
        return new PregenHandle() {
            @Override public boolean awaitCompletion(Duration timeout) { return true; }
            @Override public void cancel() { /* no-op */ }
        };
    }

    @Override public void cancel(String worldName) {}
    @Override public void pause(String worldName) {}
    @Override public void resume(String worldName) {}

    @Override
    public Optional<PregenProgress> progressOf(String worldName) {
        return Optional.empty();
    }
}
