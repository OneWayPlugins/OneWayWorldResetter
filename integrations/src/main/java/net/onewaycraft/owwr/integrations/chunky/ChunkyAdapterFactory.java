package net.onewaycraft.owwr.integrations.chunky;

import net.onewaycraft.owwr.core.pregen.PregenService;
import net.onewaycraft.owwr.integrations.SoftDep;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * @brief Factory que tenta construir um ChunkyPregenService.
 *
 * Retorna null se o plugin Chunky nao estiver presente; nesse caso o caller
 * deve usar {@code NoopPregenService} como fallback.
 */
public final class ChunkyAdapterFactory {

    private ChunkyAdapterFactory() {}

    /**
     * @brief Tenta criar um adapter Chunky.
     * @return o adapter, ou null se Chunky ausente ou se a API falhou de carregar.
     */
    public static PregenService tryCreate(Plugin owwrPlugin, Logger logger) {
        if (!SoftDep.isPresent("Chunky")) {
            return null;
        }
        try {
            return new ChunkyPregenService(owwrPlugin, logger);
        } catch (Throwable t) {
            logger.warning("Chunky plugin detected but API failed to bind: " + t.getMessage());
            return null;
        }
    }
}
