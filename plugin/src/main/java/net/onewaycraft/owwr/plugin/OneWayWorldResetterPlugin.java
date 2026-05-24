package net.onewaycraft.owwr.plugin;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @brief Bootstrap do OneWayWorldResetter — composition root único.
 *
 * Detecta a plataforma (Folia presente?) e monta o grafo de objetos por construtor.
 * Não cria nenhuma lógica de domínio aqui: tudo vive em core/api.
 */
public final class OneWayWorldResetterPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("OneWayWorldResetter " + getDescription().getVersion() + " starting up.");
    }

    @Override
    public void onDisable() {
        getLogger().info("OneWayWorldResetter shutting down.");
    }
}
