package net.okocraft.morevanillaportals.listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class PaperPortalListener extends AbstractPortalListener {

    public PaperPortalListener() {
        super(false);
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
    protected boolean canTeleportByEndPortal(@NotNull Entity entity) {
        return true;
    }

    @Override
    protected void teleportByEndPortal(@NotNull net.minecraft.world.entity.Entity entity, @NotNull ServerLevel destination) {
        if (entity instanceof ServerPlayer player) {
            player.changeDimension(destination, PlayerTeleportEvent.TeleportCause.END_PORTAL);
        } else {
            //noinspection ConstantConditions
            entity.teleportTo(destination, null);
        }
    }
}
