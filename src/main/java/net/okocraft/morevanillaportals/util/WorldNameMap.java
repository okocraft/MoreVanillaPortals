package net.okocraft.morevanillaportals.util;

import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class WorldNameMap {

    private static final String NETHER_SUFFIX = "_nether";
    private static final String THE_END_SUFFIX = "_the_end";

    private final World.Environment environment;
    private final String dimensionSuffix;
    private final int dimensionSuffixLength;

    private final Map<String, String> worldNameCache = new HashMap<>();

    private WorldNameMap(@NotNull World.Environment environment, @NotNull String dimensionSuffix) {
        this.environment = environment;
        this.dimensionSuffix = dimensionSuffix;
        this.dimensionSuffixLength = dimensionSuffix.length();
    }

    @Contract(" -> new")
    public static @NotNull WorldNameMap nether() {
        return new WorldNameMap(World.Environment.NETHER, NETHER_SUFFIX);
    }

    @Contract(" -> new")
    public static @NotNull WorldNameMap end() {
        return new WorldNameMap(World.Environment.THE_END, THE_END_SUFFIX);
    }

    public @Nullable String getWorldNameOfPortalDestination(@NotNull World world) {
        var worldName = world.getName();
        String distWorldName;

        if (worldNameCache.containsKey(worldName)) {
            distWorldName = worldNameCache.get(worldName);
        } else {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                distWorldName = worldName + dimensionSuffix;
            } else if (world.getEnvironment() == environment && worldName.endsWith(dimensionSuffix)) {
                distWorldName = worldName.substring(0, worldName.length() - dimensionSuffixLength);
            } else {
                return null;
            }

            worldNameCache.put(worldName, distWorldName);
        }

        return distWorldName;
    }
}
