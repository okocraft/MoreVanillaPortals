package net.okocraft.morevanillaportals.listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class FoliaPortalListener extends AbstractPortalListener {

    private static final Enum<?> END_PORTAL_TYPE;
    private static final Method PORTAL_TO_ASYNC_METHOD;

    static {
        try {
            Class<?> portalType = Class.forName("net.minecraft.world.entity.Entity$PortalType");
            END_PORTAL_TYPE = getEndPortalType(portalType);
            PORTAL_TO_ASYNC_METHOD = getPortalToAsyncMethod(portalType);
            PORTAL_TO_ASYNC_METHOD.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static @NotNull Enum getEndPortalType(Class portalType) {
        return Enum.valueOf(portalType, "END");
    }

    @SuppressWarnings({"JavaReflectionMemberAccess", "rawtypes"})
    private static @NotNull Method getPortalToAsyncMethod(Class portalType) throws NoSuchMethodException {
        return Entity.class.getDeclaredMethod("portalToAsync", ServerLevel.class, boolean.class, portalType, Consumer.class);
    }

    private final Plugin plugin;

    public FoliaPortalListener(@NotNull Plugin plugin) {
        super(plugin, true);
        this.plugin = plugin;
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
        // Entity#tryEndPortal
        // Entity#endPortalLogicAsync
        try {
            PORTAL_TO_ASYNC_METHOD.invoke(entity, destination, false, END_PORTAL_TYPE, null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            this.plugin.getSLF4JLogger().error("Failed to teleport entity (type: {}, uuid: {}) by the end portal.", entity.getType(), entity.getUUID());
        }
    }
}
