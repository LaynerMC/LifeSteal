package dev.laynermc.lifesteal.config;

import dev.laynermc.lifesteal.LifeStealPlugin;
import dev.laynermc.lifesteal.compat.Compat;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Loads messages.yml, merging in any new keys, and provides colour-translated,
 * placeholder-filled lookups. A message set to '' is treated as silenced and
 * {@link #get} returns "" (so {@link #send} sends nothing).
 */
public final class Messages {

    private final LifeStealPlugin plugin;
    private YamlConfiguration yaml;

    public Messages(LifeStealPlugin plugin) {
        this.plugin = plugin;
        plugin.saveResource("messages.yml", false);
        load();
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        yaml = YamlConfiguration.loadConfiguration(file);
        try (InputStreamReader r = new InputStreamReader(plugin.getResource("messages.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration def = YamlConfiguration.loadConfiguration(r);
            for (String key : def.getKeys(true)) {
                if (!yaml.contains(key)) yaml.set(key, def.get(key));
            }
            yaml.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not merge messages.yml defaults: " + e.getMessage());
        }
    }

    /** Raw, unformatted value (or "" if missing / silenced). */
    public String raw(String path) {
        String s = yaml.getString(path);
        return s == null ? "" : s;
    }

    /** Colour-translated message with placeholders applied; "" when silenced. */
    public String get(String path, String... kv) {
        String s = yaml.getString(path);
        if (s == null || s.isEmpty()) return "";
        return Compat.translateColors(format(s, kv));
    }

    public static String format(String template, String... kv) {
        if (template == null) return "";
        String result = template;
        for (int i = 0; i + 1 < kv.length; i += 2) {
            result = result.replace("${" + kv[i] + "}", kv[i + 1]);
        }
        return result;
    }

    public void send(CommandSender sender, String path, String... kv) {
        String msg = get(path, kv);
        if (!msg.isEmpty()) sender.sendMessage(msg);
    }
}
