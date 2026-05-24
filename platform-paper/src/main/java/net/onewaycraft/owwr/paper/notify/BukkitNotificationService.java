package net.onewaycraft.owwr.paper.notify;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.onewaycraft.owwr.core.notify.NotificationService;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @brief Implementação Bukkit/Adventure de NotificationService.
 */
public final class BukkitNotificationService implements NotificationService {

    private final Map<String, BossBar> activeBars = new HashMap<>();

    @Override
    public void broadcast(String worldName, String message, boolean bossbar, boolean actionbar, boolean title) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;
        Audience audience = Audience.audience(world.getPlayers());
        Component text = LegacyComponentSerializer.legacyAmpersand().deserialize(message);

        if (actionbar) {
            audience.sendActionBar(text);
        }
        if (title) {
            audience.showTitle(Title.title(text, Component.empty(),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))));
        }
        if (bossbar) {
            BossBar bar = activeBars.computeIfAbsent(worldName,
                k -> BossBar.bossBar(text, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS));
            bar.name(text);
            audience.showBossBar(bar);
        }
    }

    @Override
    public void clear(String worldName) {
        BossBar bar = activeBars.remove(worldName);
        if (bar == null) return;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;
        Audience.audience(world.getPlayers()).hideBossBar(bar);
    }
}
