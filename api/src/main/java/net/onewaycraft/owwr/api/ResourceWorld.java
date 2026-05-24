package net.onewaycraft.owwr.api;

import java.util.List;
import java.util.Map;

/**
 * @brief Definição de um resource world gerenciado pelo plugin.
 */
public record ResourceWorld(
    String id,                       // chave lógica (ex: "mining")
    String worldName,                // nome do diretório do mundo
    Environment environment,
    boolean enabled,
    boolean autoCreate,
    SeedConfig seed,
    ResetConfig reset,
    TeleportConfig teleport,
    WorldSettings worldSettings,
    RegionConfig regions,
    Map<String, Boolean> copyRegions,         // "worldguard"/"residence"/"griefprevention" → bool
    Map<String, Object> notifications         // bossbar/title/actionbar/discord-webhook
) {
    /**
     * @brief Configuração de reset por região (lista de coords).
     */
    public record RegionConfig(boolean enabled, List<RegionCoord> list) {
        public record RegionCoord(int x, int z) {}
    }
}
