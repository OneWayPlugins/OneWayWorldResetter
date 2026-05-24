package net.onewaycraft.owwr.api;

import java.util.Map;
import java.util.Objects;

/**
 * @brief Configuração de pre-gen via Chunky por mundo.
 *
 * @param enabled                    Habilita pre-gen pós-reset. Quando false, a fase PREGEN é skipada.
 * @param shape                      "square", "circle", "star", "diamond" ou "triangle".
 * @param centerX                    Centro X em blocos.
 * @param centerZ                    Centro Z em blocos.
 * @param radius                     Raio em blocos (deve ser > 0).
 * @param maxDurationMinutes         Limite de tempo do pre-gen; expirar é sucesso parcial.
 * @param blockTeleportDuringPregen  Bloqueia /resource para esse mundo enquanto pre-gen roda.
 * @param failureBehavior            "critical" (default; falha = retry padrão) | "warning" (falha = sucesso parcial).
 * @param notifications              Mapa de flags: chaves esperadas "bossbar", "actionbar", "discord".
 */
public record ChunkyConfig(
    boolean enabled,
    String shape,
    double centerX,
    double centerZ,
    double radius,
    int maxDurationMinutes,
    boolean blockTeleportDuringPregen,
    String failureBehavior,
    Map<String, Boolean> notifications
) {
    public ChunkyConfig {
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(failureBehavior, "failureBehavior");
        Objects.requireNonNull(notifications, "notifications");
        notifications = Map.copyOf(notifications);
        if (radius <= 0) throw new IllegalArgumentException("radius must be > 0");
        if (maxDurationMinutes <= 0) throw new IllegalArgumentException("maxDurationMinutes must be > 0");
        if (!"critical".equals(failureBehavior) && !"warning".equals(failureBehavior)) {
            throw new IllegalArgumentException("failureBehavior must be 'critical' or 'warning'");
        }
    }
}
