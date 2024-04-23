package net.okocraft.morevanillaportals.listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.okocraft.morevanillaportals.util.WorldNameMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.jetbrains.annotations.NotNull;

abstract class AbstractPortalListener implements Listener {

    protected final WorldNameMap netherMap;
    protected final WorldNameMap endMap;

    protected AbstractPortalListener(boolean useConcurrentMap) {
        this.netherMap = WorldNameMap.nether(useConcurrentMap);
        this.endMap = WorldNameMap.end(useConcurrentMap);
    }

    protected void processEntityPortalReadyEvent(@NotNull EntityPortalReadyEvent event) {
        if (event.getPortalType() != PortalType.NETHER) {
            return;
        }

        var currentWorld = event.getEntity().getWorld();
        var destinationWorldName = this.netherMap.getWorldNameOfPortalDestination(currentWorld);

        if (destinationWorldName == null || destinationWorldName.isEmpty()) {
            return;
        }

        var destinationWorld = Bukkit.getWorld(destinationWorldName);

        if (destinationWorld != null) {
            event.setTargetWorld(destinationWorld);
        }
    }

    protected void processEntityInsideBlockEvent(@NotNull EntityInsideBlockEvent event) {
        if (event.getBlock().getType() != Material.END_PORTAL ||
            event.getEntity().getWorld().getEnvironment() != World.Environment.NORMAL ||
            !(event.getBlock() instanceof CraftBlock block) ||
            !(event.getEntity() instanceof CraftEntity craftEntity) ||
            !(event.getEntity().getWorld() instanceof CraftWorld world)
        ) {
            return;
        }

        if (!this.canTeleportByEndPortal(craftEntity)) { // Prevent scheduling teleport task twice
            event.setCancelled(true);
            return;
        }

        // EndPortalBlock#entityInside
        var entity = craftEntity.getHandle();
        var pos = block.getPosition();

        if (!(entity.canChangeDimensions() && Shapes.joinIsNotEmpty(Shapes.create(entity.getBoundingBox().move((double) (-pos.getX()), (double) (-pos.getY()), (double) (-pos.getZ()))), block.getNMS().getShape(world.getHandle(), pos), BooleanOp.AND))) {
            return;
        }

        var destinationWorldName = this.endMap.getWorldNameOfPortalDestination(world);

        if (destinationWorldName == null || destinationWorldName.isEmpty() ||
            !(Bukkit.getWorld(destinationWorldName) instanceof CraftWorld destination)) {
            return;
        }

        // Entity#tickEndPortal
        new EntityPortalEnterEvent(craftEntity, new Location(world, pos.getX(), pos.getY(), pos.getZ())).callEvent();

        event.setCancelled(true);
        this.teleportByEndPortal(entity, destination.getHandle());
    }

    protected abstract boolean canTeleportByEndPortal(@NotNull Entity entity);

    protected abstract void teleportByEndPortal(@NotNull net.minecraft.world.entity.Entity entity, @NotNull ServerLevel destination);
}
