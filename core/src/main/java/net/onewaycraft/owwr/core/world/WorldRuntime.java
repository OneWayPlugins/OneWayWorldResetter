package net.onewaycraft.owwr.core.world;

/**
 * @brief Operações de runtime de mundo (não-ciclo-de-vida) — autosave, etc.
 */
public interface WorldRuntime {
    void setAutosave(String worldName, boolean enabled);
}
