package dev.laynermc.lifesteal.listeners;

import dev.laynermc.lifesteal.LifeStealPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Punishes combat-loggers (inventory drop) and stops the display for the player.
 */
public final class QuitListener implements Listener {

    private final LifeStealPlugin plugin;

    public QuitListener(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.combat().onQuit(player);
        plugin.display().hide(player);
    }
}
