package net.onewaycraft.owwr.core.schedule;

/**
 * @brief Referência opaca a uma região do mundo (usada pelo FoliaScheduler).
 *
 * Em Paper é ignorada (toda chamada cai no main thread).
 * Em Folia, é construída a partir de uma Location pelo bridge platform-folia.
 */
public interface RegionRef {
    String worldName();
    int chunkX();
    int chunkZ();
}
