package net.okocraft.morevanillaportals;

import net.okocraft.morevanillaportals.listener.PortalListener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class MoreVanillaPortalsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PortalListener(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}
