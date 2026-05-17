package net.okocraft.morevanillaportals;

import net.kyori.adventure.key.Key;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
final class WorldPairs {

    private static final String NETHER_SUFFIX = "_nether";
    private static final String THE_END_SUFFIX = "_the_end";

    private final World.Environment environment;
    private final String dimensionSuffix;
    private final int dimensionSuffixLength;
    private final Map<Key, Key> worldPairCache;

    private WorldPairs(World.Environment environment, String dimensionSuffix, Map<Key, Key> worldPairCache) {
        this.environment = environment;
        this.dimensionSuffix = dimensionSuffix;
        this.dimensionSuffixLength = dimensionSuffix.length();
        this.worldPairCache = worldPairCache;
    }

    @Contract(" -> new")
    static WorldPairs nether() {
        return new WorldPairs(World.Environment.NETHER, NETHER_SUFFIX, new ConcurrentHashMap<>());
    }

    @Contract(" -> new")
    static WorldPairs end() {
        return new WorldPairs(World.Environment.THE_END, THE_END_SUFFIX, new ConcurrentHashMap<>());
    }

    @Nullable Key getPairWorldKey(World world) {
        Key worldKey = world.key();
        Key cached = this.worldPairCache.get(worldKey);
        if (cached != null) {
            return cached;
        }

        Key dest;
        if (world.getEnvironment() == World.Environment.NORMAL) {
            dest = Key.key(worldKey.namespace(), worldKey.value() + this.dimensionSuffix);
        } else if (world.getEnvironment() == this.environment && worldKey.value().endsWith(this.dimensionSuffix)) {
            dest = Key.key(worldKey.namespace(), worldKey.value().substring(0, worldKey.value().length() - this.dimensionSuffixLength));
        } else {
            return null;
        }

        this.worldPairCache.put(worldKey, dest);
        return dest;
    }
}
