package net.onewaycraft.owwr.paper.preflight;

import net.onewaycraft.owwr.core.preflight.ServerSnapshot;
import org.bukkit.Bukkit;

import java.util.function.Supplier;

/**
 * @brief Captura snapshot do servidor Paper (Bukkit) para alimentar PreflightGates.
 */
public final class BukkitServerSnapshot implements Supplier<ServerSnapshot> {

    @Override
    public ServerSnapshot get() {
        double tps = Bukkit.getServer().getTPS()[0];
        int players = Bukkit.getOnlinePlayers().size();
        long free = Bukkit.getWorldContainer().getFreeSpace();
        return new ServerSnapshot(tps, players, free);
    }
}
