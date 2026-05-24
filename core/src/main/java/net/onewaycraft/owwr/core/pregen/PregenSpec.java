package net.onewaycraft.owwr.core.pregen;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * @brief Especificação imutável de uma tarefa de pre-gen.
 *
 * Derivada de {@link net.onewaycraft.owwr.api.ChunkyConfig}; este record é o que
 * passa pela interface PregenService para que a impl não precise conhecer ChunkyConfig.
 *
 * @param worldName        nome do mundo
 * @param shape            "square" | "circle" | "star" | "diamond" | "triangle"
 * @param centerX          centro X em blocos
 * @param centerZ          centro Z em blocos
 * @param radius           raio em blocos
 * @param maxDuration      tempo máximo da tarefa (após isso, observador encerra)
 * @param notifications    flags {bossbar, actionbar, discord}
 */
public record PregenSpec(
    String worldName,
    String shape,
    double centerX,
    double centerZ,
    double radius,
    Duration maxDuration,
    Map<String, Boolean> notifications
) {
    public PregenSpec {
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(maxDuration, "maxDuration");
        Objects.requireNonNull(notifications, "notifications");
        notifications = Map.copyOf(notifications);
        if (radius <= 0) throw new IllegalArgumentException("radius must be > 0");
        if (maxDuration.isNegative() || maxDuration.isZero())
            throw new IllegalArgumentException("maxDuration must be > 0");
    }
}
