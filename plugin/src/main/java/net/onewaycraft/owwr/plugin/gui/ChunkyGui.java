package net.onewaycraft.owwr.plugin.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.onewaycraft.owwr.api.OwwrConfig;
import net.onewaycraft.owwr.api.PregenProgress;
import net.onewaycraft.owwr.api.PregenState;
import net.onewaycraft.owwr.api.ResourceWorld;
import net.onewaycraft.owwr.core.pregen.PregenService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @brief GUI Triumph para controlar pre-gen Chunky por mundo.
 *
 * Um item por mundo (cujo reset config tem chunky habilitado); lore mostra estado atual
 * e clique despacha /owwr chunky &lt;action&gt; &lt;world&gt; via Bukkit.dispatchCommand.
 *
 * Click semantics:
 *  - left-click: start
 *  - right-click: cancel
 *  - shift left-click: pause
 *  - shift right-click: resume
 *  - middle-click: refresh (re-open this GUI)
 */
public final class ChunkyGui {

    private final OwwrConfig config;
    private final PregenService pregen;

    public ChunkyGui(OwwrConfig config, PregenService pregen) {
        this.config = config;
        this.pregen = pregen;
    }

    public void open(Player player) {
        List<ResourceWorld> chunkyWorlds = config.worlds().stream()
            .filter(w -> w.reset().chunky() != null)
            .toList();

        int rows = Math.max(1, Math.min(6, ((chunkyWorlds.size() - 1) / 9) + 1));
        Gui gui = Gui.gui()
            .title(Component.text("OWWR · Chunky Control"))
            .rows(rows)
            .create();

        if (chunkyWorlds.isEmpty()) {
            ItemStack none = new ItemStack(Material.BARRIER);
            ItemMeta meta = none.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("No worlds with chunky.enabled=true"));
                none.setItemMeta(meta);
            }
            gui.setItem(1, 5, new GuiItem(none));
        } else {
            for (ResourceWorld w : chunkyWorlds) {
                gui.addItem(buildItem(player, w));
            }
        }

        gui.open(player);
    }

    private GuiItem buildItem(Player player, ResourceWorld w) {
        ItemStack icon = new ItemStack(Material.OBSERVER);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(w.id()));
            meta.lore(buildLore(w));
            icon.setItemMeta(meta);
        }
        return new GuiItem(icon, event -> {
            String action = switch (event.getClick()) {
                case LEFT -> "start";
                case RIGHT -> "cancel";
                case SHIFT_LEFT -> "pause";
                case SHIFT_RIGHT -> "resume";
                case MIDDLE -> null;
                default -> null;
            };
            if (action == null) {
                // refresh
                open(player);
                return;
            }
            Bukkit.dispatchCommand(player, "owwr chunky " + action + " " + w.id());
            // re-open to show updated status
            open(player);
        });
    }

    private List<Component> buildLore(ResourceWorld w) {
        List<Component> lore = new ArrayList<>();
        Optional<PregenProgress> prog = pregen.progressOf(w.worldName());
        if (prog.isEmpty()) {
            lore.add(Component.text("Status: idle"));
        } else {
            PregenProgress p = prog.get();
            lore.add(Component.text("Status: " + p.state()));
            if (p.state() == PregenState.RUNNING || p.state() == PregenState.PAUSED) {
                lore.add(Component.text(String.format("Progress: %.1f%% (%d/%d)",
                    p.percent(), p.chunksDone(), p.chunksTotal())));
                p.eta().ifPresent(eta -> lore.add(Component.text("ETA: " + eta)));
            }
        }
        lore.add(Component.empty());
        lore.add(Component.text("Left-click: start"));
        lore.add(Component.text("Right-click: cancel"));
        lore.add(Component.text("Shift+Left: pause · Shift+Right: resume"));
        lore.add(Component.text("Middle-click: refresh"));
        return lore;
    }
}
