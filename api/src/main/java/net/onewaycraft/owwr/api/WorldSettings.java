package net.onewaycraft.owwr.api;

import java.util.Map;
import java.util.Optional;

/**
 * @brief Settings aplicadas ao mundo após o recreate (difficulty, gamerules, border).
 */
public record WorldSettings(
    String difficulty,                 // "PEACEFUL" | "EASY" | "NORMAL" | "HARD"
    Map<String, String> gamerules,
    Optional<Border> border
) {
    public record Border(double centerX, double centerZ, double size) {}
}
