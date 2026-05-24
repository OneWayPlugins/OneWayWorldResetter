package net.onewaycraft.owwr.core.reset;

import net.onewaycraft.owwr.api.SeedConfig;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @brief Calcula próxima seed conforme estratégia.
 */
public final class SeedPicker {

    private final AtomicInteger cyclingIndex = new AtomicInteger();

    public long pickSeed(SeedConfig cfg) {
        return switch (cfg.strategy()) {
            case RANDOM -> ThreadLocalRandom.current().nextLong();
            case FIXED -> cfg.values().isEmpty() ? 0L : cfg.values().get(0);
            case CYCLING -> {
                if (cfg.values().isEmpty()) yield ThreadLocalRandom.current().nextLong();
                int idx = cyclingIndex.getAndIncrement() % cfg.values().size();
                yield cfg.values().get(idx);
            }
        };
    }
}
