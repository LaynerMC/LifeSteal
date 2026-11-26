package dev.laynermc.lifesteal.listeners;

import dev.laynermc.lifesteal.LifeStealPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Synchronises the stored heart count onto the live player on join (this is the
 * authoritative source; vanilla attribute persistence is not relied upon).
 */
public final class JoinListener implements Listener {

    private final LifeStealPlugin plugin;

    public JoinListener(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.data().get(player); // ensure the player has a data entry
        plugin.hearts().sync(player);
        plugin.display().show(player);
    }
}
