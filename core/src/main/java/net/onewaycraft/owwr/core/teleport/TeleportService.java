package net.onewaycraft.owwr.core.teleport;

import java.util.List;

/**
 * @brief Teleporta jogadores em massa para um mundo de destino seguro.
 */
public interface TeleportService {

    /**
     * @brief Retorna lista de jogadores atualmente em {@code worldName}.
     */
    List<PlayerRef> playersIn(String worldName);

    /**
     * @brief Move todos os jogadores listados para o mundo destino (spawn seguro).
     * @return número de jogadores efetivamente movidos.
     */
    int evacuate(List<PlayerRef> players, String destinationWorld);

    /**
     * @brief Teleporta um jogador para o resource world especificado.
     */
    boolean teleportTo(PlayerRef player, String worldName);
}
