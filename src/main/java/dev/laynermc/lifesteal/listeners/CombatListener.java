package dev.laynermc.lifesteal.listeners;

import dev.laynermc.lifesteal.LifeStealPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Tags both the attacker and the victim whenever a player deals PvP damage, so
 * the combat logger can track them.
 */
public final class CombatListener implements Listener {

    private final LifeStealPlugin plugin;

    public CombatListener(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!plugin.combat().enabled()) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null) return;
        if (attacker.equals(victim)) return;
        plugin.combat().tag(victim);
        plugin.combat().tag(attacker);
    }

    private static Player resolveAttacker(Object damager) {
        if (damager instanceof Player) return (Player) damager;
        return null;
    }
}
