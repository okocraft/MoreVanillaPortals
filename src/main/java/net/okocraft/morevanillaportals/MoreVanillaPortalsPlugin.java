package net.okocraft.morevanillaportals;

import net.okocraft.morevanillaportals.listener.FoliaPortalListener;
import net.okocraft.morevanillaportals.listener.PaperPortalListener;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class MoreVanillaPortalsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this.createPortalListener(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    private @NotNull Listener createPortalListener() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            this.getSLF4JLogger().info("You may need to patch the server to call EntityPortalReadyEvent.");
            this.getSLF4JLogger().info("In normal Folia, nether portals will not be working by this plugin.");
            return new FoliaPortalListener(this);
        } catch (ClassNotFoundException e) {
            return new PaperPortalListener();
        }
    }
}
