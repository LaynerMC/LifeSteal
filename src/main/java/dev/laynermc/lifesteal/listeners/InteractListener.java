package dev.laynermc.lifesteal.listeners;

import dev.laynermc.lifesteal.LifeStealPlugin;
import dev.laynermc.lifesteal.compat.Compat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Right-click use of the Heart item and Revive Tome.
 */
public final class InteractListener implements Listener {

    private final LifeStealPlugin plugin;

    public InteractListener(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = Compat.getMainHandItem(event.getPlayer());
        if (item == null) return;
        try {
            if (plugin.items().isHeartItem(item)) {
                event.setCancelled(true);
                plugin.items().useHeartItem(event.getPlayer(), item);
            } else if (plugin.items().isReviveTome(item)) {
                event.setCancelled(true);
                plugin.items().useReviveTome(event.getPlayer());
            }
        } catch (Throwable ignored) {
        }
    }
}
