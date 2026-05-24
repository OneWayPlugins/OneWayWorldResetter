package net.onewaycraft.owwr.paper.world;

import net.onewaycraft.owwr.api.Environment;
import net.onewaycraft.owwr.core.world.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * @brief Fallback adapter (sem Multiverse) usando apenas Bukkit API.
 */
public final class BukkitNativeAdapter implements WorldLifecycleService {

    private final Logger logger;

    public BukkitNativeAdapter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean worldExists(String name) {
        return Bukkit.getWorld(name) != null
            || Path.of(Bukkit.getWorldContainer().toString(), name).toFile().isDirectory();
    }

    @Override
    public WorldOpResult createWorld(WorldSpec spec) {
        if (Bukkit.getWorld(spec.name()) != null) {
            return WorldOpResult.fail("world already loaded: " + spec.name());
        }
        WorldCreator wc = new WorldCreator(spec.name())
            .seed(spec.seed())
            .environment(map(spec.environment()));
        World w = wc.createWorld();
        return w != null ? WorldOpResult.ok() : WorldOpResult.fail("createWorld returned null");
    }

    @Override
    public WorldOpResult unloadWorld(String name) {
        World w = Bukkit.getWorld(name);
        if (w == null) return WorldOpResult.ok(); // já descarregado
        if (!w.getPlayers().isEmpty()) {
            return WorldOpResult.fail("players still in world: " + w.getPlayers().size());
        }
        boolean ok = Bukkit.unloadWorld(w, false);
        return ok ? WorldOpResult.ok() : WorldOpResult.fail("Bukkit.unloadWorld returned false");
    }

    @Override
    public WorldOpResult deleteWorld(String name) {
        Path container = Bukkit.getWorldContainer().toPath();
        Path target = container.resolve(name);
        if (!isSafeToDelete(target)) {
            return WorldOpResult.fail("refused unsafe delete: " + target);
        }
        try {
            FileTree.deleteRecursively(target);
            return WorldOpResult.ok();
        } catch (IOException e) {
            logger.warning("delete failed: " + e.getMessage());
            return WorldOpResult.fail("io error: " + e.getMessage());
        }
    }

    @Override
    public boolean isSafeToDelete(Path target) {
        return PathSafety.isUnder(target, Bukkit.getWorldContainer().toPath());
    }

    private static World.Environment map(Environment e) {
        return switch (e) {
            case NORMAL -> World.Environment.NORMAL;
            case NETHER -> World.Environment.NETHER;
            case END -> World.Environment.THE_END;
        };
    }
}
