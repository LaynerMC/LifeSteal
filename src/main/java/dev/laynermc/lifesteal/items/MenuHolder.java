package dev.laynermc.lifesteal.items;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Minimal inventory holder used for the revive and leaderboard GUIs. Replaces
 * the vendored SpiGUI library (10 classes) with a single 40-line class. The
 * {@link dev.laynermc.lifesteal.listeners.InventoryListener} dispatches clicks
 * based on {@link #type()} and the slot -> UUID map.
 */
public final class MenuHolder implements InventoryHolder {

    public static final String REVIVE = "revive";
    public static final String LEADERBOARD = "leaderboard";

    private final String type;
    private final int page;
    private final int maxPage;
    private final Map<Integer, UUID> slots = new HashMap<Integer, UUID>();
    private final Inventory inventory;

    public MenuHolder(String type, int page, int maxPage, String title, int size) {
        this.type = type;
        this.page = page;
        this.maxPage = maxPage;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public String type() {
        return type;
    }

    public int page() {
        return page;
    }

    public int maxPage() {
        return maxPage;
    }

    public Map<Integer, UUID> slots() {
        return slots;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
