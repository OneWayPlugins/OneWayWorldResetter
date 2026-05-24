package net.onewaycraft.owwr.folia.schedule;

import net.onewaycraft.owwr.core.schedule.RegionRef;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * @brief RegionRef carregando o World resolvido para uso pelo RegionScheduler.
 */
public record FoliaRegionRef(String worldName, int chunkX, int chunkZ) implements RegionRef {
    public World resolveWorld() {
        World w = Bukkit.getWorld(worldName);
        if (w == null) throw new IllegalStateException("World not loaded: " + worldName);
        return w;
    }
}
