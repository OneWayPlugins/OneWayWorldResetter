package net.onewaycraft.owwr.api;

/**
 * @brief Resultado de um PreflightGate. PASS prossegue; DELAY reagenda; ABORT cancela.
 */
public sealed interface GateResult permits GateResult.Pass, GateResult.Delay, GateResult.Abort {

    static GateResult pass() { return Pass.INSTANCE; }
    static GateResult delay(String reason) { return new Delay(reason); }
    static GateResult abort(String reason) { return new Abort(reason); }

    enum Pass implements GateResult { INSTANCE }

    record Delay(String reason) implements GateResult {}
    record Abort(String reason) implements GateResult {}
}
