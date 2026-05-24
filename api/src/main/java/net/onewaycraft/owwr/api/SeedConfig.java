package net.onewaycraft.owwr.api;

import java.util.List;

/**
 * @brief Configuração de seed para regeneração do mundo.
 */
public record SeedConfig(SeedStrategy strategy, List<Long> values) {}
