package net.onewaycraft.owwr.core.messages;

import net.onewaycraft.owwr.api.MessageService;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class YamlMessageServiceTest {

    private MessageService load() {
        InputStream in = getClass().getResourceAsStream("/messages-test.yml");
        return YamlMessageService.fromYaml(in);
    }

    @Test
    void resolvesNestedKeyWithPlaceholders() {
        String out = load().resolve("reset.starting", Map.of("world", "mining", "minutes", "5"));
        // {prefix} resolved, world/minutes filled, hex codes preserved.
        assertThat(out).isEqualTo("&#00FF00[OWWR] Starting reset of &#FFAA00mining&r in 5 min");
    }

    @Test
    void resolvesKeyWithoutPlaceholders() {
        assertThat(load().resolve("errors.no-permission")).isEqualTo("&cYou lack permission.");
    }

    @Test
    void returnsKeyItselfWhenMissing() {
        assertThat(load().resolve("does.not.exist")).isEqualTo("does.not.exist");
    }
}
