package net.onewaycraft.owwr.core.region;

import net.onewaycraft.owwr.api.ResourceWorld.RegionConfig.RegionCoord;
import net.onewaycraft.owwr.api.event.RegionPostResetEvent;
import net.onewaycraft.owwr.api.event.RegionPreResetEvent;
import net.onewaycraft.owwr.core.reset.EventBus;
import net.onewaycraft.owwr.core.schedule.Scheduler;
import net.onewaycraft.owwr.core.world.PathSafety;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * @brief Apaga arquivos de região listados em lotes (1 região por tick) preservando TPS.
 *
 * Dirs alvo: region/, poi/, entities/.
 */
public final class RegionResetManager {

    private static final List<String> SUBDIRS = List.of("region", "poi", "entities");

    private final Path worldContainer;
    private final Scheduler scheduler;
    private final EventBus events;
    private final Logger logger;

    public RegionResetManager(Path worldContainer, Scheduler scheduler, EventBus events, Logger logger) {
        this.worldContainer = worldContainer;
        this.scheduler = scheduler;
        this.events = events;
        this.logger = logger;
    }

    public void resetRegions(String worldId, String worldName, List<RegionCoord> regions) {
        RegionPreResetEvent pre = new RegionPreResetEvent(worldId, regions);
        if (events.fire(pre)) {
            logger.info("RegionPreResetEvent cancelled");
            return;
        }
        Path worldDir = worldContainer.resolve(worldName);
        Iterator<RegionCoord> it = regions.iterator();
        AtomicReference<Scheduler.Cancellable> handle = new AtomicReference<>();
        handle.set(scheduler.runRepeatingGlobal(Duration.ZERO, Duration.ofMillis(50), () -> {
            if (!it.hasNext()) {
                Scheduler.Cancellable c = handle.get();
                if (c != null) c.cancel();
                events.fire(new RegionPostResetEvent(worldId, regions, true));
                return;
            }
            RegionCoord c = it.next();
            String filename = RegionFiles.filenameFor(c.x(), c.z());
            for (String sub : SUBDIRS) {
                Path target = worldDir.resolve(sub).resolve(filename);
                if (!Files.exists(target)) continue;
                if (!RegionFiles.isRegionFile(target.getFileName().toString())) continue;
                if (!PathSafety.isUnder(target, worldContainer)) continue;
                try { Files.delete(target); }
                catch (IOException e) { logger.warning("region delete fail: " + e); }
            }
        }));
    }
}
