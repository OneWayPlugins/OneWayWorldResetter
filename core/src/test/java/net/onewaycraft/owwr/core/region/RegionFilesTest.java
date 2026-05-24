package net.onewaycraft.owwr.core.region;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegionFilesTest {

    @Test
    void acceptsCanonicalRegionName() {
        assertThat(RegionFiles.isRegionFile("r.0.0.mca")).isTrue();
        assertThat(RegionFiles.isRegionFile("r.-1.5.mca")).isTrue();
        assertThat(RegionFiles.isRegionFile("r.123.-456.mca")).isTrue();
    }

    @Test
    void rejectsNonRegionNames() {
        assertThat(RegionFiles.isRegionFile("level.dat")).isFalse();
        assertThat(RegionFiles.isRegionFile("r.0.0.mcaX")).isFalse();
        assertThat(RegionFiles.isRegionFile("../etc/passwd")).isFalse();
        assertThat(RegionFiles.isRegionFile("r..0.mca")).isFalse();
        assertThat(RegionFiles.isRegionFile("r.0.0")).isFalse();
    }

    @Test
    void buildsFilenameForCoord() {
        assertThat(RegionFiles.filenameFor(2, -3)).isEqualTo("r.2.-3.mca");
    }
}
