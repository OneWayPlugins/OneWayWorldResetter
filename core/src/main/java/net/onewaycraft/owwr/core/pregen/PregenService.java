package net.onewaycraft.owwr.core.pregen;

import net.onewaycraft.owwr.api.PregenProgress;

import java.time.Duration;
import java.util.Optional;

/**
 * @brief Abstração de pre-gen de mundo (Chunky ou no-op).
 *
 * Implementações: NoopPregenService (sem Chunky) e ChunkyPregenService (integrations).
 * O core nunca depende do Chunky diretamente.
 */
public interface PregenService {

    /**
     * @brief Inicia pre-gen para o mundo descrito em {@code spec}.
     * @return handle que pode ser usado para aguardar completion ou observar progresso.
     */
    PregenHandle start(PregenSpec spec, PregenObserver observer);

    /** Cancela qualquer pre-gen em andamento para o mundo. No-op se não houver. */
    void cancel(String worldName);

    /** Pausa o pre-gen em andamento. No-op se não houver. */
    void pause(String worldName);

    /** Retoma um pre-gen pausado. No-op se não houver. */
    void resume(String worldName);

    /** Snapshot atual do progresso, ou empty se nunca iniciado / desconhecido. */
    Optional<PregenProgress> progressOf(String worldName);

    /**
     * @brief Handle de uma tarefa de pre-gen iniciada.
     */
    interface PregenHandle {
        /**
         * @brief Bloqueia a thread atual até a tarefa completar OU expirar o timeout.
         * @return true se completou (com sucesso ou falha controlada); false se atingiu o timeout.
         */
        boolean awaitCompletion(Duration timeout);

        /** Cancela a tarefa associada. */
        void cancel();
    }
}
