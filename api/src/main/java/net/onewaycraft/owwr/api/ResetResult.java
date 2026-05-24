package net.onewaycraft.owwr.api;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

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
    String failureReason
) {
    public ResetResult {
        Objects.requireNonNull(worldId, "worldId");
        Objects.requireNonNull(finalPhase, "finalPhase");
        Objects.requireNonNull(totalDuration, "totalDuration");
        phaseDurations = Map.copyOf(phaseDurations);
        // failureReason may be null when success=true
    }
}
