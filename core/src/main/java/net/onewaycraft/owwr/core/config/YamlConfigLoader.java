package net.onewaycraft.owwr.core.config;

import net.onewaycraft.owwr.api.*;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

/**
 * @brief Implementação default do ConfigLoader usando SnakeYAML.
 *        Sem dependência de Bukkit — pode rodar em qualquer JVM.
 */
public final class YamlConfigLoader implements ConfigLoader {

    @Override
    @SuppressWarnings("unchecked")
    public OwwrConfig load(InputStream source) {
        Map<String, Object> root = new Yaml().load(source);
        if (root == null) root = Map.of();
        Map<String, Object> settingsMap = (Map<String, Object>) root.getOrDefault("settings", Map.of());
        OwwrConfig.Settings settings = new OwwrConfig.Settings(
            (String) settingsMap.getOrDefault("locale", "en"),
            ((Number) settingsMap.getOrDefault("max-concurrent-resets", 1)).intValue(),
            (Boolean) settingsMap.getOrDefault("update-checker", true),
            (Boolean) settingsMap.getOrDefault("metrics", true)
        );

        List<ResourceWorld> worlds = new ArrayList<>();
        Map<String, Object> worldsMap = (Map<String, Object>) root.getOrDefault("resource-worlds", Map.of());
        for (Map.Entry<String, Object> entry : worldsMap.entrySet()) {
            worlds.add(parseWorld(entry.getKey(), (Map<String, Object>) entry.getValue()));
        }
        return new OwwrConfig(settings, List.copyOf(worlds));
    }

    @SuppressWarnings("unchecked")
    private ResourceWorld parseWorld(String id, Map<String, Object> w) {
        String worldName = require(w, "world-name", id);
        SeedConfig seed = parseSeed((Map<String, Object>) w.getOrDefault("seed", Map.of()));
        ResetConfig reset = parseReset((Map<String, Object>) w.getOrDefault("reset", Map.of()), id);
        TeleportConfig tp = parseTeleport((Map<String, Object>) w.getOrDefault("teleport", Map.of()));
        WorldSettings ws = parseWorldSettings((Map<String, Object>) w.getOrDefault("world-settings", Map.of()));
        ResourceWorld.RegionConfig regions = parseRegions((Map<String, Object>) w.getOrDefault("regions", Map.of()));

        return new ResourceWorld(
            id,
            worldName,
            Environment.valueOf(((String) w.getOrDefault("environment", "NORMAL")).toUpperCase()),
            (Boolean) w.getOrDefault("enabled", true),
            (Boolean) w.getOrDefault("auto-create", true),
            seed,
            reset,
            tp,
            ws,
            regions,
            mapBooleanLowercase((Map<String, Object>) w.getOrDefault("copy-regions", Map.of())),
            (Map<String, Object>) w.getOrDefault("notifications", Map.of())
        );
    }

    private static String require(Map<String, Object> map, String key, String worldId) {
        Object v = map.get(key);
        if (v == null) {
            throw new IllegalArgumentException(
                "resource-worlds." + worldId + "." + key + " is required");
        }
        return v.toString();
    }

    private SeedConfig parseSeed(Map<String, Object> s) {
        SeedStrategy strat = SeedStrategy.valueOf(((String) s.getOrDefault("strategy", "RANDOM")).toUpperCase());
        @SuppressWarnings("unchecked")
        List<Object> raw = (List<Object>) s.getOrDefault("values", List.of());
        List<Long> values = raw.stream().map(o -> ((Number) o).longValue()).toList();
        return new SeedConfig(strat, values);
    }

    @SuppressWarnings("unchecked")
    private ResetConfig parseReset(Map<String, Object> r, String worldId) {
        Map<String, Object> schedMap = (Map<String, Object>) r.getOrDefault("schedule", Map.of());
        Schedule schedule = parseSchedule(schedMap, worldId);

        Map<String, Object> gatesRaw = (Map<String, Object>) r.getOrDefault("gates", Map.of());
        Map<String, ResetConfig.GateConfig> gates = new LinkedHashMap<>();
        for (Map.Entry<String, Object> g : gatesRaw.entrySet()) {
            Map<String, Object> gv = (Map<String, Object>) g.getValue();
            Object value = gv.get("value");
            if (value == null) {
                throw new IllegalArgumentException(
                    "resource-worlds." + worldId + ".reset.gates." + g.getKey() + ".value is required");
            }
            gates.put(g.getKey(), new ResetConfig.GateConfig(
                ((Number) value).doubleValue(),
                (String) gv.getOrDefault("on-fail", "delay")
            ));
        }

        List<Integer> warnings = ((List<Object>) r.getOrDefault("warnings-minutes", List.of()))
            .stream().map(o -> ((Number) o).intValue()).toList();

        return new ResetConfig(
            (String) r.getOrDefault("strategy", "in-place"),
            schedule,
            warnings,
            Map.copyOf(gates),
            (Boolean) r.getOrDefault("pause-autosave", true),
            (Boolean) r.getOrDefault("grace-warning", true)
        );
    }

    private Schedule parseSchedule(Map<String, Object> s, String worldId) {
        String type = ((String) s.getOrDefault("type", "daily")).toLowerCase();
        return switch (type) {
            case "daily" -> Schedule.daily(LocalTime.parse(require(s, "time", worldId)));
            case "weekly" -> Schedule.weekly(
                DayOfWeek.of(((Number) Objects.requireNonNull(s.get("day"),
                    () -> "resource-worlds." + worldId + ".reset.schedule.day is required")).intValue()),
                LocalTime.parse(require(s, "time", worldId))
            );
            case "monthly" -> Schedule.monthly(
                ((Number) Objects.requireNonNull(s.get("day"),
                    () -> "resource-worlds." + worldId + ".reset.schedule.day is required")).intValue(),
                LocalTime.parse(require(s, "time", worldId))
            );
            case "cron" -> Schedule.cron(require(s, "cron", worldId));
            default -> throw new IllegalArgumentException("unknown schedule type: " + type);
        };
    }

    private TeleportConfig parseTeleport(Map<String, Object> t) {
        return new TeleportConfig(
            (String) t.getOrDefault("mode", "spawn"),
            (Boolean) t.getOrDefault("safe-location", true),
            (String) t.getOrDefault("destination-on-reset", "world")
        );
    }

    @SuppressWarnings("unchecked")
    private WorldSettings parseWorldSettings(Map<String, Object> ws) {
        Map<String, Object> rules = (Map<String, Object>) ws.getOrDefault("gamerules", Map.of());
        Map<String, String> gamerules = new LinkedHashMap<>();
        rules.forEach((k, v) -> gamerules.put(k, String.valueOf(v)));

        Optional<WorldSettings.Border> border = Optional.empty();
        Map<String, Object> b = (Map<String, Object>) ws.getOrDefault("border", Map.of());
        if (b.containsKey("size")) {
            List<Object> center = (List<Object>) b.getOrDefault("center", List.of(0, 0));
            border = Optional.of(new WorldSettings.Border(
                ((Number) center.get(0)).doubleValue(),
                ((Number) center.get(1)).doubleValue(),
                ((Number) b.get("size")).doubleValue()
            ));
        }
        return new WorldSettings(
            (String) ws.getOrDefault("difficulty", "NORMAL"),
            Map.copyOf(gamerules),
            border
        );
    }

    @SuppressWarnings("unchecked")
    private ResourceWorld.RegionConfig parseRegions(Map<String, Object> r) {
        List<Map<String, Object>> raw = (List<Map<String, Object>>) r.getOrDefault(
            "list", Collections.<Map<String, Object>>emptyList());
        List<ResourceWorld.RegionConfig.RegionCoord> coords = raw.stream()
            .map(m -> new ResourceWorld.RegionConfig.RegionCoord(
                ((Number) m.get("x")).intValue(),
                ((Number) m.get("z")).intValue()))
            .toList();
        return new ResourceWorld.RegionConfig((Boolean) r.getOrDefault("enabled", false), coords);
    }

    private Map<String, Boolean> mapBooleanLowercase(Map<String, Object> in) {
        Map<String, Boolean> out = new LinkedHashMap<>();
        in.forEach((k, v) -> out.put(k.toLowerCase(Locale.ROOT), (Boolean) v));
        return Map.copyOf(out);
    }
}
