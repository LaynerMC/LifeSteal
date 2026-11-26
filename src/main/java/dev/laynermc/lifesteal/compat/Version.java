package dev.laynermc.lifesteal.compat;

import org.bukkit.Bukkit;

/**
 * Detects the running server version and exposes the API thresholds that matter
 * for this plugin. Versions use the new Mojang numbering (1.21.x -> 26.1, 26.2, 26.3).
 *
 * For the legacy scheme (1.x) the comparable major is the second component
 * (1.8 -> 8, 1.12 -> 12, 1.21 -> 21). For the new scheme it is the first
 * component (26.x -> 26). Both schemes produce a single increasing integer.
 */
public final class Version {

    private static final int MAJOR = parse();

    private Version() {}

    private static int parse() {
        String raw = Bukkit.getBukkitVersion(); // e.g. "1.8.8-R0.1-SNAPSHOT" or "26.3-R0.1-SNAPSHOT"
        String clean = raw.split("-")[0];
        String[] parts = clean.split("\\.");
        try {
            int first = Integer.parseInt(parts[0]);
            if (first == 1 && parts.length > 1) {
                return Integer.parseInt(parts[1]);
            }
            return first;
        } catch (NumberFormatException e) {
            return 99; // unknown -> assume modern
        }
    }

    public static int major() {
        return MAJOR;
    }

    public static boolean atLeast(int m) {
        return MAJOR >= m;
    }

    /** Bukkit BossBar API was added in 1.9. */
    public static boolean hasBossBar() {
        return MAJOR >= 9;
    }

    /** NamespacedKey, keyed recipes, setOwningPlayer and discoverRecipe were added in 1.12. */
    public static boolean hasNamespacedKey() {
        return MAJOR >= 12;
    }

    /** Material.matchMaterial and the flattened material names were added in 1.13. */
    public static boolean hasMatchMaterial() {
        return MAJOR >= 13;
    }

    /** Hex colours (ChatColor.of) were added in 1.16. */
    public static boolean hasHexColors() {
        return MAJOR >= 16;
    }

    public static String get() {
        return Bukkit.getBukkitVersion();
    }
}
