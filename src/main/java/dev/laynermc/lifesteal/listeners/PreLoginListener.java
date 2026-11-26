package dev.laynermc.lifesteal.listeners;

import dev.laynermc.lifesteal.LifeStealPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * Blocks eliminated players from rejoining (unless their ban has expired, in
 * which case the elimination is cleared automatically).
 */
public final class PreLoginListener implements Listener {

    private final LifeStealPlugin plugin;

    public PreLoginListener(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        String kick = plugin.elimination().loginCheck(event.getUniqueId());
        if (kick != null && !kick.isEmpty()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kick);
        }
    }
}
