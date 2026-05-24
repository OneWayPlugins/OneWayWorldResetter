package net.onewaycraft.owwr.api;

/**
 * @brief Fase atual da máquina de reset.
 *
 * Transições válidas: IDLE → PRECHECK → TELEPORT → UNLOAD → DELETE → RECREATE →
 *                    PREGEN → VERIFY → COMPLETE. A partir de qualquer fase pode-se ir para FAILED.
 *
 * PREGEN é opcional — só roda quando a configuração chunky.enabled = true para o mundo.
 */
public enum ResetPhase {
    IDLE, PRECHECK, TELEPORT, UNLOAD, DELETE, RECREATE, PREGEN, VERIFY, COMPLETE, FAILED
}
