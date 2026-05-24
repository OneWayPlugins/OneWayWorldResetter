package net.onewaycraft.owwr.api;

import java.util.List;

/**
 * @brief Raiz da configuração carregada do config.yml.
 */
public record OwwrConfig(Settings settings, List<ResourceWorld> worlds) {

    /**
     * @brief Configurações globais do plugin.
     */
    public record Settings(
        String locale,
        int maxConcurrentResets,
        boolean updateChecker,
        boolean metrics
    ) {}
}
