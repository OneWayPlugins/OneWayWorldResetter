package net.onewaycraft.owwr.core.reset;

import net.onewaycraft.owwr.api.*;
import net.onewaycraft.owwr.core.preflight.ResetContext;
import net.onewaycraft.owwr.core.teleport.PlayerRef;
import net.onewaycraft.owwr.core.teleport.TeleportService;
import net.onewaycraft.owwr.core.world.WorldLifecycleService;
import net.onewaycraft.owwr.core.world.WorldOpResult;
import net.onewaycraft.owwr.core.world.WorldRenamer;
import net.onewaycraft.owwr.core.world.WorldSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @brief Gera staging em background, evacua, swap rápido, recarrega.
 *
 * Premissa de disco: gate min-disk-gb deve refletir 2x tamanho do mundo.
 */
public final class DoubleBufferedResetStrategy implements ResetStrategy {

    private final WorldLifecycleService worlds;
    private final TeleportService teleports;
    private final SeedPicker seedPicker;
    private final WorldRenamer renamer;
    private final Path worldContainer;

    public DoubleBufferedResetStrategy(WorldLifecycleService worlds, TeleportService teleports,
                                       SeedPicker seedPicker, WorldRenamer renamer,
                                       Path worldContainer) {
        this.worlds = worlds;
        this.teleports = teleports;
        this.seedPicker = seedPicker;
        this.renamer = renamer;
        this.worldContainer = worldContainer;
    }

    @Override public String id() { return "double-buffered"; }

    @Override
    public ResetResult execute(ResetContext ctx, PhaseExecutor exec) {
        ResourceWorld rw = ctx.world();
        Instant start = Instant.now();
        String stagingName = rw.worldName() + "__staging";

        List<PlayerRef> players = teleports.playersIn(rw.worldName());
        int affected = players.size();

        if (ctx.dryRun()) {
            return new ResetResult(rw.id(), true, ResetPhase.COMPLETE,
                Duration.between(start, Instant.now()),
                exec.phaseDurations(), affected, null);
        }

        try {
            exec.run(ResetPhase.RECREATE, () -> {
                long seed = seedPicker.pickSeed(rw.seed());
                WorldOpResult r = worlds.createWorld(new WorldSpec(stagingName, rw.environment(), seed));
                if (!(r instanceof WorldOpResult.Ok)) return false;
                return worlds.unloadWorld(stagingName) instanceof WorldOpResult.Ok;
            });

            exec.run(ResetPhase.TELEPORT,
                () -> teleports.evacuate(players, rw.teleport().destinationOnReset()) >= 0);

            exec.run(ResetPhase.UNLOAD,
                () -> worlds.unloadWorld(rw.worldName()) instanceof WorldOpResult.Ok);

            exec.run(ResetPhase.DELETE, () -> {
                try {
                    renamer.swap(
                        worldContainer.resolve(rw.worldName()),
                        worldContainer.resolve(stagingName));
                    return true;
                } catch (IOException e) {
                    return false;
                }
            });

            exec.run(ResetPhase.VERIFY, () -> {
                WorldOpResult r = worlds.createWorld(new WorldSpec(rw.worldName(), rw.environment(), 0L));
                return r instanceof WorldOpResult.Ok;
            });

            return new ResetResult(rw.id(), true, ResetPhase.COMPLETE,
                Duration.between(start, Instant.now()),
                exec.phaseDurations(), affected, null);
        } catch (PhaseExecutor.PhaseFailure f) {
            return new ResetResult(rw.id(), false, f.phase(),
                Duration.between(start, Instant.now()),
                exec.phaseDurations(), affected, f.getMessage());
        }
    }
}
