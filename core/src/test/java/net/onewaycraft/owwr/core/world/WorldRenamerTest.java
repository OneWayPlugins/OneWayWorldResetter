package net.onewaycraft.owwr.core.world;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class WorldRenamerTest {

    @Test
    void swapsActiveDirWithStagingPreservingActiveName(@TempDir Path container) throws IOException {
        Path active = Files.createDirectory(container.resolve("world_resource"));
        Files.writeString(active.resolve("marker"), "old");
        Path staging = Files.createDirectory(container.resolve("world_resource__staging"));
        Files.writeString(staging.resolve("marker"), "new");

        new WorldRenamer().swap(active, staging);

        assertThat(active).exists();
        assertThat(staging).doesNotExist();
        assertThat(Files.readString(active.resolve("marker"))).isEqualTo("new");
    }

    @Test
    void rollsBackWhenStagingIsMissing(@TempDir Path container) throws IOException {
        Path active = Files.createDirectory(container.resolve("world_resource"));
        Files.writeString(active.resolve("marker"), "old");
        Path staging = container.resolve("missing");
        try {
            new WorldRenamer().swap(active, staging);
        } catch (IOException expected) {
            assertThat(Files.readString(active.resolve("marker"))).isEqualTo("old");
        }
    }
}
