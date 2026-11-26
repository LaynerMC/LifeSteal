package dev.laynermc.lifesteal.listeners;

import dev.laynermc.lifesteal.LifeStealPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Blocks configured commands while a player is combat-tagged.
 */
public final class CommandListener implements Listener {

    private final LifeStealPlugin plugin;

    public CommandListener(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player p = event.getPlayer();
        if (!plugin.combat().enabled() || !plugin.combat().isTagged(p)) return;
        String message = event.getMessage();
        if (message == null || message.length() < 2) return;
        String label = message.substring(1).split(" ")[0].toLowerCase();
        int colon = label.indexOf(':');
        if (colon >= 0) label = label.substring(colon + 1);
        if (plugin.combat().isBlockedCommand(label)) {
            event.setCancelled(true);
            plugin.messages().send(p, "combat-logger.blocked-command", "command", label);
        }
    }
}
