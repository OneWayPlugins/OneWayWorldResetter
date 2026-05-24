package net.onewaycraft.owwr.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @brief Configuração de reset por mundo.
 *
 * O campo {@code chunky} é opcional: quando null, a fase PREGEN é skipada.
 */
public record ResetConfig(
    String strategy,
    Schedule schedule,
    List<Integer> warningsMinutes,
    Map<String, GateConfig> gates,
    boolean pauseAutosave,
    boolean graceWarning,
    ChunkyConfig chunky                  // nullable
) {
    public ResetConfig {
        Objects.requireNonNull(strategy, "strategy");
        Objects.requireNonNull(schedule, "schedule");
        warningsMinutes = List.copyOf(warningsMinutes);
        gates = Map.copyOf(gates);
        // chunky may be null — opt-in feature
    }

    /**
     * @brief Threshold + ação em caso de falha do gate.
     * @param value double genérico (tps/jogadores/disco)
     * @param onFail "delay" | "abort" | "force"
     */
    public record GateConfig(double value, String onFail) {
        public GateConfig {
            Objects.requireNonNull(onFail, "onFail");
        }
    }
}
