package net.onewaycraft.owwr.paper.events;

import net.onewaycraft.owwr.api.event.PostResetEvent;
import net.onewaycraft.owwr.api.event.PreResetEvent;
import net.onewaycraft.owwr.api.event.RegionPostResetEvent;
import net.onewaycraft.owwr.api.event.RegionPreResetEvent;
import net.onewaycraft.owwr.api.event.ResetEvent;
import net.onewaycraft.owwr.core.reset.EventBus;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public final class BukkitEventBus implements EventBus {

    @Override
    public boolean fire(ResetEvent evt) {
        if (evt instanceof PreResetEvent pre) {
            BukkitPreResetEvent be = new BukkitPreResetEvent(pre);
            Bukkit.getPluginManager().callEvent(be);
            if (be.isCancelled()) pre.setCancelled(true);
            return pre.isCancelled();
        } else if (evt instanceof PostResetEvent post) {
            Event bukkitEvent = new BukkitPostResetEvent(post);
            Bukkit.getPluginManager().callEvent(bukkitEvent);
            return false;
        } else if (evt instanceof RegionPreResetEvent || evt instanceof RegionPostResetEvent) {
            // Region bridges land in Phase 6
            return false;
        }
        return false;
    }
}
