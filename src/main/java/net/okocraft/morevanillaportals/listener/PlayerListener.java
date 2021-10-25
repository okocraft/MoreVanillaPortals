package net.okocraft.morevanillaportals.listener;

import net.okocraft.morevanillaportals.util.PortalTickHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        PortalTickHolder.remove(event.getPlayer());
    }

}
