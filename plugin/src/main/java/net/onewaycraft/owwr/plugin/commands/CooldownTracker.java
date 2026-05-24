package net.onewaycraft.owwr.plugin.commands;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @brief Cooldown por jogador para comandos de teleporte.
 */
public final class CooldownTracker {

    private final ConcurrentHashMap<UUID, Instant> lastUse = new ConcurrentHashMap<>();
    private final Duration cooldown;

    public CooldownTracker(Duration cooldown) {
        this.cooldown = cooldown;
    }

    public long remainingSeconds(UUID uuid) {
        Instant last = lastUse.get(uuid);
        if (last == null) return 0;
        long left = cooldown.minus(Duration.between(last, Instant.now())).toSeconds();
        return Math.max(0, left);
    }

    public void markUsed(UUID uuid) { lastUse.put(uuid, Instant.now()); }
}
