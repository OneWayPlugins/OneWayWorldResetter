package net.onewaycraft.owwr.plugin;

import net.onewaycraft.owwr.core.platform.Platform;
import net.onewaycraft.owwr.core.schedule.Scheduler;
import net.onewaycraft.owwr.folia.schedule.FoliaScheduler;
import net.onewaycraft.owwr.paper.schedule.PaperScheduler;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @brief Bootstrap do OneWayWorldResetter — composition root único.
 *
 * Detecta a plataforma (Folia presente?) e monta o grafo de objetos por construtor.
 * Não cria nenhuma lógica de domínio aqui: tudo vive em core/api.
 */
public final class OneWayWorldResetterPlugin extends JavaPlugin {

    private Scheduler scheduler;

    @Override
    public void onEnable() {
        Platform platform = PlatformDetector.detect();
        getLogger().info("Platform detected: " + platform);
        scheduler = (platform == Platform.FOLIA)
            ? new FoliaScheduler(this)
            : new PaperScheduler(this);
        getLogger().info("OneWayWorldResetter " + getPluginMeta().getVersion() + " started.");
    }

    @Override
    public void onDisable() {
        if (scheduler != null) scheduler.shutdown();
        getLogger().info("OneWayWorldResetter shutting down.");
    }
}
