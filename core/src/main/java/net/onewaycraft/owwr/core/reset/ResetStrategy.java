package net.onewaycraft.owwr.core.reset;

import net.onewaycraft.owwr.api.ResetResult;
import net.onewaycraft.owwr.core.preflight.ResetContext;

/**
 * @brief Estratégia de reset (in-place ou double-buffered).
 */
public interface ResetStrategy {
    String id();
    ResetResult execute(ResetContext ctx, PhaseExecutor exec);
}
