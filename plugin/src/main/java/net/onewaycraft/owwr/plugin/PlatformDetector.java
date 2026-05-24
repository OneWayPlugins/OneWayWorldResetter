package net.onewaycraft.owwr.plugin;

import net.onewaycraft.owwr.core.platform.Platform;

/**
 * @brief Detecta Folia pela presença de RegionizedServer no classpath.
 */
public final class PlatformDetector {
    public static Platform detect() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return Platform.FOLIA;
        } catch (ClassNotFoundException e) {
            return Platform.PAPER;
        }
    }

    private PlatformDetector() {}
}
