package net.onewaycraft.owwr.api;

import java.util.List;
import java.util.Objects;

/**
 * @brief Raiz da configuração carregada do config.yml.
 */
public record OwwrConfig(Settings settings, List<ResourceWorld> worlds) {
    public OwwrConfig {
        Objects.requireNonNull(settings, "settings");
        worlds = List.copyOf(worlds);
    }

    /**
     * @brief Configurações globais do plugin.
     */
    public record Settings(
        String locale,
        int maxConcurrentResets,
        boolean updateChecker,
        boolean metrics
    ) {
        public Settings {
            Objects.requireNonNull(locale, "locale");
            if (maxConcurrentResets < 1) {
                throw new IllegalArgumentException("maxConcurrentResets must be >= 1");
            }
        }
    }
}
