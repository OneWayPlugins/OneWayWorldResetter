package net.onewaycraft.owwr.core.world;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PathSafetyTest {

    @Test
    void acceptsPathInsideContainer(@TempDir Path container) throws IOException {
        Path world = Files.createDirectory(container.resolve("world_mining"));
        assertThat(PathSafety.isUnder(world, container)).isTrue();
    }

    @Test
    void rejectsPathOutsideContainer(@TempDir Path container, @TempDir Path other) {
        assertThat(PathSafety.isUnder(other, container)).isFalse();
    }

    @Test
    void rejectsTraversalDoubleDot(@TempDir Path container) {
        Path malicious = container.resolve("..").resolve("etc");
        assertThat(PathSafety.isUnder(malicious, container)).isFalse();
    }

    @Test
    void rejectsContainerItself(@TempDir Path container) {
        assertThat(PathSafety.isUnder(container, container)).isFalse();
    }
}
