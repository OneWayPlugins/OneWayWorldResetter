package net.onewaycraft.owwr.api;

/**
 * @brief Configuração de teleporte para um resource world.
 */
public record TeleportConfig(
    String mode,                  // "spawn" | "rtp"
    boolean safeLocation,
    String destinationOnReset     // nome do mundo seguro
) {}
