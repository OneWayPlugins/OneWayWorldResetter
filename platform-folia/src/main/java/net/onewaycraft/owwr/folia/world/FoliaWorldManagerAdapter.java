package net.onewaycraft.owwr.folia.world;

import net.onewaycraft.owwr.api.Environment;
import net.onewaycraft.owwr.core.world.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @brief Folia-aware adapter. Operações de mundo são serializadas no GlobalRegionScheduler.
 *
 * Limitação documentada: a API atual do Folia ainda em flux para createWorld/unloadWorld;
 * em servidores Folia recomendamos um world-manager dedicado se disponível.
 */
public final class FoliaWorldManagerAdapter implements WorldLifecycleService {

    private final Plugin plugin;
    private final Logger logger;

    public FoliaWorldManagerAdapter(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @Override
    public boolean worldExists(String name) {
        return Bukkit.getWorld(name) != null
            || Path.of(Bukkit.getWorldContainer().toString(), name).toFile().isDirectory();
    }

    @Override
    public WorldOpResult createWorld(WorldSpec spec) {
        return runGlobalBlocking(() -> {
            if (Bukkit.getWorld(spec.name()) != null) {
                return WorldOpResult.fail("world already loaded: " + spec.name());
            }
            WorldCreator wc = new WorldCreator(spec.name())
                .seed(spec.seed())
                .environment(map(spec.environment()));
            World w = wc.createWorld();
            return w != null ? WorldOpResult.ok() : WorldOpResult.fail("createWorld returned null");
        });
    }

    @Override
    public WorldOpResult unloadWorld(String name) {
        return runGlobalBlocking(() -> {
            World w = Bukkit.getWorld(name);
            if (w == null) return WorldOpResult.ok();
            if (!w.getPlayers().isEmpty()) {
                return WorldOpResult.fail("players still in world: " + w.getPlayers().size());
            }
            boolean ok = Bukkit.unloadWorld(w, false);
            return ok ? WorldOpResult.ok() : WorldOpResult.fail("unloadWorld returned false");
        });
    }

    @Override
    public WorldOpResult deleteWorld(String name) {
        Path target = Bukkit.getWorldContainer().toPath().resolve(name);
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

    private WorldOpResult runGlobalBlocking(java.util.function.Supplier<WorldOpResult> task) {
        CompletableFuture<WorldOpResult> fut = new CompletableFuture<>();
        Bukkit.getGlobalRegionScheduler().run(plugin, t -> {
            try { fut.complete(task.get()); }
            catch (Throwable th) { fut.completeExceptionally(th); }
        });
        try {
            return fut.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            return WorldOpResult.fail("world op timed out or failed: " + e.getMessage());
        }
    }

    private static World.Environment map(Environment e) {
        return switch (e) {
            case NORMAL -> World.Environment.NORMAL;
            case NETHER -> World.Environment.NETHER;
            case END -> World.Environment.THE_END;
        };
    }
}
