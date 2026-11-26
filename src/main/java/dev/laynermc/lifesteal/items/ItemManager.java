package dev.laynermc.lifesteal.items;

import dev.laynermc.lifesteal.LifeStealPlugin;
import dev.laynermc.lifesteal.compat.Compat;
import dev.laynermc.lifesteal.compat.Materials;
import dev.laynermc.lifesteal.compat.Sounds;
import dev.laynermc.lifesteal.compat.Version;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates, identifies and consumes the Heart item and Revive Tome. Items are
 * authenticated by material + display name + exact lore match (anvil-renamed
 * items cannot forge the lore), which avoids PersistentDataContainer (1.14+)
 * and works on every version.
 */
public final class ItemManager {

    private static final Pattern CHANCE_PATTERN = Pattern.compile("(\\d+)\\s*%");

    private final LifeStealPlugin plugin;

    public ItemManager(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------
    //  Heart item
    // ------------------------------------------------------------------
    public ItemStack createHeartItem(int chance) {
        String materialName = plugin.config().getString("heart-item.material", "NETHER_STAR");
        ItemStack item = Materials.stack(materialName, 1);
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Compat.translateColors(plugin.config().getString("heart-item.name", "&c&lHeart")));
            List<String> lore = new ArrayList<String>();
            for (String line : plugin.config().getStringList("heart-item.lore")) {
                lore.add(Compat.translateColors(line.replace("${chance}", String.valueOf(chance))));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isHeartItem(ItemStack item) {
        if (item == null) return false;
        Materials.Resolved mat = Materials.resolve(plugin.config().getString("heart-item.material", "NETHER_STAR"));
        if (mat == null || item.getType() != mat.material) return false;
        if (!Version.hasMatchMaterial() && mat.data != 0 && item.getDurability() != mat.data) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        String expectedName = ChatColor.stripColor(Compat.translateColors(plugin.config().getString("heart-item.name", "&c&lHeart")));
        if (!expectedName.equals(ChatColor.stripColor(meta.getDisplayName()))) return false;
        List<String> expected = expectedHeartLore(chanceOf(item));
        if (expected.isEmpty()) return true;
        List<String> actual = meta.getLore();
        if (actual == null || actual.size() != expected.size()) return false;
        for (int i = 0; i < expected.size(); i++) {
            if (!expected.get(i).equals(ChatColor.stripColor(actual.get(i)))) return false;
        }
        return true;
    }

    private List<String> expectedHeartLore(int chance) {
        List<String> out = new ArrayList<String>();
        for (String line : plugin.config().getStringList("heart-item.lore")) {
            out.add(ChatColor.stripColor(Compat.translateColors(line.replace("${chance}", String.valueOf(chance)))));
        }
        return out;
    }

    public int chanceOf(ItemStack item) {
        if (item == null) return defaultChance();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return defaultChance();
        List<String> lore = meta.getLore();
        if (lore != null) {
            for (String line : lore) {
                Matcher m = CHANCE_PATTERN.matcher(line);
                if (m.find()) {
                    try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException e) { return defaultChance(); }
                }
            }
        }
        return defaultChance();
    }

    private int defaultChance() {
        return plugin.config().getInt("heart-item.success-chance", 100);
    }

    public void useHeartItem(Player player, ItemStack item) {
        if (!plugin.config().getBoolean("heart-item.enabled", true)) {
            plugin.messages().send(player, "general.feature-disabled");
            Sounds.play(player, "ENTITY_VILLAGER_NO", 1f, 1f);
            return;
        }
        int chance = chanceOf(item);
        int roll = random(100);
        if (chance > roll) {
            int addLimit = plugin.config().getInt("heart-item.add-limit", 0);
            int current = plugin.hearts().hearts(player);
            if (addLimit <= 0 || current + 1 <= addLimit) {
                consumeOne(player);
                plugin.hearts().addHearts(player, 1);
                plugin.messages().send(player, "heart.received");
                plugin.effects().play(player, "heart-gained");
            } else {
                Sounds.play(player, "ENTITY_VILLAGER_NO", 1f, 1f);
                plugin.messages().send(player, "heart.max-from-item", "max", String.valueOf(addLimit));
            }
        } else {
            consumeOne(player);
            int loseChance = plugin.config().getInt("heart-item.lose-chance", 50);
            int roll2 = random(100);
            if (roll2 >= loseChance) {
                plugin.messages().send(player, "heart.failure");
                Sounds.play(player, "ENTITY_VILLAGER_NO", 1f, 1f);
            } else {
                boolean dead = plugin.hearts().removeHearts(player, 1);
                if (dead) {
                    plugin.elimination().eliminate(player);
                } else {
                    plugin.messages().send(player, "heart.lost");
                    plugin.effects().play(player, "heart-lost");
                }
            }
        }
    }

    // ------------------------------------------------------------------
    //  Revive tome
    // ------------------------------------------------------------------
    public ItemStack createReviveTome() {
        String materialName = plugin.config().getString("revive-tome.material", "ENCHANTED_BOOK");
        ItemStack item = Materials.stack(materialName, 1);
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Compat.translateColors(plugin.config().getString("revive-tome.name", "&c&lRevive Tome")));
            List<String> lore = new ArrayList<String>();
            for (String line : plugin.config().getStringList("revive-tome.lore")) {
                lore.add(Compat.translateColors(line));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isReviveTome(ItemStack item) {
        if (item == null) return false;
        Materials.Resolved mat = Materials.resolve(plugin.config().getString("revive-tome.material", "ENCHANTED_BOOK"));
        if (mat == null || item.getType() != mat.material) return false;
        if (!Version.hasMatchMaterial() && mat.data != 0 && item.getDurability() != mat.data) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        String expectedName = ChatColor.stripColor(Compat.translateColors(plugin.config().getString("revive-tome.name", "&c&lRevive Tome")));
        if (!expectedName.equals(ChatColor.stripColor(meta.getDisplayName()))) return false;
        List<String> expected = new ArrayList<String>();
        for (String line : plugin.config().getStringList("revive-tome.lore")) {
            expected.add(ChatColor.stripColor(Compat.translateColors(line)));
        }
        if (expected.isEmpty()) return true;
        List<String> actual = meta.getLore();
        if (actual == null || actual.size() != expected.size()) return false;
        for (int i = 0; i < expected.size(); i++) {
            if (!expected.get(i).equals(ChatColor.stripColor(actual.get(i)))) return false;
        }
        return true;
    }

    public void useReviveTome(Player player) {
        if (!plugin.config().getBoolean("revive-tome.enabled", true)) {
            plugin.messages().send(player, "general.feature-disabled");
            Sounds.play(player, "ENTITY_VILLAGER_NO", 1f, 1f);
            return;
        }
        if (plugin.data().eliminatedPlayers().isEmpty()) {
            plugin.messages().send(player, "revive.not-needed", "player", "");
            Sounds.play(player, "ENTITY_VILLAGER_NO", 1f, 1f);
            return;
        }
        plugin.menus().openReviveMenu(player, 0);
    }

    /** Consume the revive tome on a successful revival (if configured). */
    public void consumeReviveTome(Player player) {
        if (!plugin.config().getBoolean("revive-tome.consume-on-use", true)) return;
        consumeOne(player);
    }

    // ------------------------------------------------------------------
    //  Shared helpers
    // ------------------------------------------------------------------
    public ItemStack playerHead(OfflinePlayer owner, String displayName, List<String> lore) {
        ItemStack head = Materials.stack("PLAYER_HEAD", 1);
        if (head == null) return null;
        ItemMeta meta = head.getItemMeta();
        if (meta instanceof SkullMeta) {
            SkullMeta skull = (SkullMeta) meta;
            Compat.setSkullOwner(skull, owner);
            if (displayName != null) skull.setDisplayName(Compat.translateColors(displayName));
            if (lore != null) {
                List<String> translated = new ArrayList<String>();
                for (String line : lore) translated.add(Compat.translateColors(line));
                skull.setLore(translated);
            }
            head.setItemMeta(skull);
        }
        return head;
    }

    private void consumeOne(Player player) {
        ItemStack hand = Compat.getMainHandItem(player);
        if (hand == null) return;
        if (hand.getAmount() <= 1) {
            Compat.setMainHandItem(player, null);
        } else {
            hand.setAmount(hand.getAmount() - 1);
            Compat.setMainHandItem(player, hand);
        }
        player.updateInventory();
    }

    private static int random(int bound) {
        return (int) (Math.random() * bound);
    }
}
