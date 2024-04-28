package net.okocraft.morevanillaportals.listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.okocraft.morevanillaportals.util.WorldNameMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

abstract class AbstractPortalListener implements Listener {

    private final Plugin plugin;
    private final Set<UUID> teleportingByEndPortal;
    protected final WorldNameMap netherMap;
    protected final WorldNameMap endMap;

    protected AbstractPortalListener(@NotNull Plugin plugin, boolean useConcurrentMap) {
        this.plugin = plugin;
        this.teleportingByEndPortal = useConcurrentMap ? ConcurrentHashMap.newKeySet() : new HashSet<>();
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

        event.setCancelled(true);
        var entityUUID = entity.getUUID();

        if (this.teleportingByEndPortal.add(entityUUID)) {
            var worldUid = destination.getUID();
            new EntityPortalEnterEvent(craftEntity, new Location(world, pos.getX(), pos.getY(), pos.getZ())).callEvent();
            entity.getBukkitEntity().getScheduler().run(
                    this.plugin,
                    $ -> this.teleportByEndPortal(entity, worldUid),
                    () -> this.teleportingByEndPortal.remove(entityUUID)
            );
        }
    }

    protected abstract void teleportByEndPortal(@NotNull Entity entity, @NotNull ServerLevel destination);

    private void teleportByEndPortal(@NotNull Entity entity, @NotNull UUID destinationWorldUuid) {
        if (this.teleportingByEndPortal.remove(entity.getUUID()) && entity.canChangeDimensions() && Bukkit.getWorld(destinationWorldUuid) instanceof CraftWorld destination) {
            this.teleportByEndPortal(entity, destination.getHandle());
        }
    }
}
