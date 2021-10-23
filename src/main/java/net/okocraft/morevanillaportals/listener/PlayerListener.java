package net.okocraft.morevanillaportals.listener;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.okocraft.morevanillaportals.util.PortalTickHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        PortalTickHolder.remove(event.getPlayer());
    }

    @EventHandler
    public void onPortalEnter(@NotNull EntityPortalEnterEvent event) {
        if (!(event.getEntity() instanceof CraftPlayer player)) {
            return;
        }

        var world = (CraftWorld) player.getWorld();

        if (world.getBlockAt(event.getLocation()).getType() != Material.END_PORTAL) {
            return;
        }

        var resourceKey = world.getHandle().getTypeKey() == DimensionType.END_LOCATION ? Level.OVERWORLD : Level.END;
        var server = world.getHandle().getServer();

        if (Bukkit.getAllowEnd() && server.getLevel(resourceKey) != null) {
            return;
        }

        var distWorldName = switch (world.getEnvironment()) {
            case NORMAL -> world.getName() + "_the_end";
            case THE_END -> {
                var worldName = world.getName();
                if (worldName.endsWith("_the_end")) {
                    yield worldName.substring(0, worldName.length() - 8);
                } else {
                    yield null;
                }
            }
            default -> null;
        };

        if (distWorldName == null || distWorldName.isEmpty()) {
            return;
        }

        var distWorld = Bukkit.getWorld(distWorldName);

        if (distWorld == null) {
            return;
        }

        player.getHandle().changeDimension(((CraftWorld) distWorld).getHandle(), PlayerTeleportEvent.TeleportCause.END_PORTAL);
    }
}
