package net.okocraft.morevanillaportals.listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.okocraft.morevanillaportals.util.WorldNameMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class EndPortalListener implements Listener {

    private final WorldNameMap endMap = WorldNameMap.end();

    @EventHandler(priority = EventPriority.HIGH)
    public void onEvent(@NotNull EntityInsideBlockEvent event) {
        if (event.isCancelled() ||
                !(event.getEntity() instanceof CraftPlayer player) ||
                !(event.getBlock().getWorld() instanceof CraftWorld world)
        ) {
            return;
        }

        if (event.getBlock().getType() == Material.END_PORTAL) {
            var resourceKey = world.getHandle().getTypeKey() == LevelStem.END ? Level.OVERWORLD : Level.END;
            var server = world.getHandle().getServer();

            if (Bukkit.getAllowEnd() && server.getLevel(resourceKey) != null) {
                return;
            }

            var destinationWorldName = endMap.getWorldNameOfPortalDestination(world);

            if (destinationWorldName == null || destinationWorldName.isEmpty() ||
                    !(Bukkit.getWorld(destinationWorldName) instanceof CraftWorld destination)) {
                return;
            }

            player.getHandle().changeDimension(destination.getHandle(), PlayerTeleportEvent.TeleportCause.END_PORTAL);
            event.setCancelled(true);
        }
    }
}
