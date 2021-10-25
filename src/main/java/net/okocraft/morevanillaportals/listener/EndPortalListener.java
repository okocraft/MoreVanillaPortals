package net.okocraft.morevanillaportals.listener;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.okocraft.morevanillaportals.util.WorldNameMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class EndPortalListener implements Listener {

    private final WorldNameMap worldNameMap = WorldNameMap.end();

    @EventHandler
    public void onEnter(@NotNull EntityPortalEnterEvent event) {
        if (!(event.getEntity() instanceof CraftPlayer player) ||
                !(player.getWorld() instanceof CraftWorld world)) {
            return;
        }

        if (world.getBlockAt(event.getLocation()).getType() != Material.END_PORTAL) {
            return;
        }

        var resourceKey = world.getHandle().getTypeKey() == DimensionType.END_LOCATION ? Level.OVERWORLD : Level.END;
        var server = world.getHandle().getServer();

        if (Bukkit.getAllowEnd() && server.getLevel(resourceKey) != null) {
            return;
        }

        var destinationWorldName = worldNameMap.getWorldNameOfPortalDestination(world);

        if (destinationWorldName == null || destinationWorldName.isEmpty() ||
                !(Bukkit.getWorld(destinationWorldName) instanceof CraftWorld destination)) {
            return;
        }

        player.getHandle().changeDimension(destination.getHandle(), PlayerTeleportEvent.TeleportCause.END_PORTAL);
    }
}
