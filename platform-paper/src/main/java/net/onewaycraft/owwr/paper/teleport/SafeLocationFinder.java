package net.onewaycraft.owwr.paper.teleport;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @brief Busca local seguro por amostragem aleatória dentro de um raio.
 *
 * Critério: bloco sob os pés sólido não-fluido nem fatal, dois blocos acima vazios,
 * Y dentro dos limites do mundo.
 */
public final class SafeLocationFinder {

    private static final int MAX_ATTEMPTS = 64;

    public Location findSafe(World world, int radius) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            int x = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            int z = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            Location candidate = topAt(world, x, z);
            if (candidate != null && isSafe(candidate)) return candidate;
        }
        return world.getSpawnLocation();
    }

    private Location topAt(World w, int x, int z) {
        int y = (w.getEnvironment() == World.Environment.NETHER)
            ? scanNether(w, x, z) : w.getHighestBlockYAt(x, z);
        if (y <= w.getMinHeight() || y >= w.getMaxHeight()) return null;
        return new Location(w, x + 0.5, y + 1.0, z + 0.5);
    }

    private int scanNether(World w, int x, int z) {
        for (int y = 100; y > 30; y--) {
            Block b = w.getBlockAt(x, y, z);
            if (b.getType().isSolid()
                && w.getBlockAt(x, y + 1, z).isEmpty()
                && w.getBlockAt(x, y + 2, z).isEmpty()) return y;
        }
        return -1;
    }

    private boolean isSafe(Location loc) {
        Block under = loc.clone().subtract(0, 1, 0).getBlock();
        if (!under.getType().isSolid()) return false;
        if (under.isLiquid()) return false;
        Material m = under.getType();
        if (m == Material.LAVA || m == Material.MAGMA_BLOCK || m == Material.CACTUS) return false;
        return loc.getBlock().isEmpty() && loc.clone().add(0, 1, 0).getBlock().isEmpty();
    }
}
