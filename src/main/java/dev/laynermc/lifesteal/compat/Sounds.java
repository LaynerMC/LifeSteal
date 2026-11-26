package dev.laynermc.lifesteal.compat;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Version-safe sound lookup. Sound enum names changed between 1.8 and 1.9
 * (e.g. LEVEL_UP -> ENTITY_PLAYER_LEVELUP). The table only needs to cover the
 * sounds this plugin actually plays; unknown or unsupported names are silently
 * skipped, so missing sounds never break the plugin.
 */
public final class Sounds {

    private Sounds() {}

    /** modern name -> 1.8.x name */
    private static final Map<String, String> LEGACY = new HashMap<String, String>();
    static {
        LEGACY.put("ENTITY_PLAYER_LEVELUP", "LEVEL_UP");
        LEGACY.put("ENTITY_LIGHTNING_BOLT_THUNDER", "AMBIENCE_THUNDER");
        LEGACY.put("ENTITY_VILLAGER_NO", "VILLAGER_NO");
        LEGACY.put("ENTITY_VILLAGER_YES", "VILLAGER_YES");
        LEGACY.put("ENTITY_VILLAGER_HURT", "VILLAGER_HIT");
        LEGACY.put("ENTITY_VILLAGER_DEATH", "VILLAGER_DEATH");
        LEGACY.put("ENTITY_VILLAGER_AMBIENT", "VILLAGER_IDLE");
        LEGACY.put("BLOCK_ANVIL_LAND", "ANVIL_LAND");
        LEGACY.put("BLOCK_ANVIL_USE", "ANVIL_USE");
        LEGACY.put("BLOCK_ANVIL_BREAK", "ANVIL_BREAK");
        LEGACY.put("ENTITY_WITHER_DEATH", "WITHER_DEATH");
        LEGACY.put("ENTITY_WITHER_SPAWN", "WITHER_SPAWN");
        LEGACY.put("ENTITY_WITHER_AMBIENT", "WITHER_IDLE");
        LEGACY.put("ENTITY_WITHER_HURT", "WITHER_HURT");
        LEGACY.put("ENTITY_WITHER_SHOOT", "WITHER_SHOOT");
        LEGACY.put("UI_TOAST_CHALLENGE_COMPLETE", "CHALLENGE_COMPLETE");
        LEGACY.put("UI_TOAST_IN", "ORB_PICKUP");
        LEGACY.put("UI_TOAST_OUT", "ORB_PICKUP");
        LEGACY.put("UI_BUTTON_CLICK", "CLICK");
        LEGACY.put("ENTITY_ENDERMAN_DEATH", "ENDERMAN_DEATH");
        LEGACY.put("ENTITY_ENDERMAN_TELEPORT", "ENDERMAN_TELEPORT");
        LEGACY.put("ENTITY_GENERIC_EXPLODE", "EXPLODE");
        LEGACY.put("ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP");
        LEGACY.put("ENTITY_ITEM_PICKUP", "ITEM_PICKUP");
        LEGACY.put("ENTITY_ARROW_HIT_PLAYER", "ARROW_HIT");
        LEGACY.put("ENTITY_ARROW_HIT", "ARROW_HIT");
        LEGACY.put("BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING");
        LEGACY.put("BLOCK_PORTAL_TRAVEL", "PORTAL_TRAVEL");
        LEGACY.put("BLOCK_PORTAL_AMBIENT", "PORTAL");
        LEGACY.put("ITEM_TOTEM_USE", "TOTEM");
        LEGACY.put("ENTITY_PLAYER_HURT", "HURT_FLESH");
        LEGACY.put("ENTITY_PLAYER_DEATH", "HURT_FLESH");
        LEGACY.put("ENTITY_FIREWORK_ROCKET_BLAST", "FIREWORK_BLAST");
        LEGACY.put("ENTITY_FIREWORK_ROCKET_LAUNCH", "FIREWORK_LAUNCH");
        LEGACY.put("ENTITY_DRAGON_FIREBALL_EXPLODE", "EXPLODE");
        LEGACY.put("ENTITY_ENDER_DRAGON_GROWL", "ENDERDRAGON_GROWL");
        LEGACY.put("ENTITY_ENDER_DRAGON_DEATH", "ENDERDRAGON_DEATH");
        LEGACY.put("ENTITY_ENDER_DRAGON_AMBIENT", "ENDERDRAGON_GROWL");
        LEGACY.put("ENTITY_ENDER_DRAGON_HURT", "ENDERDRAGON_HIT");
        LEGACY.put("ENTITY_ENDER_DRAGON_FLAP", "ENDERDRAGON_WINGS");
        LEGACY.put("ENTITY_ZOMBIE_AMBIENT", "ZOMBIE_IDLE");
        LEGACY.put("ENTITY_ZOMBIE_HURT", "ZOMBIE_HURT");
        LEGACY.put("ENTITY_ZOMBIE_DEATH", "ZOMBIE_DEATH");
        LEGACY.put("ENTITY_SKELETON_AMBIENT", "SKELETON_IDLE");
        LEGACY.put("ENTITY_SKELETON_HURT", "SKELETON_HURT");
        LEGACY.put("ENTITY_SKELETON_DEATH", "SKELETON_DEATH");
        LEGACY.put("ENTITY_CREEPER_PRIMED", "CREEPER_HISS");
        LEGACY.put("ENTITY_GENERIC_BURN", "FIZZ");
        LEGACY.put("ENTITY_GENERIC_DRINK", "DRINK");
        LEGACY.put("ENTITY_GENERIC_EAT", "EAT");
        LEGACY.put("ENTITY_GENERIC_BIG_FALL", "FALL_BIG");
        LEGACY.put("ENTITY_GENERIC_SMALL_FALL", "FALL_SMALL");
        LEGACY.put("ENTITY_FIREWORK_ROCKET_SHOOT", "SHOOT_ARROW");
        LEGACY.put("BLOCK_BEACON_ACTIVATE", "LEVEL_UP");
        LEGACY.put("BLOCK_BEACON_AMBIENT", "PORTAL");
        LEGACY.put("BLOCK_BEACON_DEACTIVATE", "FIZZ");
        LEGACY.put("BLOCK_BEACON_POWER_SELECT", "ORB_PICKUP");
    }

    public static Sound resolve(String name) {
        if (name == null || name.isEmpty()) return null;
        String upper = name.toUpperCase();
        try {
            return Sound.valueOf(upper);
        } catch (IllegalArgumentException e) {
            String legacy = LEGACY.get(upper);
            if (legacy != null) {
                try { return Sound.valueOf(legacy); } catch (IllegalArgumentException e2) { return null; }
            }
            return null;
        }
    }

    public static void play(Player p, String name, float vol, float pitch) {
        Sound s = resolve(name);
        if (s == null || p == null) return;
        try {
            p.playSound(p.getLocation(), s, vol, pitch);
        } catch (Throwable ignored) {
        }
    }
}
