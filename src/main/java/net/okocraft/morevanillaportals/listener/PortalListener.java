package net.okocraft.morevanillaportals.listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.okocraft.morevanillaportals.util.WorldNameMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class PortalListener implements Listener {

    private final WorldNameMap netherMap = WorldNameMap.nether();
    private final WorldNameMap endMap = WorldNameMap.end();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void handleInsideEndPortal(@NotNull EntityInsideBlockEvent event) {
        if (event.getBlock().getType() != Material.END_PORTAL ||
                event.getEntity().getWorld().getEnvironment() != World.Environment.NORMAL ||
                !(event.getBlock() instanceof CraftBlock block) ||
                !(event.getEntity() instanceof CraftEntity craftEntity) ||
                !(event.getEntity().getWorld() instanceof CraftWorld world)
        ) {
            return;
        }

        // EndPortalBlock#entityInside
        var entity = craftEntity.getHandle();
        var pos = block.getPosition();
        if (!entity.isPassenger() &&
                !entity.isVehicle() &&
                entity.canChangeDimensions() &&
                Shapes.joinIsNotEmpty(Shapes.create(entity.getBoundingBox().move((double) (-pos.getX()), (double) (-pos.getY()), (double) (-pos.getZ()))), block.getNMS().getShape(world.getHandle(), pos), BooleanOp.AND)) {
            var destinationWorldName = endMap.getWorldNameOfPortalDestination(world);

            if (destinationWorldName == null || destinationWorldName.isEmpty() ||
                    !(Bukkit.getWorld(destinationWorldName) instanceof CraftWorld destination)) {
                return;
            }

            if (entity instanceof ServerPlayer player) {
                player.changeDimension(destination.getHandle(), PlayerTeleportEvent.TeleportCause.END_PORTAL);
            } else {
                //noinspection ConstantConditions
                entity.teleportTo(destination.getHandle(), null);
            }

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNetherPortalReady(@NotNull EntityPortalReadyEvent event) {
        var currentWorld = event.getEntity().getWorld();
        var destinationWorldName = netherMap.getWorldNameOfPortalDestination(currentWorld);

        if (destinationWorldName == null || destinationWorldName.isEmpty()) {
            return;
        }

        var destinationWorld = Bukkit.getWorld(destinationWorldName);

        if (destinationWorld != null) {
            event.setTargetWorld(destinationWorld);
        }
    }
}
