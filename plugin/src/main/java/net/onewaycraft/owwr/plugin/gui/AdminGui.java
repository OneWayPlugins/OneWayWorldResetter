package net.onewaycraft.owwr.plugin.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.onewaycraft.owwr.api.OwwrConfig;
import net.onewaycraft.owwr.api.ResourceWorld;
import net.onewaycraft.owwr.core.pregen.PregenService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * @brief GUI admin para visualizar status dos mundos e disparar resets.
 *
 * Cada ícone de mundo dispara `/owwr reset &lt;id&gt; confirm` via dispatchCommand.
 * Slot final: "Chunky Control" abre {@link ChunkyGui}.
 */
public final class AdminGui {

    private final OwwrConfig config;
    private final PregenService pregen;

    public AdminGui(OwwrConfig config, PregenService pregen) {
        this.config = config;
        this.pregen = pregen;
    }

    public void open(Player player) {
        int rows = Math.max(2, Math.min(6, ((config.worlds().size() - 1) / 9) + 2));
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

        // Chunky entry item — last slot of last row
        ItemStack chunkyItem = new ItemStack(Material.COMPASS);
        ItemMeta chunkyMeta = chunkyItem.getItemMeta();
        if (chunkyMeta != null) {
            chunkyMeta.displayName(Component.text("Chunky Control"));
            chunkyMeta.lore(List.of(
                Component.text("Open pre-gen control panel"),
                Component.text("for worlds with chunky.enabled=true")
            ));
            chunkyItem.setItemMeta(chunkyMeta);
        }
        gui.setItem(rows, 9, new GuiItem(chunkyItem, e -> {
            gui.close(player);
            new ChunkyGui(config, pregen).open(player);
        }));

        gui.open(player);
    }
}
