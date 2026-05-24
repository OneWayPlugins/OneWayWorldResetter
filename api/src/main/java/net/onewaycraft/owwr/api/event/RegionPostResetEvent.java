package net.onewaycraft.owwr.api.event;

import net.onewaycraft.owwr.api.ResourceWorld.RegionConfig.RegionCoord;
import java.util.List;

public final class RegionPostResetEvent implements ResetEvent {
    private final String worldId;
    private final List<RegionCoord> regions;
    private final boolean success;
    public RegionPostResetEvent(String worldId, List<RegionCoord> regions, boolean success) {
        this.worldId = worldId;
        this.regions = List.copyOf(regions);
        this.success = success;
    }
    @Override public String worldId() { return worldId; }
    public List<RegionCoord> regions() { return regions; }
    public boolean success() { return success; }
}
