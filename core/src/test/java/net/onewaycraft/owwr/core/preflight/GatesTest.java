package net.onewaycraft.owwr.core.preflight;

import net.onewaycraft.owwr.api.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GatesTest {

    private ResetContext ctx(ServerSnapshot snap, ResetConfig.GateConfig gateCfg) {
        ResetConfig rc = new ResetConfig(
            "in-place",
            Schedule.daily(java.time.LocalTime.NOON),
            List.of(),
            Map.of("min-tps", gateCfg, "max-players", gateCfg, "min-disk-gb", gateCfg),
            true, true, null);
        ResourceWorld rw = new ResourceWorld("id", "world_resource", Environment.NORMAL, true, true,
            new SeedConfig(SeedStrategy.RANDOM, List.of()),
            rc,
            new TeleportConfig("rtp", true, "world"),
            new WorldSettings("NORMAL", Map.of(), Optional.empty()),
            new ResourceWorld.RegionConfig(false, List.of()),
            Map.of(), Map.of());
        return new ResetContext(rw, snap, false);
    }

    @Test
    void tpsGatePassesAboveThreshold() {
        var g = new TpsGate();
        var r = g.check(ctx(new ServerSnapshot(18.0, 0, Long.MAX_VALUE),
            new ResetConfig.GateConfig(15.0, "delay")));
        assertThat(r).isEqualTo(GateResult.pass());
    }

    @Test
    void tpsGateDelaysBelowThreshold() {
        var g = new TpsGate();
        var r = g.check(ctx(new ServerSnapshot(10.0, 0, Long.MAX_VALUE),
            new ResetConfig.GateConfig(15.0, "delay")));
        assertThat(r).isInstanceOf(GateResult.Delay.class);
    }

    @Test
    void onlinePlayersGateAbortsAboveThreshold() {
        var g = new OnlinePlayersGate();
        var r = g.check(ctx(new ServerSnapshot(20.0, 50, Long.MAX_VALUE),
            new ResetConfig.GateConfig(10.0, "abort")));
        assertThat(r).isInstanceOf(GateResult.Abort.class);
    }

    @Test
    void diskGateAbortsBelowGb() {
        var g = new DiskSpaceGate();
        var r = g.check(ctx(new ServerSnapshot(20.0, 0, 1_000_000_000L),
            new ResetConfig.GateConfig(5.0, "abort")));
        assertThat(r).isInstanceOf(GateResult.Abort.class);
    }
}
