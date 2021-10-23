package net.okocraft.morevanillaportals.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public final class PortalTickHolder {

    public static final Map<Player, AtomicInteger> PORTAL_TICK_MAP = new HashMap<>();

    public static int get(@NotNull Player player) {
        return Optional.ofNullable(PORTAL_TICK_MAP.get(player)).map(AtomicInteger::get).orElse(0);
    }

    public static void set(@NotNull Player player, int tick) {
       PORTAL_TICK_MAP.computeIfAbsent(player, p -> new AtomicInteger()).set(tick);
    }

    public static void remove(@NotNull Player player) {
        PORTAL_TICK_MAP.remove(player);
    }

    public static void clear() {
        PORTAL_TICK_MAP.clear();
    }
}
