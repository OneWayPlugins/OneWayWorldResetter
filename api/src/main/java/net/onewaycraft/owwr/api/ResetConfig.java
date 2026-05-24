package net.onewaycraft.owwr.api;

import java.util.List;
import java.util.Map;

/**
 * @brief Configuração de reset por mundo.
 */
public record ResetConfig(
    String strategy,               // "in-place" | "double-buffered"
    Schedule schedule,
    List<Integer> warningsMinutes,
    Map<String, GateConfig> gates, // chave: "min-tps" | "max-players" | "min-disk-gb"
    boolean pauseAutosave,
    boolean graceWarning
) {
    /**
     * @brief Threshold + ação em caso de falha do gate.
     * @param value double genérico (tps/jogadores/disco)
     * @param onFail "delay" | "abort" | "force"
     */
    public record GateConfig(double value, String onFail) {}
}
