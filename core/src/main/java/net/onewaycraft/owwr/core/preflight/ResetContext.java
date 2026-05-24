package net.onewaycraft.owwr.core.preflight;

import net.onewaycraft.owwr.api.ResourceWorld;

/**
 * @brief Contexto compartilhado entre gates e fases (read-only).
 */
public record ResetContext(ResourceWorld world, ServerSnapshot snapshot, boolean dryRun) {}
