package net.onewaycraft.owwr.plugin.commands;

import net.onewaycraft.owwr.api.ChunkyConfig;
import net.onewaycraft.owwr.api.MessageService;
import net.onewaycraft.owwr.api.OwwrConfig;
import net.onewaycraft.owwr.api.PregenProgress;
import net.onewaycraft.owwr.api.ResourceWorld;
import net.onewaycraft.owwr.core.persistence.HistoryRepository;
import net.onewaycraft.owwr.core.pregen.PregenObserver;
import net.onewaycraft.owwr.core.pregen.PregenService;
import net.onewaycraft.owwr.core.pregen.PregenSpec;
import net.onewaycraft.owwr.core.reset.ResetService;
import net.onewaycraft.owwr.core.teleport.PlayerRef;
import net.onewaycraft.owwr.core.teleport.TeleportService;
import net.onewaycraft.owwr.paper.teleport.BukkitTeleportService;
import net.onewaycraft.owwr.plugin.gui.AdminGui;
import net.onewaycraft.owwr.plugin.gui.TeleportGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class OwwrCommandModule {

    private final LegacyPaperCommandManager<CommandSender> manager;
    private final ResetService reset;
    private final OwwrConfig config;
    private final BukkitTeleportService teleport;
    private final MessageService messages;
    private final HistoryRepository history;
    private final CooldownTracker cooldowns;
    private final TeleportGui teleportGui;
    private final AdminGui adminGui;
    private final PregenService pregen;

    public OwwrCommandModule(Plugin plugin, ResetService reset, OwwrConfig config,
                             TeleportService teleport, MessageService messages,
                             HistoryRepository history, CooldownTracker cooldowns,
                             PregenService pregen) {
        this.manager = LegacyPaperCommandManager.createNative(plugin, ExecutionCoordinator.simpleCoordinator());
        try { this.manager.registerBrigadier(); } catch (Throwable ignored) {}
        this.reset = reset;
        this.config = config;
        // teleport is always a BukkitTeleportService on Paper; cast for GUI integration
        this.teleport = (BukkitTeleportService) teleport;
        this.messages = messages;
        this.history = history;
        this.cooldowns = cooldowns;
        this.teleportGui = new TeleportGui(this.teleport);
        this.adminGui = new AdminGui(config);
        this.pregen = pregen;
    }

    public void register() {
        registerOwwrAdmin();
        registerResource();
        registerChunky();
    }

    private void registerOwwrAdmin() {
        BlockingSuggestionProvider.Strings<CommandSender> worldIds = (ctx, in) ->
            config.worlds().stream().map(ResourceWorld::id).toList();

        var root = manager.commandBuilder("owwr").permission("owwr.admin");

        manager.command(root.literal("help").handler(c ->
            c.sender().sendMessage("/owwr help | gui | reload | reset <world> [confirm|dry-run] | history | status | tp [world] | chunky <status|start|cancel|pause|resume> <world>")));

        manager.command(root.literal("gui").permission("owwr.command.gui")
            .senderType(Player.class)
            .handler(c -> adminGui.open(c.sender())));

        manager.command(root.literal("reload").permission("owwr.command.reload").handler(c ->
            c.sender().sendMessage(messages.resolve("admin.reload-ok"))));

        manager.command(root.literal("reset")
            .permission("owwr.command.reset")
            .required("world", StringParser.stringParser(), worldIds)
            .optional("mode", StringParser.stringParser())
            .handler(c -> {
                String worldId = c.get("world");
                String mode = c.getOrDefault("mode", "");
                boolean dry = "dry-run".equalsIgnoreCase(mode);
                boolean confirm = "confirm".equalsIgnoreCase(mode);
                if (!dry && !confirm) {
                    c.sender().sendMessage(messages.resolve("admin.reset-confirm",
                        Map.of("world", worldId)));
                    return;
                }
                reset.request(worldId, dry);
                c.sender().sendMessage("Queued reset for " + worldId + (dry ? " (dry-run)" : ""));
            }));

        manager.command(root.literal("history")
            .permission("owwr.command.history")
            .handler(c -> {
                var recent = history.recent(10);
                c.sender().sendMessage("Recent resets: " + recent.size());
                recent.forEach(r -> c.sender().sendMessage(
                    " - " + r.worldId() + " @ " + r.startedAt() + " success=" + r.success()));
            }));

        manager.command(root.literal("status").permission("owwr.command.status")
            .handler(c -> c.sender().sendMessage(
                "Worlds: " + config.worlds().size() + " · Strategies: in-place, double-buffered")));

        manager.command(root.literal("tp")
            .senderType(Player.class)
            .optional("world", StringParser.stringParser(), worldIds)
            .handler(c -> {
                Player p = c.sender();
                String world = c.getOrDefault("world", (String) null);
                handleTeleport(p, world);
            }));
    }

    private void registerResource() {
        BlockingSuggestionProvider.Strings<CommandSender> enabledWorldIds = (ctx, in) ->
            config.worlds().stream()
                .filter(ResourceWorld::enabled)
                .map(ResourceWorld::id).toList();

        manager.command(manager.commandBuilder("resource", "rw", "resourceworld")
            .permission("owwr.tp")
            .senderType(Player.class)
            .optional("world", StringParser.stringParser(), enabledWorldIds)
            .handler(c -> {
                Player p = c.sender();
                String world = c.getOrDefault("world", (String) null);
                handleTeleport(p, world);
            }));
    }

    private void registerChunky() {
        BlockingSuggestionProvider.Strings<CommandSender> chunkyWorldIds = (ctx, in) ->
            config.worlds().stream()
                .filter(w -> w.reset().chunky() != null)
                .map(ResourceWorld::id).toList();

        var chunky = manager.commandBuilder("owwr")
            .literal("chunky")
            .permission("owwr.command.chunky");

        manager.command(chunky.literal("status")
            .required("world", StringParser.stringParser(), chunkyWorldIds)
            .handler(c -> {
                String worldId = c.get("world");
                ResourceWorld rw = resolveWorld(worldId);
                if (rw == null) {
                    c.sender().sendMessage("Unknown world: " + worldId);
                    return;
                }
                Optional<PregenProgress> p = pregen.progressOf(rw.worldName());
                if (p.isEmpty()) {
                    c.sender().sendMessage("Pre-gen idle for " + worldId);
                } else {
                    PregenProgress pp = p.get();
                    c.sender().sendMessage(String.format(
                        "Pre-gen %s for %s: %.1f%% (%d/%d), elapsed=%s",
                        pp.state(), worldId, pp.percent(),
                        pp.chunksDone(), pp.chunksTotal(), pp.elapsed()));
                }
            }));

        manager.command(chunky.literal("start")
            .required("world", StringParser.stringParser(), chunkyWorldIds)
            .handler(c -> {
                String worldId = c.get("world");
                ResourceWorld rw = resolveWorld(worldId);
                if (rw == null) {
                    c.sender().sendMessage("Unknown world: " + worldId);
                    return;
                }
                ChunkyConfig cfg = rw.reset().chunky();
                if (cfg == null) {
                    c.sender().sendMessage("Chunky pre-gen not enabled in config for " + worldId);
                    return;
                }
                PregenSpec spec = new PregenSpec(
                    rw.worldName(), cfg.shape(),
                    cfg.centerX(), cfg.centerZ(), cfg.radius(),
                    Duration.ofMinutes(cfg.maxDurationMinutes()),
                    cfg.notifications());
                pregen.start(spec, PregenObserver.noop());
                c.sender().sendMessage("Pre-gen started for " + worldId);
            }));

        manager.command(chunky.literal("cancel")
            .required("world", StringParser.stringParser(), chunkyWorldIds)
            .handler(c -> {
                String worldId = c.get("world");
                ResourceWorld rw = resolveWorld(worldId);
                if (rw == null) {
                    c.sender().sendMessage("Unknown world: " + worldId);
                    return;
                }
                pregen.cancel(rw.worldName());
                c.sender().sendMessage("Pre-gen cancelled for " + worldId);
            }));

        manager.command(chunky.literal("pause")
            .required("world", StringParser.stringParser(), chunkyWorldIds)
            .handler(c -> {
                String worldId = c.get("world");
                ResourceWorld rw = resolveWorld(worldId);
                if (rw == null) {
                    c.sender().sendMessage("Unknown world: " + worldId);
                    return;
                }
                pregen.pause(rw.worldName());
                c.sender().sendMessage("Pre-gen paused for " + worldId);
            }));

        manager.command(chunky.literal("resume")
            .required("world", StringParser.stringParser(), chunkyWorldIds)
            .handler(c -> {
                String worldId = c.get("world");
                ResourceWorld rw = resolveWorld(worldId);
                if (rw == null) {
                    c.sender().sendMessage("Unknown world: " + worldId);
                    return;
                }
                pregen.resume(rw.worldName());
                c.sender().sendMessage("Pre-gen resumed for " + worldId);
            }));
    }

    private ResourceWorld resolveWorld(String worldId) {
        return config.worlds().stream()
            .filter(w -> w.id().equalsIgnoreCase(worldId))
            .findFirst().orElse(null);
    }

    private void handleTeleport(Player p, String worldId) {
        if (!p.hasPermission("owwr.bypass.cooldown")) {
            long remaining = cooldowns.remainingSeconds(p.getUniqueId());
            if (remaining > 0) {
                p.sendMessage(messages.resolve("reset.cooldown",
                    Map.of("seconds", String.valueOf(remaining))));
                return;
            }
        }
        List<ResourceWorld> active = config.worlds().stream()
            .filter(ResourceWorld::enabled).toList();

        if (worldId == null) {
            if (active.size() == 1) {
                doTeleport(p, active.get(0));
            } else if (active.size() > 1) {
                teleportGui.open(p, active);
            } else {
                p.sendMessage("No resource worlds available.");
            }
            return;
        }
        active.stream()
            .filter(w -> w.id().equalsIgnoreCase(worldId))
            .findFirst()
            .ifPresentOrElse(w -> doTeleport(p, w),
                () -> p.sendMessage(messages.resolve("teleport.no-world",
                    Map.of("world", worldId))));
    }

    private void doTeleport(Player p, ResourceWorld w) {
        teleport.teleportTo(new PlayerRef(p.getUniqueId(), p.getName()), w);
        cooldowns.markUsed(p.getUniqueId());
        p.sendMessage(messages.resolve("teleport.confirmed", Map.of("world", w.id())));
    }
}
