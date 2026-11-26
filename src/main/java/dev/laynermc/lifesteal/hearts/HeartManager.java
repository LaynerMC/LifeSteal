package dev.laynermc.lifesteal.hearts;

import dev.laynermc.lifesteal.LifeStealPlugin;
import dev.laynermc.lifesteal.storage.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Central heart logic. Hearts are stored as max-health HP (1 heart = 2 HP) in
 * the player's {@link PlayerData} and applied to the live player with the
 * stable {@link org.bukkit.entity.LivingEntity#setMaxHealth(double)} API, which
 * works across every server version (1.8.8 - 26.3) and delegates to the
 * max-health attribute internally.
 */
public final class HeartManager {

    private final LifeStealPlugin plugin;

    public HeartManager(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    public PlayerData data(Player p) {
        return plugin.data().get(p);
    }

    public PlayerData data(UUID u) {
        return plugin.data().get(u);
    }

    public int hearts(Player p) {
        return data(p).heartsInt();
    }

    public double hp(int hearts) {
        return hearts * 2.0;
    }

    public int heartsFromHp(double hp) {
        return (int) Math.round(hp / 2.0);
    }

    /** Effective max HP for a player; -1 means unlimited. */
    public double effectiveMaxHp(PlayerData d, Player p) {
        if (p != null && p.hasPermission("lifesteal.bypass.heartcap")) return -1;
        double per = d.maxHearts;
        if (per >= 0) return per; // personal cap (already in HP)
        // per < 0 -> fall back to the global cap
        double global = plugin.config().getDouble("hearts.maximum", 25);
        if (global < 0) return -1; // unlimited
        return global * 2.0;
    }

    public int effectiveMaxHearts(PlayerData d, Player p) {
        double hp = effectiveMaxHp(d, p);
        return hp < 0 ? -1 : heartsFromHp(hp);
    }

    public void applyMaxHealth(Player p, double hp) {
        double safe = hp < 2.0 ? 2.0 : hp; // never below 1 heart (attribute minimum)
        try {
            p.setMaxHealth(safe);
            if (p.getHealth() > safe) p.setHealth(safe);
        } catch (Throwable ignored) {
        }
    }

    /** Called on join: clamps stored hearts to the cap and applies them. */
    public void sync(Player p) {
        PlayerData d = data(p);
        double max = effectiveMaxHp(d, p);
        if (max >= 0 && d.hearts > max) d.hearts = max;
        if (d.hearts < 0) d.hearts = 0;
        applyMaxHealth(p, d.hearts);
    }

    public void setHearts(Player p, int hearts) {
        PlayerData d = data(p);
        double hpVal = hearts * 2.0;
        double max = effectiveMaxHp(d, p);
        if (max >= 0 && hpVal > max) hpVal = max;
        if (hpVal < 0) hpVal = 0;
        d.hearts = hpVal;
        if (hpVal > 0) applyMaxHealth(p, hpVal);
    }

    public void setHeartsOffline(UUID u, int hearts) {
        PlayerData d = data(u);
        double hpVal = hearts * 2.0;
        double max = effectiveMaxHp(d, null);
        if (max >= 0 && hpVal > max) hpVal = max;
        if (hpVal < 0) hpVal = 0;
        d.hearts = hpVal;
        if (hpVal > 0) {
            Player online = Bukkit.getPlayer(u);
            if (online != null) applyMaxHealth(online, hpVal);
        }
    }

    /** Returns true if the addition fit within the cap. */
    public boolean addHearts(Player p, int hearts) {
        PlayerData d = data(p);
        double newHp = d.hearts + hearts * 2.0;
        double max = effectiveMaxHp(d, p);
        if (max >= 0 && newHp > max) return false;
        d.hearts = newHp;
        applyMaxHealth(p, newHp);
        return true;
    }

    /** Returns true if the player reached (or is below) the elimination minimum. */
    public boolean removeHearts(Player p, int hearts) {
        PlayerData d = data(p);
        double newHp = d.hearts - hearts * 2.0;
        if (newHp < 0) newHp = 0;
        d.hearts = newHp;
        double minHp = minimumHearts() * 2.0;
        boolean dead = newHp <= minHp;
        if (!dead) applyMaxHealth(p, newHp);
        return dead;
    }

    public boolean isAtOrBelowMinimum(PlayerData d) {
        return d.hearts <= minimumHearts() * 2.0;
    }

    /** Per-player cap. hearts < 0 means unlimited; NaN (cleared) means use global. */
    public void setMaxHearts(Player p, int hearts) {
        data(p).maxHearts = hearts < 0 ? -1.0 : hearts * 2.0;
    }

    public void setMaxHeartsOffline(UUID u, int hearts) {
        data(u).maxHearts = hearts < 0 ? -1.0 : hearts * 2.0;
    }

    public void clearMaxHearts(UUID u) {
        data(u).maxHearts = Double.NaN;
    }

    public void reset(Player p) {
        setHearts(p, startingHearts());
    }

    public void resetOffline(UUID u) {
        setHeartsOffline(u, startingHearts());
    }

    public int startingHearts() {
        return plugin.config().getInt("hearts.starting", 10);
    }

    public int minimumHearts() {
        return plugin.config().getInt("hearts.minimum", 0);
    }

    public int stealAmount() {
        return plugin.config().getInt("hearts.steal-amount", 1);
    }

    public int lossAmount() {
        return plugin.config().getInt("hearts.loss-amount", 1);
    }

    public boolean loseOnlyIfKilledByPlayer() {
        return plugin.config().getBoolean("hearts.lose-only-if-killed-by-player", false);
    }

    public boolean selfKillProtection() {
        return plugin.config().getBoolean("hearts.self-kill-protection", true);
    }
}
