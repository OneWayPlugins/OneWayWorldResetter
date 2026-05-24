package net.onewaycraft.owwr.api;

/**
 * @brief Fase atual da máquina de reset.
 *
 * Transições válidas: IDLE → PRECHECK → TELEPORT → UNLOAD → DELETE → RECREATE →
 *                    VERIFY → COMPLETE. A partir de qualquer fase pode-se ir para FAILED.
 */
public enum ResetPhase {
    IDLE, PRECHECK, TELEPORT, UNLOAD, DELETE, RECREATE, VERIFY, COMPLETE, FAILED
}
