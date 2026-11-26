package dev.laynermc.lifesteal;

import dev.laynermc.lifesteal.compat.Compat;
import dev.laynermc.lifesteal.compat.Sounds;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Plays the configured sound + particle for a given effect key. Both are looked
 * up by name and silently skipped when unavailable on the running version.
 */
public final class Effects {

    private final LifeStealPlugin plugin;

    public Effects(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    public void play(Player p, String key) {
        if (p == null) return;
        String sound = plugin.config().getString("effects." + key + ".sound", "");
        String particle = plugin.config().getString("effects." + key + ".particle", "");
        Sounds.play(p, sound, 1f, 1f);
        if (particle != null && !particle.isEmpty()) {
            Location loc = p.getLocation().add(0, 1, 0);
            Compat.playParticle(p.getWorld(), loc, particle, 12);
        }
    }

    public void playAt(World w, Location loc, String key) {
        if (w == null || loc == null) return;
        String sound = plugin.config().getString("effects." + key + ".sound", "");
        String particle = plugin.config().getString("effects." + key + ".particle", "");
        if (particle != null && !particle.isEmpty()) {
            Compat.playParticle(w, loc, particle, 12);
        }
        if (sound != null && !sound.isEmpty()) {
            for (Player p : w.getPlayers()) {
                try {
                    if (p.getLocation().distanceSquared(loc) < 256) Sounds.play(p, sound, 1f, 1f);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }
}
