package net.onewaycraft.owwr.plugin.commands;

import net.onewaycraft.owwr.api.MessageService;
import net.onewaycraft.owwr.api.OwwrConfig;
import net.onewaycraft.owwr.api.ResourceWorld;
import net.onewaycraft.owwr.core.persistence.HistoryRepository;
import net.onewaycraft.owwr.core.reset.ResetService;
import net.onewaycraft.owwr.core.teleport.PlayerRef;
import net.onewaycraft.owwr.core.teleport.TeleportService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.util.List;
import java.util.Map;

/**
 * @brief Registra a árvore de comandos via Cloud.
 *
 * /owwr (admin): help, reload, reset, history, status, tp
 * /resource [world]: teleporta para resource world.
 */
public final class OwwrCommandModule {

    private final LegacyPaperCommandManager<CommandSender> manager;
    private final ResetService reset;
    private final OwwrConfig config;
    private final TeleportService teleport;
    private final MessageService messages;
    private final HistoryRepository history;
    private final CooldownTracker cooldowns;

    public OwwrCommandModule(Plugin plugin, ResetService reset, OwwrConfig config,
                             TeleportService teleport, MessageService messages,
                             HistoryRepository history, CooldownTracker cooldowns) {
        this.manager = LegacyPaperCommandManager.createNative(plugin, ExecutionCoordinator.simpleCoordinator());
        try { this.manager.registerBrigadier(); } catch (Throwable ignored) {}
        this.reset = reset;
        this.config = config;
        this.teleport = teleport;
        this.messages = messages;
        this.history = history;
        this.cooldowns = cooldowns;
    }

    public void register() {
        registerOwwrAdmin();
        registerResource();
    }

    private void registerOwwrAdmin() {
        BlockingSuggestionProvider.Strings<CommandSender> worldIds = (ctx, in) ->
            config.worlds().stream().map(ResourceWorld::id).toList();

        var root = manager.commandBuilder("owwr").permission("owwr.admin");

        manager.command(root.literal("help").handler(c ->
            c.sender().sendMessage("/owwr help | reload | reset <world> [confirm|dry-run] | history | status | tp [world]")));

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

    private void handleTeleport(Player p, String worldId) {
        if (!p.hasPermission("owwr.bypass.cooldown")) {
            long remaining = cooldowns.remainingSeconds(p.getUniqueId());
            if (remaining > 0) {
                p.sendMessage(messages.resolve("reset.cooldown",
                    Map.of("seconds", String.valueOf(remaining))));
                return;
            }
        }
        if (worldId == null) {
            List<ResourceWorld> active = config.worlds().stream()
                .filter(ResourceWorld::enabled).toList();
            if (active.size() == 1) {
                doTeleport(p, active.get(0));
            } else {
                p.sendMessage("Multiple worlds. Specify one: " +
                    active.stream().map(ResourceWorld::id).toList());
            }
            return;
        }
        config.worlds().stream()
            .filter(w -> w.id().equalsIgnoreCase(worldId) && w.enabled())
            .findFirst()
            .ifPresentOrElse(w -> doTeleport(p, w),
                () -> p.sendMessage(messages.resolve("teleport.no-world",
                    Map.of("world", worldId))));
    }

    private void doTeleport(Player p, ResourceWorld w) {
        teleport.teleportTo(new PlayerRef(p.getUniqueId(), p.getName()), w.worldName());
        cooldowns.markUsed(p.getUniqueId());
        p.sendMessage(messages.resolve("teleport.confirmed", Map.of("world", w.id())));
    }
}
