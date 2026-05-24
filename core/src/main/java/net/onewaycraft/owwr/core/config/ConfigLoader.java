package net.onewaycraft.owwr.core.config;

import net.onewaycraft.owwr.api.OwwrConfig;
import java.io.InputStream;

/**
 * @brief Interface para carregar a OwwrConfig a partir de uma fonte de bytes.
 */
public interface ConfigLoader {
    OwwrConfig load(InputStream source);
}
