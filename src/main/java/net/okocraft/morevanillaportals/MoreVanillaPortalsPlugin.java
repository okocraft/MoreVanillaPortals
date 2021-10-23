package net.okocraft.morevanillaportals;

import net.okocraft.morevanillaportals.listener.PlayerListener;
import net.okocraft.morevanillaportals.task.NetherPortalTickTask;
import net.okocraft.morevanillaportals.util.PortalTickHolder;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class MoreVanillaPortalsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getScheduler().runTaskTimer(this, new NetherPortalTickTask(), 1, 1);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
        PortalTickHolder.clear();
    }
}
