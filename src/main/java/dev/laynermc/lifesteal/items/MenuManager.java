package dev.laynermc.lifesteal.items;

import dev.laynermc.lifesteal.LifeStealPlugin;
import dev.laynermc.lifesteal.compat.Compat;
import dev.laynermc.lifesteal.compat.Materials;
import dev.laynermc.lifesteal.storage.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builds the paginated revive and leaderboard GUIs using {@link MenuHolder}.
 */
public final class MenuManager {

    private static final int SIZE = 54;
    private static final int PAGE_SIZE = 45;
    private static final int PREV_SLOT = 45;
    private static final int INFO_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    private final LifeStealPlugin plugin;

    public MenuManager(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    public void openReviveMenu(Player player, int page) {
        List<UUID> eliminated = plugin.data().eliminatedPlayers();
        int maxPage = Math.max(1, (eliminated.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = clampPage(page, maxPage);
        String title = Compat.translateColors(plugin.config().getString("revive-tome.menu-title", "&a&lRevive a player"));
        MenuHolder holder = new MenuHolder(MenuHolder.REVIVE, page, maxPage, title, SIZE);
        List<String> skullLore = plugin.config().getStringList("revive-tome.skull-lore");
        int start = page * PAGE_SIZE;
        int end = Math.min(eliminated.size(), start + PAGE_SIZE);
        int slot = 0;
        for (int i = start; i < end; i++) {
            UUID u = eliminated.get(i);
            OfflinePlayer op = Bukkit.getOfflinePlayer(u);
            String name = op.getName() == null ? u.toString() : op.getName();
            ItemStack head = plugin.items().playerHead(op, "&6&l" + name, skullLore);
            if (head != null) {
                holder.getInventory().setItem(slot, head);
                holder.slots().put(slot, u);
                slot++;
            }
        }
        addPagination(holder, page, maxPage);
        player.openInventory(holder.getInventory());
    }

    public void openLeaderboard(Player player, int page) {
        if (!plugin.config().getBoolean("leaderboard.enabled", true)) {
            plugin.messages().send(player, "general.feature-disabled");
            return;
        }
        List<Map.Entry<UUID, PlayerData>> all = plugin.data().topByHearts(Integer.MAX_VALUE);
        if (all.isEmpty()) {
            plugin.messages().send(player, "leaderboard.empty");
            return;
        }
        int maxPage = Math.max(1, (all.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = clampPage(page, maxPage);
        String rawTitle = plugin.config().getString("leaderboard.menu-title", "&c&lHeart Top")
                .replace("_PAGE_", String.valueOf(page + 1))
                .replace("_MAX_", String.valueOf(maxPage));
        MenuHolder holder = new MenuHolder(MenuHolder.LEADERBOARD, page, maxPage, Compat.translateColors(rawTitle), SIZE);
        int start = page * PAGE_SIZE;
        int end = Math.min(all.size(), start + PAGE_SIZE);
        int rank = start + 1;
        for (int i = start; i < end; i++) {
            Map.Entry<UUID, PlayerData> e = all.get(i);
            PlayerData d = e.getValue();
            String name = d.name == null || d.name.isEmpty() ? e.getKey().toString() : d.name;
            OfflinePlayer op = Bukkit.getOfflinePlayer(e.getKey());
            List<String> lore = Arrays.asList(
                    "&7Rank: &c" + rank,
                    "&7Hearts: &c" + d.heartsInt(),
                    "&7Kills: &a" + d.kills,
                    "&7Deaths: &c" + d.deaths);
            ItemStack head = plugin.items().playerHead(op, "&c" + rank + ". &f" + name, lore);
            if (head != null) holder.getInventory().setItem(i - start, head);
            rank++;
        }
        addPagination(holder, page, maxPage);
        player.openInventory(holder.getInventory());
    }

    private void addPagination(MenuHolder holder, int page, int maxPage) {
        if (page > 0) {
            ItemStack prev = namedItem("ARROW", plugin.messages().get("leaderboard.prev-page"), null);
            holder.getInventory().setItem(PREV_SLOT, prev);
        }
        ItemStack info = namedItem("PAPER",
                plugin.messages().get("leaderboard.page-info", "page", String.valueOf(page + 1), "maxPage", String.valueOf(maxPage)),
                null);
        holder.getInventory().setItem(INFO_SLOT, info);
        if (page < maxPage - 1) {
            ItemStack next = namedItem("ARROW", plugin.messages().get("leaderboard.next-page"), null);
            holder.getInventory().setItem(NEXT_SLOT, next);
        }
    }

    private ItemStack namedItem(String material, String name, List<String> lore) {
        ItemStack item = Materials.stack(material, 1);
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null && !name.isEmpty()) meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                List<String> translated = new ArrayList<String>();
                for (String l : lore) translated.add(Compat.translateColors(l));
                meta.setLore(translated);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private int clampPage(int page, int maxPage) {
        if (page < 0) page = 0;
        if (page >= maxPage) page = maxPage - 1;
        return page;
    }

    public static int prevSlot() { return PREV_SLOT; }
    public static int nextSlot() { return NEXT_SLOT; }
}
