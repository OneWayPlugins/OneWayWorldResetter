package net.onewaycraft.owwr.integrations.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.onewaycraft.owwr.api.OwwrConfig;
import net.onewaycraft.owwr.api.ResourceWorld;
import net.onewaycraft.owwr.core.persistence.HistoryRepository;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @brief PAPI expansion. Placeholders:
 *
 * - %owwr_status_<id>%  -> "enabled" | "disabled" | "unknown"
 * - %owwr_last_<id>%    -> timestamp do último reset bem-sucedido, ou "never"
 */
public final class OwwrExpansion extends PlaceholderExpansion {

    private final OwwrConfig config;
    private final HistoryRepository history;
    private final String version;

    public OwwrExpansion(OwwrConfig config, HistoryRepository history, String version) {
        this.config = config;
        this.history = history;
        this.version = version;
    }

    @Override public String getIdentifier() { return "owwr"; }
    @Override public String getAuthor() { return "OneWayCraft"; }
    @Override public String getVersion() { return version; }
    @Override public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params == null) return null;
        int sep = params.indexOf('_');
        if (sep < 0) return null;
        String kind = params.substring(0, sep);
        String worldId = params.substring(sep + 1);

        Optional<ResourceWorld> rw = config.worlds().stream()
            .filter(w -> w.id().equalsIgnoreCase(worldId)).findFirst();

        return switch (kind) {
            case "status" -> rw.map(w -> w.enabled() ? "enabled" : "disabled").orElse("unknown");
            case "last" -> history.recent(100).stream()
                .filter(r -> r.worldId().equalsIgnoreCase(worldId) && r.success())
                .map(r -> r.startedAt().toString())
                .reduce((a, b) -> b).orElse("never");
            default -> null;
        };
    }
}
