package net.onewaycraft.owwr.api.event;

/**
 * @brief Evento base de reset. Implementações concretas em api/event.
 *        O bridge para Bukkit Event vive em platform-paper.
 */
public interface ResetEvent {
    String worldId();
}
