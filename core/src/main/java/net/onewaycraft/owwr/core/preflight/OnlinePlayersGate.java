package net.onewaycraft.owwr.core.preflight;

import net.onewaycraft.owwr.api.GateResult;
import net.onewaycraft.owwr.api.ResetConfig;

public final class OnlinePlayersGate implements PreflightGate {
    @Override public String name() { return "max-players"; }

    @Override public GateResult check(ResetContext ctx) {
        ResetConfig.GateConfig cfg = ctx.world().reset().gates().get(name());
        if (cfg == null) return GateResult.pass();
        if (ctx.snapshot().onlinePlayers() <= cfg.value()) return GateResult.pass();
        return TpsGate.failWith(cfg, "players=" + ctx.snapshot().onlinePlayers() + " > " + cfg.value());
    }
}
