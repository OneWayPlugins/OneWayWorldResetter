package net.onewaycraft.owwr.core.config;

import net.onewaycraft.owwr.api.*;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YamlConfigLoaderTest {

    @Test
    void loadsSampleConfigWithSingleWorld() {
        InputStream in = getClass().getResourceAsStream("/config-sample.yml");
        OwwrConfig cfg = new YamlConfigLoader().load(in);

        assertThat(cfg.settings().locale()).isEqualTo("en");
        assertThat(cfg.settings().maxConcurrentResets()).isEqualTo(1);
        assertThat(cfg.worlds()).hasSize(1);

        ResourceWorld mining = cfg.worlds().get(0);
        assertThat(mining.id()).isEqualTo("mining");
        assertThat(mining.worldName()).isEqualTo("world_resource");
        assertThat(mining.environment()).isEqualTo(Environment.NORMAL);
        assertThat(mining.enabled()).isTrue();
        assertThat(mining.seed().strategy()).isEqualTo(SeedStrategy.CYCLING);
        assertThat(mining.seed().values()).isEqualTo(List.of(12345L, 67890L));
        assertThat(mining.reset().strategy()).isEqualTo("in-place");
        assertThat(mining.reset().schedule().type()).isEqualTo(ResetType.DAILY);
        assertThat(mining.reset().warningsMinutes()).containsExactly(30, 10, 5, 1);
        assertThat(mining.reset().gates()).containsKeys("min-tps", "max-players", "min-disk-gb");
        assertThat(mining.teleport().mode()).isEqualTo("rtp");
        assertThat(mining.regions().enabled()).isFalse();
    }
}
