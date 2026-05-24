package net.onewaycraft.owwr.core.config;

import net.onewaycraft.owwr.api.ChunkyConfig;
import net.onewaycraft.owwr.api.OwwrConfig;
import net.onewaycraft.owwr.api.ResourceWorld;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChunkyConfigParseTest {

    private OwwrConfig load() {
        InputStream in = getClass().getResourceAsStream("/config-with-chunky.yml");
        return new YamlConfigLoader().load(in);
    }

    private Optional<ChunkyConfig> chunkyOf(OwwrConfig cfg, String worldId) {
        ResourceWorld rw = cfg.worlds().stream()
            .filter(w -> w.id().equals(worldId))
            .findFirst().orElseThrow();
        return Optional.ofNullable(rw.reset().chunky());
    }

    @Test
    void parsesFullChunkyConfigWhenPresent() {
        ChunkyConfig c = chunkyOf(load(), "mining").orElseThrow();
        assertThat(c.enabled()).isTrue();
        assertThat(c.shape()).isEqualTo("square");
        assertThat(c.centerX()).isEqualTo(100.0);
        assertThat(c.centerZ()).isEqualTo(-200.0);
        assertThat(c.radius()).isEqualTo(5000.0);
        assertThat(c.maxDurationMinutes()).isEqualTo(60);
        assertThat(c.blockTeleportDuringPregen()).isTrue();
        assertThat(c.failureBehavior()).isEqualTo("critical");
        assertThat(c.notifications()).containsEntry("bossbar", true)
            .containsEntry("actionbar", false)
            .containsEntry("discord", false);
    }

    @Test
    void returnsNullWhenEnabledIsFalse() {
        assertThat(chunkyOf(load(), "hunting")).isEmpty();
    }

    @Test
    void returnsNullWhenChunkyBlockAbsent() {
        assertThat(chunkyOf(load(), "nochunky")).isEmpty();
    }

    @Test
    void appliesDefaultsForOptionalFields() {
        // Minimal chunky block: only enabled, shape, center, radius are required.
        // Defaults: max-duration-minutes=60, block-teleport=true, failure-behavior=critical, notifications={}
        InputStream in = getClass().getResourceAsStream("/config-with-chunky-minimal.yml");
        OwwrConfig cfg = new YamlConfigLoader().load(in);
        ChunkyConfig c = cfg.worlds().get(0).reset().chunky();
        assertThat(c).isNotNull();
        assertThat(c.maxDurationMinutes()).isEqualTo(60);
        assertThat(c.blockTeleportDuringPregen()).isTrue();
        assertThat(c.failureBehavior()).isEqualTo("critical");
        assertThat(c.notifications()).isEmpty();
    }

    @Test
    void rejectsInvalidRadius() {
        InputStream in = getClass().getResourceAsStream("/config-with-chunky-bad-radius.yml");
        assertThatThrownBy(() -> new YamlConfigLoader().load(in))
            .isInstanceOfAny(IllegalArgumentException.class, NullPointerException.class);
    }

    @Test
    void rejectsInvalidFailureBehavior() {
        InputStream in = getClass().getResourceAsStream("/config-with-chunky-bad-failure.yml");
        assertThatThrownBy(() -> new YamlConfigLoader().load(in))
            .isInstanceOfAny(IllegalArgumentException.class, NullPointerException.class);
    }
}
