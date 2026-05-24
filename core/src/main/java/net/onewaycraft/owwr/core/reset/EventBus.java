package net.onewaycraft.owwr.core.reset;

import net.onewaycraft.owwr.api.event.ResetEvent;

/**
 * @brief Dispara eventos para o resto do servidor. Impl em platform-paper.
 *
 * @return true se o evento foi cancelado (apenas para eventos canceláveis).
 */
public interface EventBus {
    boolean fire(ResetEvent event);
}
