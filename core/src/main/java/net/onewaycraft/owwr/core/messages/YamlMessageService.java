package net.onewaycraft.owwr.core.messages;

import net.onewaycraft.owwr.api.MessageService;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @brief MessageService que lê de um messages.yml (mapas aninhados → chaves "a.b.c").
 *
 * Suporta auto-resolução de placeholders top-level (ex.: {prefix}) para evitar
 * repetição em cada mensagem. Apenas chaves escalares de topo entram no namespace
 * de placeholders — chaves aninhadas como "reset.starting" NÃO podem ser usadas
 * como placeholders e ficam isoladas no namespace de mensagens.
 */
public final class YamlMessageService implements MessageService {

    private final Map<String, String> messages;
    private final Map<String, String> topLevelScalars;

    private YamlMessageService(Map<String, String> messages, Map<String, String> topLevelScalars) {
        this.messages = messages;
        this.topLevelScalars = topLevelScalars;
    }

    public static YamlMessageService fromYaml(InputStream source) {
        @SuppressWarnings("unchecked")
        Map<String, Object> root = new Yaml().load(source);
        if (root == null) root = Map.of();

        Map<String, String> messages = new LinkedHashMap<>();
        Map<String, String> topLevelScalars = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : root.entrySet()) {
            if (e.getValue() instanceof Map<?, ?> m) {
                @SuppressWarnings("unchecked")
                Map<String, Object> sub = (Map<String, Object>) m;
                flatten(e.getKey(), sub, messages);
            } else {
                String value = String.valueOf(e.getValue());
                messages.put(e.getKey(), value);
                topLevelScalars.put(e.getKey(), value);
            }
        }
        return new YamlMessageService(Map.copyOf(messages), Map.copyOf(topLevelScalars));
    }

    @SuppressWarnings("unchecked")
    private static void flatten(String prefix, Map<String, Object> node, Map<String, String> out) {
        for (Map.Entry<String, Object> e : node.entrySet()) {
            String key = prefix + "." + e.getKey();
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
        String raw = messages.getOrDefault(key, key);
        return apply(raw, placeholders);
    }

    private String apply(String template, Map<String, String> placeholders) {
        String out = template;
        for (Map.Entry<String, String> e : topLevelScalars.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue());
        }
        for (Map.Entry<String, String> p : placeholders.entrySet()) {
            out = out.replace("{" + p.getKey() + "}", p.getValue());
        }
        return out;
    }
}
