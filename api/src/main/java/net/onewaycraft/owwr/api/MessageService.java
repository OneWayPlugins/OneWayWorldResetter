package net.onewaycraft.owwr.api;

import java.util.Map;

/**
 * @brief Resolve mensagens i18n com placeholders e cores hex.
 *
 * Placeholders no formato {placeholder}. Cores hex no formato &#RRGGBB.
 */
public interface MessageService {
    String resolve(String key);
    String resolve(String key, Map<String, String> placeholders);
}
