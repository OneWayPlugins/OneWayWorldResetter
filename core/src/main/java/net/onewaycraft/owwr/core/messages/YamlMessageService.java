package net.onewaycraft.owwr.core.messages;

import net.onewaycraft.owwr.api.MessageService;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @brief MessageService que lê de um messages.yml (mapas aninhados → chaves "a.b.c").
 *
 * Suporta auto-resolução de {prefix} para evitar repetição em cada chave.
 */
public final class YamlMessageService implements MessageService {

    private final Map<String, String> flat;

    private YamlMessageService(Map<String, String> flat) {
        this.flat = flat;
    }

    public static YamlMessageService fromYaml(InputStream source) {
        @SuppressWarnings("unchecked")
        Map<String, Object> root = new Yaml().load(source);
        Map<String, String> flat = new LinkedHashMap<>();
        flatten("", root, flat);
        return new YamlMessageService(Map.copyOf(flat));
    }

    @SuppressWarnings("unchecked")
    private static void flatten(String prefix, Map<String, Object> node, Map<String, String> out) {
        for (Map.Entry<String, Object> e : node.entrySet()) {
            String key = prefix.isEmpty() ? e.getKey() : prefix + "." + e.getKey();
            if (e.getValue() instanceof Map<?, ?> m) {
                flatten(key, (Map<String, Object>) m, out);
            } else {
                out.put(key, String.valueOf(e.getValue()));
            }
        }
    }

    @Override
    public String resolve(String key) {
        return resolve(key, Map.of());
    }

    @Override
    public String resolve(String key, Map<String, String> placeholders) {
        String raw = flat.getOrDefault(key, key);
        return apply(raw, placeholders);
    }

    private String apply(String template, Map<String, String> placeholders) {
        String out = template;
        // expand {prefix} (and any other top-level scalar key) recursively (1 pass enough for typical cases)
        for (Map.Entry<String, String> e : flat.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue());
        }
        for (Map.Entry<String, String> p : placeholders.entrySet()) {
            out = out.replace("{" + p.getKey() + "}", p.getValue());
        }
        return out;
    }
}
