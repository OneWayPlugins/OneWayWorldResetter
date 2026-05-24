package net.onewaycraft.owwr.api.event;

import net.onewaycraft.owwr.api.ResourceWorld.RegionConfig.RegionCoord;
import java.util.List;

public final class RegionPreResetEvent implements ResetEvent {
    private final String worldId;
    private final List<RegionCoord> regions;
    private boolean cancelled;
    public RegionPreResetEvent(String worldId, List<RegionCoord> regions) {
        this.worldId = worldId; this.regions = List.copyOf(regions);
    }
    @Override public String worldId() { return worldId; }
    public List<RegionCoord> regions() { return regions; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean v) { cancelled = v; }
}
