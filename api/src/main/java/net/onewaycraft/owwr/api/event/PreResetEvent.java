package net.onewaycraft.owwr.api.event;

/**
 * @brief Disparado antes de iniciar um reset; setCancelled(true) aborta.
 */
public final class PreResetEvent implements ResetEvent {
    private final String worldId;
    private boolean cancelled;

    public PreResetEvent(String worldId) {
        this.worldId = worldId;
    }

    @Override public String worldId() { return worldId; }

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}
