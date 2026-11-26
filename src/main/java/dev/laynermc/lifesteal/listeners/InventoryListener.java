package dev.laynermc.lifesteal.listeners;

import dev.laynermc.lifesteal.LifeStealPlugin;
import dev.laynermc.lifesteal.items.MenuHolder;
import dev.laynermc.lifesteal.items.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Handles clicks in the revive and leaderboard GUIs: cancels item taking,
 * paginates on the prev/next buttons and revives the clicked player.
 */
public final class InventoryListener implements Listener {

    private final LifeStealPlugin plugin;

    public InventoryListener(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof MenuHolder)) return;
        MenuHolder menu = (MenuHolder) holder;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        if (slot == MenuManager.prevSlot()) {
            openPage(menu, player, menu.page() - 1);
            return;
        }
        if (slot == MenuManager.nextSlot()) {
            openPage(menu, player, menu.page() + 1);
            return;
        }

        if (MenuHolder.REVIVE.equals(menu.type())) {
            UUID target = menu.slots().get(slot);
            if (target == null) return;
            OfflinePlayer op = Bukkit.getOfflinePlayer(target);
            String name = op.getName() == null ? target.toString() : op.getName();
            boolean ok = plugin.elimination().revive(target, player);
            if (ok) {
                plugin.items().consumeReviveTome(player);
                plugin.messages().send(player, "revive.success", "player", name);
                player.closeInventory();
            } else {
                plugin.messages().send(player, "revive.not-needed", "player", name);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }

    private void openPage(MenuHolder menu, Player player, int page) {
        if (MenuHolder.REVIVE.equals(menu.type())) {
            plugin.menus().openReviveMenu(player, page);
        } else {
            plugin.menus().openLeaderboard(player, page);
        }
    }
}
