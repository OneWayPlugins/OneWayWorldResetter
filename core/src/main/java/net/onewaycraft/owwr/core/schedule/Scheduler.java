package net.onewaycraft.owwr.core.schedule;

import java.time.Duration;

/**
 * @brief Abstração de scheduling Bukkit vs Folia.
 *
 * Implementações: PaperScheduler (Bukkit Scheduler global) e FoliaScheduler
 * (Global/Region/AsyncScheduler do Folia). O core nunca toca em Bukkit diretamente.
 */
public interface Scheduler {

    /** Roda imediatamente no thread global do servidor (Paper main / Folia global). */
    Cancellable runGlobal(Runnable task);

    /** Roda em thread async. */
    Cancellable runAsync(Runnable task);

    /** Roda após {@code delay} no thread global. */
    Cancellable runDelayedGlobal(Duration delay, Runnable task);

    /** Loop no thread global. */
    Cancellable runRepeatingGlobal(Duration initialDelay, Duration period, Runnable task);

    /**
     * @brief Roda na região correta (em Folia) ou no main thread (em Paper).
     *        Necessário para mexer em entidades/blocos numa região específica.
     */
    Cancellable runAtRegion(RegionRef region, Runnable task);

    /**
     * @brief Cancela tudo agendado por este scheduler. Chamar em onDisable.
     */
    void shutdown();

    interface Cancellable {
        void cancel();
        boolean isCancelled();
    }
}
