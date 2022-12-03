package net.okocraft.morevanillaportals.listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.okocraft.morevanillaportals.util.WorldNameMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PortalListener implements Listener {

    private final WorldNameMap netherMap = WorldNameMap.nether();
    private final WorldNameMap endMap = WorldNameMap.end();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void handleInsideEndPortal(@NotNull EntityInsideBlockEvent event) {
        var block = event.getBlock();
        var world = event.getEntity().getWorld();
        if (block.getType() != Material.END_PORTAL || world.getEnvironment() != World.Environment.NORMAL) {
            // EndPortalBlock#entityInside - CraftWorld#getHandle -> ServerLevel
            return;
        }

        var level = getLevel(world.getKey());
        if (level == null) {
            return;
        }

        // EndPortalBlock#entityInside
        var entity = level.getEntity(event.getEntity().getUniqueId());
        if (entity == null) {
            return;
        }
        var pos = new BlockPos(block.getLocation().getX(), block.getLocation().getY(), block.getLocation().getZ());
        if (!entity.isPassenger() &&
                !entity.isVehicle() &&
                entity.canChangeDimensions() &&
                Shapes.joinIsNotEmpty(Shapes.create(entity.getBoundingBox().move((double) (-pos.getX()), (double) (-pos.getY()), (double) (-pos.getZ()))), level.getBlockState(pos).getShape(level, pos), BooleanOp.AND)) {
            var destinationWorldName = endMap.getWorldNameOfPortalDestination(world);
            if (destinationWorldName == null || destinationWorldName.isEmpty()) {
                return;
            }
            var destinationWorld = getLevel(destinationWorldName);
            if (destinationWorld == null) {
                return;
            }

            if (entity instanceof ServerPlayer player) {
                player.changeDimension(destinationWorld, PlayerTeleportEvent.TeleportCause.END_PORTAL);
            } else {
                //noinspection ConstantConditions
                entity.teleportTo(destinationWorld, null);
            }

            event.setCancelled(true);
        }
    }

    private @Nullable ServerLevel getLevel(@NotNull NamespacedKey key) {
        return MinecraftServer.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(key.getNamespace(), key.getKey())));
    }

    private @Nullable ServerLevel getLevel(@Nullable String levelName) {
        return StreamSupport.stream(MinecraftServer.getServer().getAllLevels().spliterator(), false)
                .filter(l -> l.serverLevelData.getLevelName().equals(levelName))
                .findFirst()
                .orElse(null);
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
