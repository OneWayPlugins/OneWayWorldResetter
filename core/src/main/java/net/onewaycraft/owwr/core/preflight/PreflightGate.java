package net.onewaycraft.owwr.core.preflight;

import net.onewaycraft.owwr.api.GateResult;

/**
 * @brief Verificação executada na fase PRECHECK.
 */
public interface PreflightGate {
    String name();
    GateResult check(ResetContext ctx);
}
