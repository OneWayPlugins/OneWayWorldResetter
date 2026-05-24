package net.onewaycraft.owwr.core.world;

/**
 * @brief Resultado tipado de operações de mundo (sucesso/erro + razão).
 */
public sealed interface WorldOpResult permits WorldOpResult.Ok, WorldOpResult.Fail {

    static WorldOpResult ok() { return Ok.INSTANCE; }
    static WorldOpResult fail(String reason) { return new Fail(reason); }

    enum Ok implements WorldOpResult { INSTANCE }
    record Fail(String reason) implements WorldOpResult {}
}
