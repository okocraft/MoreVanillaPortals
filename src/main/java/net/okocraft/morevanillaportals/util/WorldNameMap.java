package net.okocraft.morevanillaportals.util;

import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WorldNameMap {

    private static final String NETHER_SUFFIX = "_nether";
    private static final String THE_END_SUFFIX = "_the_end";

    private final World.Environment environment;
    private final String dimensionSuffix;
    private final int dimensionSuffixLength;

    private final Map<String, String> worldNameCache;

    private WorldNameMap(@NotNull World.Environment environment, @NotNull String dimensionSuffix, @NotNull Map<String, String> worldNameCache) {
        this.environment = environment;
        this.dimensionSuffix = dimensionSuffix;
        this.dimensionSuffixLength = dimensionSuffix.length();
        this.worldNameCache = worldNameCache;
    }

    @Contract("_ -> new")
    public static @NotNull WorldNameMap nether(boolean useConcurrentMap) {
        return new WorldNameMap(World.Environment.NETHER, NETHER_SUFFIX, createMap(useConcurrentMap));
    }

    @Contract("_ -> new")
    public static @NotNull WorldNameMap end(boolean useConcurrentMap) {
        return new WorldNameMap(World.Environment.THE_END, THE_END_SUFFIX, createMap(useConcurrentMap));
    }

    private static @NotNull Map<String, String> createMap(boolean useConcurrentMap) {
        return useConcurrentMap ? new ConcurrentHashMap<>() : new HashMap<>();
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
