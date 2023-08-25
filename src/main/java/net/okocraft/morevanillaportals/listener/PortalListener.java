package net.okocraft.morevanillaportals.listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.okocraft.morevanillaportals.MoreVanillaPortalsPlugin;
import net.okocraft.morevanillaportals.util.FoliaChecker;
import net.okocraft.morevanillaportals.util.WorldNameMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PortalListener implements Listener {

    private static Enum endPortalType = null;
    private static Method portalToAsync = null;

    static {
        if (FoliaChecker.IS_FOLIA) {
            Method portalToAsync;

            try {
                Class portalType = Class.forName("net.minecraft.world.entity.Entity$PortalType");
                endPortalType = Enum.valueOf(portalType, "END");
                portalToAsync = Entity.class.getDeclaredMethod("portalToAsync", ServerLevel.class, boolean.class, portalType, Consumer.class);
                portalToAsync.setAccessible(true);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }

            PortalListener.portalToAsync = portalToAsync;
        }
    }

    private final WorldNameMap netherMap = WorldNameMap.nether();
    private final WorldNameMap endMap = WorldNameMap.end();
    private final Set<UUID> teleporting = Collections.synchronizedSet(new HashSet<>());

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

        if (teleporting.contains(craftEntity.getUniqueId())) { // Prevent scheduling teleport task twice
            event.setCancelled(true);
            return;
        }

        // EndPortalBlock#entityInside
        var entity = craftEntity.getHandle();
        var pos = block.getPosition();

        if (!(entity.canChangeDimensions() && Shapes.joinIsNotEmpty(Shapes.create(entity.getBoundingBox().move((double) (-pos.getX()), (double) (-pos.getY()), (double) (-pos.getZ()))), block.getNMS().getShape(world.getHandle(), pos), BooleanOp.AND))) {
            return;
        }

        var destinationWorldName = endMap.getWorldNameOfPortalDestination(world);

        if (destinationWorldName == null || destinationWorldName.isEmpty() ||
                !(Bukkit.getWorld(destinationWorldName) instanceof CraftWorld destination)) {
            return;
        }

        // Entity#tickEndPortal
        new EntityPortalEnterEvent(craftEntity, new Location(world, pos.getX(), pos.getY(), pos.getZ())).callEvent();

        event.setCancelled(true);

        if (FoliaChecker.IS_FOLIA) {
            var entityUUID = entity.getUUID();
            teleporting.add(entityUUID);

            // Teleports the entity in the next tick because of world mismatches
            entity.getBukkitEntity()
                    .getScheduler()
                    .run(
                            JavaPlugin.getPlugin(MoreVanillaPortalsPlugin.class),
                            $ -> callPortalToAsync(entity, destination.getUID(), endPortalType),
                            () -> teleporting.remove(entityUUID)
                    );
        } else { // Normal Paper
            // Entity#tickEndPortal
            if (entity instanceof ServerPlayer player) {
                player.changeDimension(destination.getHandle(), PlayerTeleportEvent.TeleportCause.END_PORTAL);
            } else {
                //noinspection ConstantConditions
                entity.teleportTo(destination.getHandle(), null);
            }
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

    // Entity#endPortalLogicAsync
    private void callPortalToAsync(@NotNull Entity entity, @NotNull UUID destinationWorldUuid, @NotNull Enum portalType) {
        teleporting.remove(entity.getUUID());

        if (!entity.canChangeDimensions() || !(Bukkit.getWorld(destinationWorldUuid) instanceof CraftWorld destination)) {
            // wat
            return;
        }

        // Entity#tryEndPortal
        // Entity#endPortalLogicAsync
        try {
            portalToAsync.invoke(entity, destination.getHandle(), false, portalType, null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
