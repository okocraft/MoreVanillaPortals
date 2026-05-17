package net.okocraft.morevanillaportals;

import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.key.Key;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MoreVanillaPortalsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this.createPortalListener(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    private Listener createPortalListener() {
        if (!ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("okocraft", "folia"))) {
            this.getSLF4JLogger().warn("You may need to patch the server to call EntityPortalReadyEvent for end portals.");
            this.getSLF4JLogger().warn("Without patching, End portals will not be working by this plugin.");
        }

        return new PortalListener();
    }
}
