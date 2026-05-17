package net.okocraft.morevanillaportals;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NullMarked;

@NullMarked
class PortalListener implements Listener {

    private final WorldPairs netherMap = WorldPairs.nether();
    private final WorldPairs endMap = WorldPairs.end();

    @EventHandler
    public void onPortalReady(EntityPortalReadyEvent event) {
        WorldPairs map;
        switch (event.getPortalType()) {
            case NETHER -> map = this.netherMap;
            case ENDER -> map = this.endMap;
            default -> {
                return;
            }
        }

        Key key = map.getPairWorldKey(event.getEntity().getWorld());
        if (key == null) {
            return;
        }

        World destinationWorld = Bukkit.getWorld(key);
        if (destinationWorld != null) {
            event.setTargetWorld(destinationWorld);
        }
    }
}
