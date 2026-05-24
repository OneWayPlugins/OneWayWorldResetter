package net.onewaycraft.owwr.core.preflight;

import net.onewaycraft.owwr.api.GateResult;
import net.onewaycraft.owwr.api.ResetConfig;

/**
 * @brief Falha (delay/abort) se TPS &lt; min-tps.
 */
public final class TpsGate implements PreflightGate {
    @Override public String name() { return "min-tps"; }

    @Override public GateResult check(ResetContext ctx) {
        ResetConfig.GateConfig cfg = ctx.world().reset().gates().get(name());
        if (cfg == null) return GateResult.pass();
        if (ctx.snapshot().tps() >= cfg.value()) return GateResult.pass();
        return failWith(cfg, "tps=" + ctx.snapshot().tps() + " < " + cfg.value());
    }

    static GateResult failWith(ResetConfig.GateConfig cfg, String reason) {
        return switch (cfg.onFail()) {
            case "abort" -> GateResult.abort(reason);
            case "force" -> GateResult.pass();
            default -> GateResult.delay(reason);
        };
    }
}
