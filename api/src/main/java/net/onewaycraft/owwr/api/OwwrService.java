package net.onewaycraft.owwr.api;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * @brief API pública para outros plugins.
 *
 * Acesso: Bukkit.getServicesManager().load(OwwrService.class).
 */
public interface OwwrService {
    List<ResourceWorld> worlds();
    Optional<Instant> nextResetOf(String worldId);
    void requestReset(String worldId, boolean dryRun);
    Optional<ResetState> currentState(String worldId);
    List<ResetRecord> recentHistory(int limit);

    /**
     * @brief Progresso atual do Chunky pre-gen para um mundo.
     *
     * Default implementation returns empty (backward-compatible).
     * Implementações que suportam pre-gen devem sobrescrever.
     *
     * @return progresso ou empty se nunca iniciado ou se Chunky ausente
     */
    default Optional<PregenProgress> pregenProgressOf(String worldId) {
        return Optional.empty();
    }
}
