package net.onewaycraft.owwr.plugin;

import net.onewaycraft.owwr.api.OwwrConfig;
import net.onewaycraft.owwr.api.OwwrService;
import net.onewaycraft.owwr.api.ResourceWorld;
import net.onewaycraft.owwr.core.config.YamlConfigLoader;
import net.onewaycraft.owwr.core.persistence.HistoryRepository;
import net.onewaycraft.owwr.core.persistence.StateRepository;
import net.onewaycraft.owwr.core.platform.Platform;
import net.onewaycraft.owwr.core.preflight.DiskSpaceGate;
import net.onewaycraft.owwr.core.preflight.OnlinePlayersGate;
import net.onewaycraft.owwr.core.preflight.PreflightGate;
import net.onewaycraft.owwr.core.preflight.TpsGate;
import net.onewaycraft.owwr.core.reset.DoubleBufferedResetStrategy;
import net.onewaycraft.owwr.core.reset.InPlaceResetStrategy;
import net.onewaycraft.owwr.core.reset.ResetQueue;
import net.onewaycraft.owwr.core.reset.ResetService;
import net.onewaycraft.owwr.core.reset.ResetStrategy;
import net.onewaycraft.owwr.core.reset.SeedPicker;
import net.onewaycraft.owwr.core.schedule.Scheduler;
import net.onewaycraft.owwr.core.teleport.TeleportService;
import net.onewaycraft.owwr.core.world.WorldLifecycleService;
import net.onewaycraft.owwr.core.world.WorldRenamer;
import net.onewaycraft.owwr.core.world.WorldSpec;
import net.onewaycraft.owwr.folia.schedule.FoliaScheduler;
import net.onewaycraft.owwr.paper.events.BukkitEventBus;
import net.onewaycraft.owwr.paper.preflight.BukkitServerSnapshot;
import net.onewaycraft.owwr.paper.schedule.PaperScheduler;
import net.onewaycraft.owwr.paper.teleport.BukkitTeleportService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class OneWayWorldResetterPlugin extends JavaPlugin {

    private Scheduler scheduler;
    private WorldLifecycleService worldService;
    private OwwrConfig config;
    private ResetService resetService;
    private net.onewaycraft.owwr.core.region.RegionResetManager regionReset;
    private net.onewaycraft.owwr.core.schedule.ResetSchedulerLoop schedulerLoop;

    @Override
    public void onEnable() {
        Platform platform = PlatformDetector.detect();
        getLogger().info("Platform detected: " + platform);

        saveDefaultConfigIfMissing();
        try (InputStream in = Files.newInputStream(getDataFolder().toPath().resolve("config.yml"))) {
            config = new YamlConfigLoader().load(in);
        } catch (IOException e) {
            getLogger().severe("Failed to load config.yml: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        scheduler = (platform == Platform.FOLIA)
            ? new FoliaScheduler(this) : new PaperScheduler(this);
        worldService = WorldServiceFactory.build(this, platform, getLogger());

        TeleportService teleport = new BukkitTeleportService();
        SeedPicker seedPicker = new SeedPicker();
        Map<String, ResetStrategy> strategies = Map.of(
            "in-place", new InPlaceResetStrategy(worldService, teleport, seedPicker),
            "double-buffered", new DoubleBufferedResetStrategy(
                worldService, teleport, seedPicker,
                new WorldRenamer(), getServer().getWorldContainer().toPath())
        );

        Map<String, ResourceWorld> worldsById = config.worlds().stream()
            .collect(Collectors.toMap(ResourceWorld::id, w -> w));
        List<PreflightGate> gates = List.of(new TpsGate(), new OnlinePlayersGate(), new DiskSpaceGate());

        StateRepository state = new StateRepository(getDataFolder().toPath());
        HistoryRepository history = new HistoryRepository(getDataFolder().toPath(), 100);
        ResetQueue queue = new ResetQueue(config.settings().maxConcurrentResets());

        resetService = new ResetService(
            queue, worldsById, strategies, gates,
            new BukkitServerSnapshot(), state, history,
            new BukkitEventBus(), scheduler, getLogger());

        regionReset = new net.onewaycraft.owwr.core.region.RegionResetManager(
            getServer().getWorldContainer().toPath(),
            scheduler,
            new BukkitEventBus(),
            getLogger());

        // Load messages and register Cloud command tree.
        net.onewaycraft.owwr.api.MessageService messages;
        try (InputStream m = Files.newInputStream(getDataFolder().toPath().resolve("messages.yml"))) {
            messages = net.onewaycraft.owwr.core.messages.YamlMessageService.fromYaml(m);
        } catch (IOException e) {
            getLogger().severe("Failed to load messages.yml: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        java.time.Duration cooldownDuration = java.time.Duration.ofSeconds(5);
        new net.onewaycraft.owwr.plugin.commands.OwwrCommandModule(
            this, resetService, config, teleport, messages, history,
            new net.onewaycraft.owwr.plugin.commands.CooldownTracker(cooldownDuration)
        ).register();

        autoCreateConfiguredWorlds();
        resetService.resumePending();

        schedulerLoop = new net.onewaycraft.owwr.core.schedule.ResetSchedulerLoop(
            scheduler, resetService, worldsById, java.time.ZoneId.systemDefault(), getLogger());
        schedulerLoop.start();

        // Register public API via ServicesManager.
        OwwrServiceImpl api = new OwwrServiceImpl(
            config, resetService, state, history, java.time.ZoneId.systemDefault());
        getServer().getServicesManager().register(
            OwwrService.class, api, this, ServicePriority.Normal);

        try {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new net.onewaycraft.owwr.integrations.papi.OwwrExpansion(
                    config, history, getPluginMeta().getVersion().toString()
                ).register();
                getLogger().info("PlaceholderAPI expansion registered.");
            }
        } catch (Throwable t) {
            getLogger().warning("Failed to register PAPI expansion: " + t.getMessage());
        }

        getLogger().info("OneWayWorldResetter " + getPluginMeta().getVersion() + " started.");
    }

    @Override
    public void onDisable() {
        if (schedulerLoop != null) schedulerLoop.stop();
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
