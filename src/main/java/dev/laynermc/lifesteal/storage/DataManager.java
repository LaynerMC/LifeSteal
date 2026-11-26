package dev.laynermc.lifesteal.storage;

import dev.laynermc.lifesteal.LifeStealPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory player store backed by data.yml. The whole map is the source of
 * truth while the server runs; it is persisted periodically, on disable and on
 * important state changes (eliminate / revive), so a per-death disk write is
 * avoided.
 */
public final class DataManager {

    private final LifeStealPlugin plugin;
    private final File file;
    private final YamlConfiguration yaml;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<UUID, PlayerData>();
    private int taskId = -1;

    public DataManager(LifeStealPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create data.yml: " + e.getMessage());
            }
        }
        this.yaml = YamlConfiguration.loadConfiguration(file);
        loadAll();
    }

    private void loadAll() {
        ConfigurationSection root = yaml.getConfigurationSection("players");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(key);
            if (s == null) continue;
            try {
                UUID uuid = UUID.fromString(key);
                PlayerData d = new PlayerData();
                d.name = s.getString("name", "");
                d.hearts = s.getDouble("hearts", 20.0);
                d.maxHearts = s.getDouble("maxHearts", -1.0);
                d.kills = s.getInt("kills", 0);
                d.deaths = s.getInt("deaths", 0);
                d.eliminations = s.getInt("eliminations", 0);
                d.revives = s.getInt("revives", 0);
                d.eliminated = s.getBoolean("eliminated", false);
                d.unbanTime = s.getLong("unbanTime", 0);
                cache.put(uuid, d);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    /** Returns the player's data, creating a fresh default entry if needed. */
    public PlayerData get(UUID uuid) {
        PlayerData d = cache.get(uuid);
        if (d == null) {
            d = new PlayerData();
            d.hearts = plugin.config().getDouble("hearts.starting", 10) * 2.0;
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            d.name = op.getName() == null ? "" : op.getName();
            cache.put(uuid, d);
        }
        return d;
    }

    public PlayerData get(OfflinePlayer player) {
        PlayerData d = get(player.getUniqueId());
        String n = player.getName();
        if (n != null && !n.equals(d.name)) d.name = n;
        return d;
    }

    public boolean isCached(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    public List<UUID> eliminatedPlayers() {
        List<UUID> list = new ArrayList<UUID>();
        for (Map.Entry<UUID, PlayerData> e : cache.entrySet()) {
            if (e.getValue().eliminated) list.add(e.getKey());
        }
        return list;
    }

    /** Top players by hearts, descending. */
    public List<Map.Entry<UUID, PlayerData>> topByHearts(int limit) {
        List<Map.Entry<UUID, PlayerData>> all = new ArrayList<Map.Entry<UUID, PlayerData>>(cache.entrySet());
        Collections.sort(all, new Comparator<Map.Entry<UUID, PlayerData>>() {
            public int compare(Map.Entry<UUID, PlayerData> a, Map.Entry<UUID, PlayerData> b) {
                return Double.compare(b.getValue().hearts, a.getValue().hearts);
            }
        });
        if (all.size() > limit) return all.subList(0, limit);
        return all;
    }

    public List<Map.Entry<UUID, PlayerData>> all() {
        return new ArrayList<Map.Entry<UUID, PlayerData>>(cache.entrySet());
    }

    public synchronized void saveAll() {
        yaml.set("players", null);
        ConfigurationSection root = yaml.createSection("players");
        for (Map.Entry<UUID, PlayerData> e : cache.entrySet()) {
            PlayerData d = e.getValue();
            ConfigurationSection s = root.createSection(e.getKey().toString());
            s.set("name", d.name);
            s.set("hearts", d.hearts);
            s.set("maxHearts", d.maxHearts);
            s.set("kills", d.kills);
            s.set("deaths", d.deaths);
            s.set("eliminations", d.eliminations);
            s.set("revives", d.revives);
            s.set("eliminated", d.eliminated);
            s.set("unbanTime", d.unbanTime);
        }
        try {
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not save data.yml: " + ex.getMessage());
        }
    }

    public void startAutoSave() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    public void run() {
                        saveAll();
                    }
                });
            }
        }, 600L, 600L); // 30s
    }

    public void shutdown() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        saveAll();
    }
}
