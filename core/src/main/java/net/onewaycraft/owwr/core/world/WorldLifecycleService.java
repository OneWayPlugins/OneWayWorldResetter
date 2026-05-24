package net.onewaycraft.owwr.core.world;

import java.nio.file.Path;

/**
 * @brief Operações de ciclo de vida de mundos, abstraindo Paper/Folia/Multiverse.
 *
 * Implementações em platform-paper (BukkitNativeAdapter), platform-folia
 * (FoliaWorldManagerAdapter) e integrations (MultiverseAdapter).
 */
public interface WorldLifecycleService {

    boolean worldExists(String name);

    /** Cria o mundo conforme spec. Retorna fail se já existe ou se o backend falhar. */
    WorldOpResult createWorld(WorldSpec spec);

    /** Descarrega o mundo do servidor (não apaga arquivos). Falha se houver jogadores. */
    WorldOpResult unloadWorld(String name);

    /** Apaga o diretório do mundo do disco. PRÉ-REQUISITO: isSafeToDelete == true. */
    WorldOpResult deleteWorld(String name);

    /**
     * @brief Validação de path-safety: recusa qualquer alvo que não esteja sob o
     *        worldContainer canonical do servidor.
     */
    boolean isSafeToDelete(Path target);
}
