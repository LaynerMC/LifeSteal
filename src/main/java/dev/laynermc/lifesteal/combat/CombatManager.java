package dev.laynermc.lifesteal.combat;

import dev.laynermc.lifesteal.LifeStealPlugin;
import dev.laynermc.lifesteal.compat.Compat;
import dev.laynermc.lifesteal.compat.Version;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Combat logger. Tags players for a configurable duration when they deal or take
 * PvP damage, shows a countdown (boss bar on 1.9+, action bar everywhere), blocks
 * configured commands while tagged, and drops the inventory of players who log
 * out while in combat.
 */
public final class CombatManager {

    private final LifeStealPlugin plugin;
    private final Map<UUID, Tag> tags = new HashMap<UUID, Tag>();
    private int taskId = -1;
    private final boolean bossBarAvailable = Version.hasBossBar();

    public CombatManager(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean enabled() {
        return plugin.config().getBoolean("combat-logger.enabled", true);
    }

    public int duration() {
        return plugin.config().getInt("combat-logger.duration", 10);
    }

    public String display() {
        String s = plugin.config().getString("combat-logger.display", "BOSSBAR");
        return s == null ? "NONE" : s.toUpperCase();
    }

    public boolean isTagged(Player p) {
        Tag t = tags.get(p.getUniqueId());
        return t != null && System.currentTimeMillis() < t.expiry;
    }

    public void tag(Player p) {
        if (!enabled()) return;
        if (p.hasPermission("lifesteal.bypass.combatlog")) return;
        boolean wasTagged = isTagged(p);
        Tag t = tags.get(p.getUniqueId());
        if (t == null) t = new Tag();
        t.expiry = System.currentTimeMillis() + duration() * 1000L;
        if ("BOSSBAR".equals(display()) && bossBarAvailable && t.bar == null) {
            t.bar = Compat.createBossBar(
                    plugin.messages().raw("combat-logger.tagged"),
                    plugin.config().getString("combat-logger.bossbar-color", "RED"),
                    plugin.config().getString("combat-logger.bossbar-style", "SOLID"));
            if (t.bar != null) {
                t.bar.setVisible(true);
                t.bar.addPlayer(p);
            }
        }
        tags.put(p.getUniqueId(), t);
        if (!wasTagged) {
            plugin.messages().send(p, "combat-logger.tagged");
            plugin.effects().play(p, "combat-tag");
        }
    }

    public boolean isBlockedCommand(String label) {
        List<String> blocked = plugin.config().getStringList("combat-logger.blocked-commands");
        String l = label.toLowerCase();
        for (String b : blocked) {
            if (b != null && l.equals(b.toLowerCase())) return true;
        }
        return false;
    }

    /** Called from PlayerQuitEvent: punishes a combat-logger by dropping gear. */
    public void onQuit(Player p) {
        Tag t = tags.get(p.getUniqueId());
        boolean wasTagged = t != null && System.currentTimeMillis() < t.expiry;
        if (t != null) {
            if (t.bar != null) {
                t.bar.removePlayer(p);
                t.bar.removeAll();
            }
            tags.remove(p.getUniqueId());
        }
        if (!wasTagged) return;
        if (!plugin.config().getBoolean("combat-logger.kill-on-logout", true)) return;
        dropAndClearInventory(p);
        if (plugin.config().getBoolean("combat-logger.broadcast", true)) {
            String msg = plugin.messages().get("combat-logger.logged-out", "player", p.getName());
            if (!msg.isEmpty()) Bukkit.broadcastMessage(msg);
        }
    }

    private void dropAndClearInventory(Player p) {
        Location loc = p.getLocation();
        ItemStack[] contents = p.getInventory().getContents();
        for (ItemStack item : contents) {
            if (item != null && item.getType() != null) {
                p.getWorld().dropItemNaturally(loc, item);
            }
        }
        ItemStack[] armor = p.getInventory().getArmorContents();
        for (ItemStack item : armor) {
            if (item != null && item.getType() != null) {
                p.getWorld().dropItemNaturally(loc, item);
            }
        }
        p.getInventory().clear();
        p.getInventory().setArmorContents(new ItemStack[4]);
    }

    public void start() {
        if (taskId != -1) return;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                tick();
            }
        }, 5L, 5L);
    }

    public void stop() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
        for (Tag t : tags.values()) {
            if (t.bar != null) t.bar.removeAll();
        }
        tags.clear();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Tag>> it = tags.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Tag> entry = it.next();
            Tag t = entry.getValue();
            if (now >= t.expiry) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (t.bar != null) {
                    if (p != null) t.bar.removePlayer(p);
                    t.bar.removeAll();
                }
                it.remove();
                if (p != null && p.isOnline()) plugin.messages().send(p, "combat-logger.untagged");
                continue;
            }
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null || !p.isOnline()) continue;
            double remaining = (t.expiry - now) / 1000.0;
            int sec = (int) Math.ceil(remaining);
            double progress = Math.max(0.0, Math.min(1.0, remaining / duration()));
            String mode = display();
            String title = plugin.messages().raw("combat-logger.tagged") + " &7(" + sec + "s)";
            if ("BOSSBAR".equals(mode) && t.bar != null) {
                t.bar.setTitle(Compat.translateColors(title));
                t.bar.setProgress(progress);
            } else if ("ACTIONBAR".equals(mode) || ("BOSSBAR".equals(mode) && t.bar == null)) {
                // Action bar (also the 1.8 fallback when boss bars are unavailable).
                if (sec != t.lastChatSecond) {
                    t.lastChatSecond = sec;
                    Compat.sendActionBar(p, title);
                }
            } else if ("CHAT".equals(mode)) {
                if (sec != t.lastChatSecond) {
                    t.lastChatSecond = sec;
                    plugin.messages().send(p, "combat-logger.tagged");
                }
            }
        }
    }

    private static final class Tag {
        long expiry;
        Compat.BossBarHandle bar;
        int lastChatSecond = -1;
    }
}
