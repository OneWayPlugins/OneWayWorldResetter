package net.onewaycraft.owwr.api;

import java.time.Duration;
import java.time.Instant;

/**
 * @brief Entrada de histórico persistida em JSON.
 */
public record ResetRecord(
    String worldId,
    Instant startedAt,
    Duration duration,
    boolean success,
    ResetPhase finalPhase,
    int affectedPlayers,
    String failureReason
) {}
