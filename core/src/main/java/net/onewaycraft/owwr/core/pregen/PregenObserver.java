package net.onewaycraft.owwr.core.pregen;

import net.onewaycraft.owwr.api.PregenProgress;

/**
 * @brief Callback invocado pela PregenService durante a execução.
 *
 * Todas as três callbacks são opcionais; default no-op.
 */
public interface PregenObserver {

    /** Disparada periodicamente conforme chunks são gerados. */
    default void onProgress(PregenProgress progress) {}

    /** Disparada quando a tarefa termina com sucesso. */
    default void onComplete(PregenProgress finalProgress) {}

    /** Disparada quando a tarefa termina com erro hard (Chunky lança / mundo corrompido). */
    default void onFailed(String reason) {}

    /** Observer no-op para uso em testes ou quando notifications estão desligadas. */
    static PregenObserver noop() { return new PregenObserver() {}; }
}
