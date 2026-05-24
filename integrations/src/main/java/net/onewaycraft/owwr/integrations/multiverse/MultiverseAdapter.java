package net.onewaycraft.owwr.integrations.multiverse;

import net.onewaycraft.owwr.api.Environment;
import net.onewaycraft.owwr.core.world.*;
import net.onewaycraft.owwr.integrations.SoftDep;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * @brief Delegate para Multiverse-Core via reflection. Apenas instanciado quando
 *        Multiverse-Core estiver presente.
 */
public final class MultiverseAdapter implements WorldLifecycleService {

    private final Logger logger;
    private final Object worldManager;
    private final WorldLifecycleService fallback;

    private MultiverseAdapter(Logger logger, Object wm, WorldLifecycleService fallback) {
        this.logger = logger;
        this.worldManager = wm;
        this.fallback = fallback;
    }

    /**
     * @brief Retorna um adapter Multiverse, ou null se o plugin não estiver presente.
     */
    public static WorldLifecycleService tryCreate(Logger logger, WorldLifecycleService fallback) {
        if (!SoftDep.isPresent("Multiverse-Core")) return null;
        try {
            Plugin mv = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
            Method getMvWorldManager = mv.getClass().getMethod("getMVWorldManager");
            Object wm = getMvWorldManager.invoke(mv);
            return new MultiverseAdapter(logger, wm, fallback);
        } catch (ReflectiveOperationException e) {
            logger.warning("Multiverse detected but reflection failed: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean worldExists(String name) {
        try {
            Method m = worldManager.getClass().getMethod("isMVWorld", String.class);
            return (boolean) m.invoke(worldManager, name);
        } catch (ReflectiveOperationException e) {
            return fallback.worldExists(name);
        }
    }

    @Override
    public WorldOpResult createWorld(WorldSpec spec) {
        try {
            Method m = worldManager.getClass().getMethod(
                "addWorld", String.class, org.bukkit.World.Environment.class,
                String.class, org.bukkit.WorldType.class, Boolean.class, String.class);
            boolean ok = (boolean) m.invoke(
                worldManager, spec.name(),
                mapEnv(spec.environment()),
                Long.toString(spec.seed()),
                org.bukkit.WorldType.NORMAL,
                Boolean.TRUE, null);
            return ok ? WorldOpResult.ok() : WorldOpResult.fail("MV addWorld returned false");
        } catch (ReflectiveOperationException e) {
            logger.warning("MV createWorld reflection failed, falling back: " + e.getMessage());
            return fallback.createWorld(spec);
        }
    }

    @Override
    public WorldOpResult unloadWorld(String name) {
        try {
            Method m = worldManager.getClass().getMethod("unloadWorld", String.class);
            boolean ok = (boolean) m.invoke(worldManager, name);
            return ok ? WorldOpResult.ok() : WorldOpResult.fail("MV unloadWorld false");
        } catch (ReflectiveOperationException e) {
            return fallback.unloadWorld(name);
        }
    }

    @Override
    public WorldOpResult deleteWorld(String name) {
        try {
            Method m = worldManager.getClass().getMethod("deleteWorld", String.class);
            boolean ok = (boolean) m.invoke(worldManager, name);
            return ok ? WorldOpResult.ok() : WorldOpResult.fail("MV deleteWorld false");
        } catch (ReflectiveOperationException e) {
            return fallback.deleteWorld(name);
        }
    }

    @Override
    public boolean isSafeToDelete(Path target) {
        return fallback.isSafeToDelete(target);
    }

    private static org.bukkit.World.Environment mapEnv(Environment e) {
        return switch (e) {
            case NORMAL -> org.bukkit.World.Environment.NORMAL;
            case NETHER -> org.bukkit.World.Environment.NETHER;
            case END -> org.bukkit.World.Environment.THE_END;
        };
    }
}
