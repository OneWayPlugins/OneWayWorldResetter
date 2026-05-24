package net.onewaycraft.owwr.core.preflight;

/**
 * @brief Snapshot mínimo das métricas de servidor para gates puros.
 *        Fornecido pelo platform-paper (BukkitServerSnapshot).
 */
public record ServerSnapshot(double tps, int onlinePlayers, long freeDiskBytes) {}
