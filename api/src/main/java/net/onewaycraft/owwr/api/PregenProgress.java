package net.onewaycraft.owwr.api;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * @brief Snapshot do progresso de pre-gen de um mundo.
 *
 * @param state    estado atual
 * @param chunksDone chunks já gerados
 * @param chunksTotal total estimado de chunks (zero se desconhecido)
 * @param elapsed  tempo decorrido desde o start
 * @param eta      tempo estimado restante; vazio se não estimável
 */
public record PregenProgress(
    PregenState state,
    long chunksDone,
    long chunksTotal,
    Duration elapsed,
    Optional<Duration> eta
) {
    public PregenProgress {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(elapsed, "elapsed");
        Objects.requireNonNull(eta, "eta");
        if (chunksDone < 0) throw new IllegalArgumentException("chunksDone must be >= 0");
        if (chunksTotal < 0) throw new IllegalArgumentException("chunksTotal must be >= 0");
    }

    /** Percentual concluído (0.0 a 100.0). Zero se chunksTotal == 0. */
    public double percent() {
        return chunksTotal == 0 ? 0.0 : 100.0 * chunksDone / chunksTotal;
    }
}
