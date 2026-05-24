package net.onewaycraft.owwr.plugin;

import net.onewaycraft.owwr.api.OwwrConfig;
import net.onewaycraft.owwr.api.ResourceWorld;
import net.onewaycraft.owwr.core.config.YamlConfigLoader;
import net.onewaycraft.owwr.core.platform.Platform;
import net.onewaycraft.owwr.core.schedule.Scheduler;
import net.onewaycraft.owwr.core.world.WorldLifecycleService;
import net.onewaycraft.owwr.core.world.WorldSpec;
import net.onewaycraft.owwr.folia.schedule.FoliaScheduler;
import net.onewaycraft.owwr.paper.schedule.PaperScheduler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class OneWayWorldResetterPlugin extends JavaPlugin {

    private Scheduler scheduler;
    private WorldLifecycleService worldService;
    private OwwrConfig config;

    @Override
    public void onEnable() {
        Platform platform = PlatformDetector.detect();
        getLogger().info("Platform detected: " + platform);

        saveDefaultConfigIfMissing();
        try (InputStream in = Files.newInputStream(getDataFolder().toPath().resolve("config.yml"))) {
            config = new YamlConfigLoader().load(in);
        } catch (IOException e) {
            getLogger().severe("Failed to load config.yml: " + e.getMessage());
            getPluginLoader().disablePlugin(this);
            return;
        }

        scheduler = (platform == Platform.FOLIA)
            ? new FoliaScheduler(this) : new PaperScheduler(this);
        worldService = WorldServiceFactory.build(this, platform, getLogger());

        autoCreateConfiguredWorlds();
        getLogger().info("OneWayWorldResetter " + getPluginMeta().getVersion() + " started.");
    }

    @Override
    public void onDisable() {
        if (scheduler != null) scheduler.shutdown();
        getLogger().info("OneWayWorldResetter shutting down.");
    }

    private void saveDefaultConfigIfMissing() {
        if (!getDataFolder().toPath().resolve("config.yml").toFile().exists()) {
            saveResource("config.yml", false);
        }
        if (!getDataFolder().toPath().resolve("messages.yml").toFile().exists()) {
            saveResource("messages.yml", false);
        }
    }

    private void autoCreateConfiguredWorlds() {
        long seed = System.currentTimeMillis();
        for (ResourceWorld rw : config.worlds()) {
            if (!rw.enabled() || !rw.autoCreate()) continue;
            if (worldService.worldExists(rw.worldName())) continue;
            worldService.createWorld(new WorldSpec(rw.worldName(), rw.environment(), seed++));
        }
    }
}
