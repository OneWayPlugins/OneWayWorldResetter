package net.onewaycraft.owwr.core.teleport;

import java.util.UUID;

/**
 * @brief Referência opaca a um jogador (UUID + nome) para uso pelo core.
 */
public record PlayerRef(UUID uuid, String name) {}
