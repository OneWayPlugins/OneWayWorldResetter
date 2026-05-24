package net.onewaycraft.owwr.api.event;

import net.onewaycraft.owwr.api.ResetResult;

/**
 * @brief Disparado após o reset (sucesso ou falha).
 */
public final class PostResetEvent implements ResetEvent {
    private final ResetResult result;
    public PostResetEvent(ResetResult result) { this.result = result; }
    @Override public String worldId() { return result.worldId(); }
    public ResetResult result() { return result; }
}
