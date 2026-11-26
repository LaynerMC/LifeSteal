package dev.laynermc.lifesteal.config;

import dev.laynermc.lifesteal.LifeStealPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Loads config.yml, copying the bundled default when missing and merging in any
 * new keys that a older file on disk does not yet contain.
 */
public final class PluginConfig {

    private final LifeStealPlugin plugin;
    private YamlConfiguration yaml;

    public PluginConfig(LifeStealPlugin plugin) {
        this.plugin = plugin;
        plugin.saveResource("config.yml", false);
        load();
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "config.yml");
        yaml = YamlConfiguration.loadConfiguration(file);
        try (InputStreamReader r = new InputStreamReader(plugin.getResource("config.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration def = YamlConfiguration.loadConfiguration(r);
            for (String key : def.getKeys(true)) {
                if (!yaml.contains(key)) yaml.set(key, def.get(key));
            }
            yaml.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not merge config.yml defaults: " + e.getMessage());
        }
    }

    public int getInt(String path, int def) {
        return yaml.getInt(path, def);
    }

    public int getInt(String path) {
        return yaml.getInt(path, 0);
    }

    public boolean getBoolean(String path, boolean def) {
        return yaml.getBoolean(path, def);
    }

    public boolean getBoolean(String path) {
        return yaml.getBoolean(path, false);
    }

    public String getString(String path, String def) {
        String s = yaml.getString(path);
        return s == null ? def : s;
    }

    public String getString(String path) {
        return yaml.getString(path);
    }

    public double getDouble(String path, double def) {
        return yaml.getDouble(path, def);
    }

    public double getDouble(String path) {
        return yaml.getDouble(path, 0);
    }

    public List<String> getStringList(String path) {
        return yaml.getStringList(path);
    }

    public YamlConfiguration raw() {
        return yaml;
    }
}
