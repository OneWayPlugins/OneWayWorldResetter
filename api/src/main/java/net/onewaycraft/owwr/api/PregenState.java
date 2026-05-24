package net.onewaycraft.owwr.api;

/**
 * @brief Estado atual de uma tarefa de pré-geração do Chunky para um mundo.
 *
 * IDLE: nunca iniciada ou já encerrada.
 * RUNNING: em progresso.
 * PAUSED: pausada por comando admin ou auto-pause do Chunky.
 * COMPLETED: terminou normalmente.
 * FAILED: erro hard durante a geração.
 */
public enum PregenState { IDLE, RUNNING, PAUSED, COMPLETED, FAILED }
