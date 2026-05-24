package net.onewaycraft.owwr.api;

import java.time.Duration;
import java.util.Map;

/**
 * @brief Resultado consolidado de um reset.
 */
public record ResetResult(
    String worldId,
    boolean success,
    ResetPhase finalPhase,
    Duration totalDuration,
    Map<ResetPhase, Duration> phaseDurations,
    int affectedPlayers,
    String failureReason  // null se success=true
) {}
