package net.onewaycraft.owwr.core.persistence;

import net.onewaycraft.owwr.api.ResetPhase;
import net.onewaycraft.owwr.api.ResetState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class StateRepositoryTest {

    @Test
    void roundTripsState(@TempDir Path dir) {
        StateRepository repo = new StateRepository(dir);
        ResetState state = new ResetState("mining", ResetPhase.DELETE, 2,
            Instant.parse("2026-05-24T05:00:00Z"), Instant.parse("2026-05-24T05:01:00Z"));
        repo.save(state);
        ResetState loaded = repo.load("mining").orElseThrow();
        assertThat(loaded).isEqualTo(state);
    }

    @Test
    void loadReturnsEmptyWhenAbsent(@TempDir Path dir) {
        assertThat(new StateRepository(dir).load("absent")).isEmpty();
    }

    @Test
    void clearRemovesFile(@TempDir Path dir) {
        StateRepository repo = new StateRepository(dir);
        repo.save(new ResetState("w", ResetPhase.IDLE, 0, Instant.EPOCH, Instant.EPOCH));
        repo.clear("w");
        assertThat(repo.load("w")).isEmpty();
    }
}
