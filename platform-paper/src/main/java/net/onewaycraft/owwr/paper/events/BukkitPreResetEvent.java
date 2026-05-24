package net.onewaycraft.owwr.paper.events;

import net.onewaycraft.owwr.api.event.PreResetEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @brief Bridge Bukkit Event para PreResetEvent (cancelável).
 */
public final class BukkitPreResetEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final PreResetEvent inner;
    private boolean cancelled;

    public BukkitPreResetEvent(PreResetEvent inner) { this.inner = inner; }
    public PreResetEvent inner() { return inner; }
    public String worldId() { return inner.worldId(); }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean c) { cancelled = c; }
    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
