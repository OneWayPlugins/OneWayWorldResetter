package net.onewaycraft.owwr.core.preflight;

import net.onewaycraft.owwr.api.GateResult;
import net.onewaycraft.owwr.api.ResetConfig;

public final class DiskSpaceGate implements PreflightGate {
    @Override public String name() { return "min-disk-gb"; }

    @Override public GateResult check(ResetContext ctx) {
        ResetConfig.GateConfig cfg = ctx.world().reset().gates().get(name());
        if (cfg == null) return GateResult.pass();
        long minBytes = (long) (cfg.value() * 1_000_000_000L);
        if (ctx.snapshot().freeDiskBytes() >= minBytes) return GateResult.pass();
        return TpsGate.failWith(cfg, "free=" + ctx.snapshot().freeDiskBytes() + "B < " + minBytes);
    }
}
