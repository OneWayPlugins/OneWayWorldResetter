package net.onewaycraft.owwr.paper.world;

import net.onewaycraft.owwr.core.world.WorldRuntime;
import org.bukkit.Bukkit;
import org.bukkit.World;

public final class BukkitWorldRuntime implements WorldRuntime {
    @Override
    public void setAutosave(String worldName, boolean enabled) {
        World w = Bukkit.getWorld(worldName);
        if (w != null) w.setAutoSave(enabled);
    }
}
