package net.onewaycraft.owwr.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @brief Definição de um resource world gerenciado pelo plugin.
 */
public record ResourceWorld(
    String id,
    String worldName,
    Environment environment,
    boolean enabled,
    boolean autoCreate,
    SeedConfig seed,
    ResetConfig reset,
    TeleportConfig teleport,
    WorldSettings worldSettings,
    RegionConfig regions,
    Map<String, Boolean> copyRegions,
    Map<String, Object> notifications
) {
    public ResourceWorld {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(seed, "seed");
        Objects.requireNonNull(reset, "reset");
        Objects.requireNonNull(teleport, "teleport");
        Objects.requireNonNull(worldSettings, "worldSettings");
        Objects.requireNonNull(regions, "regions");
        copyRegions = Map.copyOf(copyRegions);
        notifications = Map.copyOf(notifications);
    }

    /**
     * @brief Configuração de reset por região (lista de coords).
     */
    public record RegionConfig(boolean enabled, List<RegionCoord> list) {
        public RegionConfig {
            list = List.copyOf(list);
        }

        public record RegionCoord(int x, int z) {}
    }
}
