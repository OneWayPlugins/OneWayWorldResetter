package net.onewaycraft.owwr.api;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @brief Settings aplicadas ao mundo após o recreate (difficulty, gamerules, border).
 */
public record WorldSettings(
    String difficulty,
    Map<String, String> gamerules,
    Optional<Border> border
) {
    public WorldSettings {
        Objects.requireNonNull(difficulty, "difficulty");
        gamerules = Map.copyOf(gamerules);
        Objects.requireNonNull(border, "border");
    }

    public record Border(double centerX, double centerZ, double size) {}
}
