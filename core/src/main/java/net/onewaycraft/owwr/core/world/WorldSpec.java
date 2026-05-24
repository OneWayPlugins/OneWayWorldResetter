package net.onewaycraft.owwr.core.world;

import net.onewaycraft.owwr.api.Environment;

/**
 * @brief Especificação para criação de um mundo.
 */
public record WorldSpec(String name, Environment environment, long seed) {
    public WorldSpec {
        java.util.Objects.requireNonNull(name, "name");
        java.util.Objects.requireNonNull(environment, "environment");
    }
}
