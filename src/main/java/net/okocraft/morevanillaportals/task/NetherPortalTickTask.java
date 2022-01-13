package net.okocraft.morevanillaportals.task;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.okocraft.morevanillaportals.util.PortalTickHolder;
import net.okocraft.morevanillaportals.util.WorldNameMap;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class NetherPortalTickTask implements Runnable {

    private final WorldNameMap worldNameMap = WorldNameMap.nether();

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

            var destinationWorldName = worldNameMap.getWorldNameOfPortalDestination(world);

            if (destinationWorldName == null || destinationWorldName.isEmpty() ||
                    !(Bukkit.getWorld(destinationWorldName) instanceof CraftWorld destination)) {
                return;
            }

            portalTick++;

            if (portalTick >= warmup) {
                var profiler = player.getHandle().getLevel().getProfiler();
                profiler.push("portal");
                player.getHandle().setPortalCooldown();
                player.getHandle().changeDimension(destination.getHandle(), PlayerTeleportEvent.TeleportCause.NETHER_PORTAL);
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
