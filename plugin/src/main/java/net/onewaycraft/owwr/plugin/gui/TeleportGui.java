package net.onewaycraft.owwr.plugin.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.onewaycraft.owwr.api.ResourceWorld;
import net.onewaycraft.owwr.core.teleport.PlayerRef;
import net.onewaycraft.owwr.paper.teleport.BukkitTeleportService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * @brief GUI Triumph para escolher entre N resource worlds.
 */
public final class TeleportGui {

    private final BukkitTeleportService teleport;

    public TeleportGui(BukkitTeleportService teleport) { this.teleport = teleport; }

    public void open(Player player, List<ResourceWorld> worlds) {
        int rows = Math.max(1, Math.min(6, ((worlds.size() - 1) / 9) + 1));
        Gui gui = Gui.gui()
            .title(Component.text("Resource Worlds"))
            .rows(rows)
            .create();

        for (ResourceWorld w : worlds) {
            ItemStack icon = new ItemStack(iconFor(w));
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(w.id()));
                icon.setItemMeta(meta);
            }
            gui.addItem(new GuiItem(icon, e -> {
                teleport.teleportTo(new PlayerRef(player.getUniqueId(), player.getName()), w);
                gui.close(player);
            }));
        }
        gui.open(player);
    }

    private Material iconFor(ResourceWorld w) {
        return switch (w.environment()) {
            case NORMAL -> Material.GRASS_BLOCK;
            case NETHER -> Material.NETHERRACK;
            case END -> Material.END_STONE;
        };
    }
}
