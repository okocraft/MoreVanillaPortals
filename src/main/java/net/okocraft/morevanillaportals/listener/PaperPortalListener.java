package net.okocraft.morevanillaportals.listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PaperPortalListener extends AbstractPortalListener {

    public PaperPortalListener(@NotNull Plugin plugin) {
        super(plugin, false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortalReadyEvent(@NotNull EntityPortalReadyEvent event) {
        this.processEntityPortalReadyEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInsideBlockEvent(@NotNull EntityInsideBlockEvent event) {
        this.processEntityInsideBlockEvent(event);
    }

    @Override
    protected void teleportByEndPortal(@NotNull Entity entity, @NotNull ServerLevel destination) {
        if (entity instanceof ServerPlayer player) {
            player.changeDimension(destination, PlayerTeleportEvent.TeleportCause.END_PORTAL);
        } else {
            //noinspection ConstantConditions
            entity.teleportTo(destination, null);
        }
    }
}
