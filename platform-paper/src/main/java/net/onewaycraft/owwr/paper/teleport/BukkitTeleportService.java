package net.onewaycraft.owwr.paper.teleport;

import net.onewaycraft.owwr.core.teleport.PlayerRef;
import net.onewaycraft.owwr.core.teleport.TeleportService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public final class BukkitTeleportService implements TeleportService {

    @Override
    public List<PlayerRef> playersIn(String worldName) {
        World w = Bukkit.getWorld(worldName);
        if (w == null) return List.of();
        return w.getPlayers().stream()
            .map(p -> new PlayerRef(p.getUniqueId(), p.getName()))
            .collect(Collectors.toList());
    }

    @Override
    public int evacuate(List<PlayerRef> players, String destinationWorld) {
        World dest = Bukkit.getWorld(destinationWorld);
        if (dest == null) return 0;
        Location spawn = dest.getSpawnLocation();
        int moved = 0;
        for (PlayerRef ref : players) {
            Player p = Bukkit.getPlayer(ref.uuid());
            if (p != null && p.teleport(spawn)) moved++;
        }
        return moved;
    }

    @Override
    public boolean teleportTo(PlayerRef player, String worldName) {
        World w = Bukkit.getWorld(worldName);
        if (w == null) return false;
        Player p = Bukkit.getPlayer(player.uuid());
        if (p == null) return false;
        return p.teleport(w.getSpawnLocation());
    }
}
