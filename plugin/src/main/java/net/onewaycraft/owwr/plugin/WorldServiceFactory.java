package net.onewaycraft.owwr.plugin;

import net.onewaycraft.owwr.core.platform.Platform;
import net.onewaycraft.owwr.core.world.WorldLifecycleService;
import net.onewaycraft.owwr.folia.world.FoliaWorldManagerAdapter;
import net.onewaycraft.owwr.integrations.multiverse.MultiverseAdapter;
import net.onewaycraft.owwr.paper.world.BukkitNativeAdapter;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * @brief Seleciona o WorldLifecycleService apropriado:
 *
 * - Folia presente → FoliaWorldManagerAdapter
 * - Multiverse presente → MultiverseAdapter (com BukkitNativeAdapter como fallback interno)
 * - caso contrário → BukkitNativeAdapter
 */
public final class WorldServiceFactory {

    private WorldServiceFactory() {}

    public static WorldLifecycleService build(Plugin plugin, Platform platform, Logger logger) {
        if (platform == Platform.FOLIA) {
            return new FoliaWorldManagerAdapter(plugin, logger);
        }
        WorldLifecycleService bukkitNative = new BukkitNativeAdapter(logger);
        WorldLifecycleService mv = MultiverseAdapter.tryCreate(logger, bukkitNative);
        return mv != null ? mv : bukkitNative;
    }
}
