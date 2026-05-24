package net.onewaycraft.owwr.core.persistence;

import net.onewaycraft.owwr.api.ResetPhase;
import net.onewaycraft.owwr.api.ResetRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryRepositoryTest {

    @Test
    void appendAndRecentReturnsMostRecent(@TempDir Path dir) {
        HistoryRepository repo = new HistoryRepository(dir, 10);
        for (int i = 0; i < 5; i++) {
            repo.append(new ResetRecord("w", Instant.EPOCH.plusSeconds(i),
                Duration.ofSeconds(10), true, ResetPhase.COMPLETE, i, null));
        }
        assertThat(repo.recent(3)).hasSize(3);
    }

    @Test
    void trimsToMaxEntries(@TempDir Path dir) {
        HistoryRepository repo = new HistoryRepository(dir, 3);
        for (int i = 0; i < 5; i++) {
            repo.append(new ResetRecord("w", Instant.EPOCH.plusSeconds(i),
                Duration.ZERO, true, ResetPhase.COMPLETE, 0, null));
        }
        assertThat(repo.recent(10)).hasSize(3);
    }
}
