package net.onewaycraft.owwr.api;

import java.time.Instant;

/**
 * @brief Estado serializável de um reset em andamento (crash recovery).
 */
public record ResetState(
    String worldId,
    ResetPhase phase,
    int attempt,
    Instant startedAt,
    Instant lastTransitionAt
) {}
