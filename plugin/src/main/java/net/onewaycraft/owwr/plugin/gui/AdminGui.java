package net.onewaycraft.owwr.plugin.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.onewaycraft.owwr.api.OwwrConfig;
import net.onewaycraft.owwr.api.ResourceWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @brief GUI admin para visualizar status dos mundos e disparar resets.
 *
 * Por simplicidade no Phase 8, cada ícone de mundo dispara `/owwr reset <id> confirm`
 * via dispatchCommand, reusando a árvore de comandos Cloud.
 */
public final class AdminGui {

    private final OwwrConfig config;

    public AdminGui(OwwrConfig config) { this.config = config; }

    public void open(Player player) {
        int rows = Math.max(1, Math.min(6, ((config.worlds().size() - 1) / 9) + 1));
        Gui gui = Gui.gui()
            .title(Component.text("OWWR Admin"))
            .rows(rows)
            .create();

        for (ResourceWorld w : config.worlds()) {
            ItemStack icon = new ItemStack(w.enabled() ? Material.GRASS_BLOCK : Material.BARRIER);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(w.id() + (w.enabled() ? "" : " (disabled)")));
                icon.setItemMeta(meta);
            }
            gui.addItem(new GuiItem(icon, e -> {
                gui.close(player);
                Bukkit.dispatchCommand(player, "owwr reset " + w.id() + " confirm");
            }));
        }
        gui.open(player);
    }
}
