package net.okocraft.morevanillaportals.util;

import org.bukkit.Bukkit;

public class FoliaChecker {

    public static final boolean IS_FOLIA;

    static {
        boolean isFolia;

        try {
            Bukkit.class.getDeclaredMethod("getAsyncScheduler");
            isFolia = true;
        } catch (NoSuchMethodException e) {
            isFolia = false;
        }

        IS_FOLIA = isFolia;
    }
}
