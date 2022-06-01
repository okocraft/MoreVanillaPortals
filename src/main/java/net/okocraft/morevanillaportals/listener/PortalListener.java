package net.okocraft.morevanillaportals.listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import net.okocraft.morevanillaportals.util.WorldNameMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PortalListener implements Listener {

    private final WorldNameMap netherMap = WorldNameMap.nether();
    private final WorldNameMap endMap = WorldNameMap.end();
    private final Map<Player, AtomicInteger> lastTickMap = new HashMap<>();
    private final Map<Player, AtomicInteger> portalTimeMap = new HashMap<>();

    @EventHandler
    public void clearCache(@NotNull PlayerQuitEvent event) {
        lastTickMap.remove(event.getPlayer());
        portalTimeMap.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void handleInsideBlockEvent(@NotNull EntityInsideBlockEvent event) {
        var entity = event.getEntity();

        // org.bukkit.entity.Entity#isEmpty = Entity#getPassengers.isEmpty
        if (entity.isDead() || !entity.isValid() ||
                entity.getVehicle() != null || !entity.isEmpty() ||
                !(entity instanceof CraftPlayer player)) {
            return;
        }

        if (!isPortal(event.getBlock()) || !(event.getBlock() instanceof CraftBlock block)) {
            return;
        }

        player.getHandle().handleInsidePortal(block.getPosition());

        if (player.getHandle().isInsidePortal) {
            player.getHandle().isInsidePortal = false;
        } else {
            return;
        }

        var world = block.getWorld();

        if (block.getType() == Material.NETHER_PORTAL && isOverworldOrNether(world.getEnvironment())) {
            if (handleNetherPortal(player)) {
                event.setCancelled(true);
            }
            return;
        }

        if (block.getType() == Material.END_PORTAL && isOverworldOrEnd(world.getEnvironment())) {
            if (handleEndPortal(player)) {
                event.setCancelled(true);
            }
        }
    }

    public boolean handleNetherPortal(@NotNull CraftPlayer player) {
        var destinationWorldName = netherMap.getWorldNameOfPortalDestination(player.getWorld());

        if (destinationWorldName == null || destinationWorldName.isEmpty() ||
                !(Bukkit.getWorld(destinationWorldName) instanceof CraftWorld destination)) {
            return false;
        }

        var lastTick = lastTickMap.computeIfAbsent(player, p -> new AtomicInteger(-1));
        var portalTime = portalTimeMap.computeIfAbsent(player, p -> new AtomicInteger(0));

        int elapsed = Bukkit.getCurrentTick() - lastTick.get();

        // prevent multiple counts on the same tick
        if (elapsed == 0) {
            return true;
        }

        if (lastTick.get() != -1 && 1 < elapsed) {
            // nms - Entity#handleNetherPortal
            if (portalTime.get() > 0) {
                portalTime.addAndGet(elapsed * -4);
            }

            if (portalTime.get() < 0) {
                portalTime.set(0);
            }
        }

        int waitTime = player.isInvulnerable() ? 1 : 80; // Entity#getPortalWaitTime

        if (waitTime <= portalTime.incrementAndGet()) {
            player.setPortalCooldown(player.getHandle().getDimensionChangingDelay()); // Entity#setPortalCooldown
            player.getHandle().changeDimension(destination.getHandle(), PlayerTeleportEvent.TeleportCause.NETHER_PORTAL);
        }

        lastTick.set(Bukkit.getCurrentTick());

        return true;
    }

    public boolean handleEndPortal(@NotNull CraftPlayer player) {
        var destinationWorldName = endMap.getWorldNameOfPortalDestination(player.getWorld());

        if (destinationWorldName == null || destinationWorldName.isEmpty() ||
                !(Bukkit.getWorld(destinationWorldName) instanceof CraftWorld destination)) {
            return false;
        }

        // Entity#tickEndPortal
        player.getHandle().changeDimension(destination.getHandle(), PlayerTeleportEvent.TeleportCause.END_PORTAL);
        return true;
    }

    private boolean isPortal(@NotNull Block block) {
        return block.getType() == Material.NETHER_PORTAL || block.getType() == Material.END_PORTAL;
    }

    private boolean isOverworldOrNether(@NotNull World.Environment environment) {
        return environment == World.Environment.NORMAL || environment == World.Environment.NETHER;
    }

    private boolean isOverworldOrEnd(@NotNull World.Environment environment) {
        return environment == World.Environment.NORMAL || environment == World.Environment.THE_END;
    }
}
