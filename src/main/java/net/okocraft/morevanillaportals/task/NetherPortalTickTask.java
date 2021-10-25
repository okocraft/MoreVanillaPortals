package net.okocraft.morevanillaportals.task;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.okocraft.morevanillaportals.util.PortalTickHolder;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class NetherPortalTickTask implements Runnable {

    private static final String NETHER_SUFFIX = "_nether";
    private static final int NETHER_SUFFIX_LENGTH = NETHER_SUFFIX.length();

    // world_name -> world_name_nether or world_name_nether -> world_name
    private final Map<String, String> worldNameCache = new HashMap<>();

    @Override
    public void run() {
        for (var player : Bukkit.getOnlinePlayers()) {
            processPlayer((CraftPlayer) player);
        }
    }

    public void processPlayer(@NotNull CraftPlayer player) {
        if (!(player.getWorld() instanceof CraftWorld world)) {
            return;
        }

        var resourceKey = world.getHandle().getTypeKey() == DimensionType.NETHER_LOCATION ? Level.OVERWORLD : Level.NETHER;
        var server = world.getHandle().getServer();

        if (server.isNetherEnabled() && server.getLevel(resourceKey) != null) {
            return;
        }

        int warmup = player.isInvulnerable() ? 1 : 80;

        int portalTick = PortalTickHolder.get(player);

        if (player.getHandle().isInsidePortal) {
            if (player.getVehicle() != null) {
                return;
            }

            var worldName = world.getName();
            String distWorldName;

            if (worldNameCache.containsKey(worldName)) {
                distWorldName = worldNameCache.get(worldName);
            } else {
                distWorldName = switch (world.getEnvironment()) {
                    case NORMAL -> worldName + NETHER_SUFFIX;
                    case NETHER -> worldName.endsWith(NETHER_SUFFIX) ?
                            worldName.substring(0, worldName.length() - NETHER_SUFFIX_LENGTH) : null;
                    default -> null;
                };

                if (distWorldName == null || distWorldName.isEmpty()) {
                    return;
                }

                worldNameCache.put(worldName, distWorldName);
            }

            if (!(Bukkit.getWorld(distWorldName) instanceof CraftWorld distWorld)) {
                return;
            }

            portalTick++;

            if (portalTick >= warmup) {
                var profiler = player.getHandle().getLevel().getProfiler();
                profiler.push("portal");
                player.getHandle().setPortalCooldown();
                player.getHandle().changeDimension(distWorld.getHandle(), PlayerTeleportEvent.TeleportCause.NETHER_PORTAL);
                profiler.pop();
            }

            player.getHandle().isInsidePortal = false;
        } else {
            if (portalTick > 0) {
                portalTick -= 4;
            }

            if (portalTick < 0) {
                portalTick = 0;
            }
        }

        PortalTickHolder.set(player, portalTick);
    }
}
