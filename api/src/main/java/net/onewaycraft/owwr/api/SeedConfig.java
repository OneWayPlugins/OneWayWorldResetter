package net.onewaycraft.owwr.api;

import java.util.List;
import java.util.Objects;

/**
 * @brief Configuração de seed para regeneração do mundo.
 */
public record SeedConfig(SeedStrategy strategy, List<Long> values) {
    public SeedConfig {
        Objects.requireNonNull(strategy, "strategy");
        values = List.copyOf(values);
    }
}
