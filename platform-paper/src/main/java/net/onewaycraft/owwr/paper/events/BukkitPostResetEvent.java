package net.onewaycraft.owwr.paper.events;

import net.onewaycraft.owwr.api.event.PostResetEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class BukkitPostResetEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final PostResetEvent inner;
    public BukkitPostResetEvent(PostResetEvent inner) { this.inner = inner; }
    public PostResetEvent inner() { return inner; }
    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
