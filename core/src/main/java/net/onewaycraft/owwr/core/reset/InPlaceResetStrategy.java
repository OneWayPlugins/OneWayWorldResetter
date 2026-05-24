package net.onewaycraft.owwr.core.reset;

import net.onewaycraft.owwr.api.*;
import net.onewaycraft.owwr.core.preflight.ResetContext;
import net.onewaycraft.owwr.core.teleport.PlayerRef;
import net.onewaycraft.owwr.core.teleport.TeleportService;
import net.onewaycraft.owwr.core.world.WorldLifecycleService;
import net.onewaycraft.owwr.core.world.WorldOpResult;
import net.onewaycraft.owwr.core.world.WorldSpec;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @brief Reset in-place: evacua, unload, delete, recreate, verify.
 */
public final class InPlaceResetStrategy implements ResetStrategy {

    private final WorldLifecycleService worlds;
    private final TeleportService teleports;
    private final SeedPicker seedPicker;

    public InPlaceResetStrategy(WorldLifecycleService worlds, TeleportService teleports,
                                SeedPicker seedPicker) {
        this.worlds = worlds;
        this.teleports = teleports;
        this.seedPicker = seedPicker;
    }

    @Override public String id() { return "in-place"; }

    @Override
    public ResetResult execute(ResetContext ctx, PhaseExecutor exec) {
        ResourceWorld rw = ctx.world();
        Instant start = Instant.now();

        List<PlayerRef> players = teleports.playersIn(rw.worldName());
        int affected = players.size();

        if (ctx.dryRun()) {
            return new ResetResult(rw.id(), true, ResetPhase.COMPLETE,
                Duration.between(start, Instant.now()),
                exec.phaseDurations(), affected, null);
        }

        try {
            exec.run(ResetPhase.TELEPORT, () ->
                teleports.evacuate(players, rw.teleport().destinationOnReset()) >= 0);
            exec.run(ResetPhase.UNLOAD,
                () -> worlds.unloadWorld(rw.worldName()) instanceof WorldOpResult.Ok);
            exec.run(ResetPhase.DELETE,
                () -> worlds.deleteWorld(rw.worldName()) instanceof WorldOpResult.Ok);
            exec.run(ResetPhase.RECREATE, () -> {
                long seed = seedPicker.pickSeed(rw.seed());
                return worlds.createWorld(new WorldSpec(rw.worldName(), rw.environment(), seed))
                    instanceof WorldOpResult.Ok;
            });
            exec.run(ResetPhase.VERIFY, () -> worlds.worldExists(rw.worldName()));

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
