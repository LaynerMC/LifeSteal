package dev.laynermc.lifesteal.hearts;

import dev.laynermc.lifesteal.LifeStealPlugin;
import dev.laynermc.lifesteal.storage.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles elimination (ban on 0 hearts), revival, custom commands and the
 * login-time rejoin block. Elimination state lives on {@link PlayerData}.
 */
public final class EliminationManager {

    private final LifeStealPlugin plugin;

    public EliminationManager(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean enabled() {
        return plugin.config().getBoolean("elimination.enabled", true);
    }

    public boolean blockRejoin() {
        return plugin.config().getBoolean("elimination.block-rejoin", true);
    }

    public boolean broadcast() {
        return plugin.config().getBoolean("elimination.broadcast", true);
    }

    public int banTimeMinutes() {
        return plugin.config().getInt("elimination.ban-time", 0);
    }

    public int reviveHearts() {
        return plugin.config().getInt("elimination.revive-heart-amount", 10);
    }

    /** True if the player is currently eliminated (auto-clears expired bans). */
    public boolean isEliminated(UUID u) {
        PlayerData d = plugin.data().get(u);
        if (!d.eliminated) return false;
        if (d.unbanTime > 0 && System.currentTimeMillis() / 1000 > d.unbanTime) {
            d.eliminated = false;
            d.unbanTime = 0;
            d.hearts = plugin.hearts().hp(reviveHearts());
            return false;
        }
        return true;
    }

    /** Returns the kick message if the player must be blocked, or null to allow. */
    public String loginCheck(UUID u) {
        if (!enabled() || !blockRejoin()) return null;
        if (isEliminated(u)) {
            return plugin.messages().get("elimination.no-more");
        }
        return null;
    }

    public void eliminate(Player p) {
        if (p != null && p.hasPermission("lifesteal.bypass.elimination")) {
            plugin.messages().send(p, "elimination.bypass");
            return;
        }
        eliminate(p.getUniqueId(), p);
    }

    public void eliminate(UUID u, Player onlineIfAny) {
        PlayerData d = plugin.data().get(u);
        if (d.eliminated) return;
        d.eliminated = true;
        d.eliminations++;
        d.hearts = 0;
        long banTime = banTimeMinutes();
        d.unbanTime = banTime > 0 ? System.currentTimeMillis() / 1000 + banTime * 60 : 0;
        runCustom("on-eliminate", u, null);
        if (onlineIfAny != null) {
            plugin.effects().play(onlineIfAny, "eliminated");
        }
        if (broadcast()) {
            String name = d.name.isEmpty() ? u.toString() : d.name;
            String msg = plugin.messages().get("elimination.broadcast", "player", name);
            if (!msg.isEmpty()) Bukkit.broadcastMessage(msg);
        }
        if (onlineIfAny != null && blockRejoin()) {
            final Player target = onlineIfAny;
            final String kick = plugin.messages().get("elimination.no-more");
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    if (target.isOnline()) target.kickPlayer(kick);
                }
            }, 2L);
        }
        plugin.data().saveAll();
    }

    public boolean revive(UUID u, Player reviver) {
        PlayerData d = plugin.data().get(u);
        if (!d.eliminated) return false;
        d.eliminated = false;
        d.unbanTime = 0;
        d.revives++;
        d.hearts = plugin.hearts().hp(reviveHearts());
        Player online = Bukkit.getPlayer(u);
        if (online != null) plugin.hearts().applyMaxHealth(online, d.hearts);
        runCustom("on-revive", u, reviver);
        plugin.effects().play(online, "revived");
        plugin.data().saveAll();
        return true;
    }

    public int reviveAll() {
        int count = 0;
        for (UUID u : plugin.data().eliminatedPlayers()) {
            if (revive(u, null)) count++;
        }
        return count;
    }

    public int eliminateAll() {
        int count = 0;
        for (Map.Entry<UUID, PlayerData> e : plugin.data().all()) {
            Player p = Bukkit.getPlayer(e.getKey());
            if (e.getKey() != null && !e.getValue().eliminated) {
                if (p != null && p.hasPermission("lifesteal.bypass.elimination")) continue;
                eliminate(e.getKey(), p);
                count++;
            }
        }
        return count;
    }

    private void runCustom(String path, UUID player, Player reviver) {
        String mode = plugin.config().getString("custom-commands.mode", "disabled");
        if (mode == null || mode.equalsIgnoreCase("disabled")) return;
        PlayerData d = plugin.data().get(player);
        String name = d.name.isEmpty() ? player.toString() : d.name;
        String reviverName = reviver == null ? "CONSOLE" : reviver.getName();
        for (String cmd : plugin.config().getStringList("custom-commands." + path)) {
            String c = cmd.replace("${player}", name)
                    .replace("%player%", name)
                    .replace("${revived}", name)
                    .replace("%revived%", name)
                    .replace("${reviving}", reviverName)
                    .replace("%reviving%", reviverName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c);
        }
    }
}
