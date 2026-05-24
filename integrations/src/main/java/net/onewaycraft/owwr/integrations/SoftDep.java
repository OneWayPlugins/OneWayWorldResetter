package net.onewaycraft.owwr.integrations;

import org.bukkit.Bukkit;

/**
 * @brief Utilitário para verificar plugins-alvo disponíveis em runtime.
 */
public final class SoftDep {
    private SoftDep() {}

    public static boolean isPresent(String pluginName) {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }
}
