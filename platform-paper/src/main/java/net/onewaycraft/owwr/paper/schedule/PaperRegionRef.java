package net.onewaycraft.owwr.paper.schedule;

import net.onewaycraft.owwr.core.schedule.RegionRef;

/**
 * @brief Implementação inerte para Paper (sem regionalização).
 */
public record PaperRegionRef(String worldName, int chunkX, int chunkZ) implements RegionRef {}
