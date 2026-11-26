package dev.laynermc.lifesteal.listeners;

import dev.laynermc.lifesteal.LifeStealPlugin;
import dev.laynermc.lifesteal.storage.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Applies the heart steal/loss on player death and exposes the same logic for
 * reuse (e.g. by the combat logger or admin commands).
 */
public final class DeathListener implements Listener {

    private final LifeStealPlugin plugin;

    public DeathListener(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        applyPenalty(event.getEntity(), event.getEntity().getKiller());
    }

    public void applyPenalty(Player victim, Player killer) {
        if (victim == null) return;
        PlayerData vd = plugin.data().get(victim);
        vd.deaths++;

        // Self-kill protection.
        if (killer != null && killer.equals(victim) && plugin.hearts().selfKillProtection()) {
            return;
        }

        // Alt-farming check (only when killed by another player).
        if (killer != null && !killer.equals(victim) && isAltFarming(victim, killer)) {
            String msg = plugin.messages().get("security.alt-farming", "killed", victim.getName());
            if (!msg.isEmpty()) {
                killer.sendMessage(msg);
                victim.sendMessage(msg);
            }
            return;
        }

        // Victim heart loss.
        boolean shouldLose = !plugin.hearts().loseOnlyIfKilledByPlayer() || killer != null;
        if (shouldLose) {
            boolean dead = plugin.hearts().removeHearts(victim, plugin.hearts().lossAmount());
            plugin.messages().send(victim, "heart.lost");
            plugin.effects().play(victim, "heart-lost");
            if (dead) {
                plugin.elimination().eliminate(victim);
            }
        }

        // Killer heart gain.
        if (killer != null && !killer.equals(victim)) {
            PlayerData kd = plugin.data().get(killer);
            kd.kills++;
            int steal = plugin.hearts().stealAmount();
            boolean added = plugin.hearts().addHearts(killer, steal);
            if (added) {
                plugin.messages().send(killer, "heart.gained", "player", victim.getName());
                plugin.effects().play(killer, "heart-gained");
            } else {
                plugin.messages().send(killer, "heart.max-cap-dropped", "player", victim.getName());
                ItemStack heart = plugin.items().createHeartItem(plugin.config().getInt("heart-item.success-chance", 100));
                if (heart != null) victim.getWorld().dropItemNaturally(victim.getLocation(), heart);
            }
        }
    }

    private boolean isAltFarming(Player victim, Player killer) {
        if (!plugin.config().getBoolean("security.alt-farming.ip-check", false)) return false;
        if (killer.hasPermission("lifesteal.bypass.altfarming")) return false;
        String vIp = ipOf(victim);
        String kIp = ipOf(killer);
        if (vIp == null || kIp == null) return false;
        return vIp.equalsIgnoreCase(kIp);
    }

    private static String ipOf(Player p) {
        try {
            if (p.getAddress() == null || p.getAddress().getAddress() == null) return null;
            return p.getAddress().getAddress().getHostAddress();
        } catch (Throwable t) {
            return null;
        }
    }
}
