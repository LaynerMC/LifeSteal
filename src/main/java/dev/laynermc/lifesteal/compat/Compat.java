package dev.laynermc.lifesteal.compat;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reflection-backed compatibility shim. Every call site that touches an API
 * added after 1.8.8 goes through here, so the plugin (compiled against the
 * 1.8.8 Spigot API) loads and runs on every server from 1.8.8 up to 26.3.
 *
 * Each helper degrades gracefully: on a version that lacks the feature it
 * silently no-ops (or falls back to the legacy equivalent) instead of throwing.
 */
public final class Compat {

    private Compat() {}

    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");

    // ------------------------------------------------------------------
    //  Colour translation (& codes + &#RRGGBB hex on 1.16+)
    // ------------------------------------------------------------------
    public static String translateColors(String text) {
        if (text == null) return "";
        if (Version.hasHexColors()) {
            Matcher m = HEX.matcher(text);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String repl = hexOf("#" + m.group(1));
                m.appendReplacement(sb, Matcher.quoteReplacement(repl == null ? "" : repl));
            }
            m.appendTail(sb);
            text = sb.toString();
        } else {
            text = HEX.matcher(text).replaceAll("");
        }
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }

    private static String hexOf(String hex) {
        try {
            Class<?> cc = Class.forName("net.md_5.bungee.api.ChatColor");
            Method of = cc.getMethod("of", String.class);
            Object result = of.invoke(null, hex);
            return result == null ? null : result.toString();
        } catch (Throwable e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    //  Player skull owner (setOwningPlayer on 1.12+, setOwner before)
    // ------------------------------------------------------------------
    public static void setSkullOwner(org.bukkit.inventory.meta.ItemMeta meta, OfflinePlayer player) {
        if (meta == null || player == null) return;
        try {
            Method m = meta.getClass().getMethod("setOwningPlayer", OfflinePlayer.class);
            m.invoke(meta, player);
            return;
        } catch (Throwable ignored) {
        }
        try {
            Method m = meta.getClass().getMethod("setOwner", String.class);
            m.invoke(meta, player.getName());
        } catch (Throwable ignored) {
        }
    }

    // ------------------------------------------------------------------
    //  Main hand item (getItemInMainHand on 1.9+, getItemInHand before)
    // ------------------------------------------------------------------
    public static ItemStack getMainHandItem(Player p) {
        try {
            Method m = p.getInventory().getClass().getMethod("getItemInMainHand");
            return (ItemStack) m.invoke(p.getInventory());
        } catch (Throwable e) {
            return p.getInventory().getItemInHand();
        }
    }

    public static void setMainHandItem(Player p, ItemStack item) {
        try {
            Method m = p.getInventory().getClass().getMethod("setItemInMainHand", ItemStack.class);
            m.invoke(p.getInventory(), item);
        } catch (Throwable e) {
            p.getInventory().setItemInHand(item);
        }
    }

    // ------------------------------------------------------------------
    //  Action bar (Spigot API, present on every version 1.8.8 - 26.3).
    //  Done fully through reflection so the plugin does not need bungeecord-chat
    //  at compile time (the server provides it at runtime).
    // ------------------------------------------------------------------
    public static void sendActionBar(Player p, String message) {
        if (p == null || message == null || message.isEmpty()) return;
        try {
            Object spigot = p.getClass().getMethod("spigot").invoke(p);
            if (spigot == null) return;
            Class<?> chatMsgType = Class.forName("net.md_5.bungee.api.ChatMessageType");
            Object actionBar = chatMsgType.getField("ACTION_BAR").get(null);
            Class<?> baseCompClass = Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            Class<?> textCompClass = Class.forName("net.md_5.bungee.api.chat.TextComponent");
            Object comp = textCompClass.getConstructor(String.class).newInstance(translateColors(message));
            Object array = java.lang.reflect.Array.newInstance(baseCompClass, 1);
            java.lang.reflect.Array.set(array, 0, comp);
            Method send = spigot.getClass().getMethod("sendMessage", chatMsgType, array.getClass());
            send.invoke(spigot, actionBar, array);
        } catch (Throwable ignored) {
        }
    }

    // ------------------------------------------------------------------
    //  Particles (org.bukkit.Particle, 1.9+)
    // ------------------------------------------------------------------
    public static void playParticle(World world, Location loc, String particleName, int count) {
        if (world == null || loc == null || particleName == null || particleName.isEmpty()) return;
        if (!Version.atLeast(9)) return;
        try {
            Class<?> particleClass = Class.forName("org.bukkit.Particle");
            Object particle = particleClass.getMethod("valueOf", String.class).invoke(null, particleName.toUpperCase());
            if (particle == null) return;
            Method spawn = world.getClass().getMethod("spawnParticle", particleClass, Location.class, int.class);
            spawn.invoke(world, particle, loc, count);
        } catch (Throwable ignored) {
        }
    }

    // ------------------------------------------------------------------
    //  Boss bar (1.9+). Returns null on older versions or on any failure.
    // ------------------------------------------------------------------
    public static BossBarHandle createBossBar(String title, String color, String style) {
        if (!Version.hasBossBar()) return null;
        try {
            Class<?> barColorClass = Class.forName("org.bukkit.boss.BarColor");
            Class<?> barStyleClass = Class.forName("org.bukkit.boss.BarStyle");
            Object c = barColorClass.getMethod("valueOf", String.class).invoke(null, color.toUpperCase());
            Object s = barStyleClass.getMethod("valueOf", String.class).invoke(null, style.toUpperCase());
            Method create = org.bukkit.Bukkit.class.getMethod("createBossBar", String.class, barColorClass, barStyleClass);
            Object bar = create.invoke(null, translateColors(title), c, s);
            return bar == null ? null : new BossBarHandle(bar);
        } catch (Throwable e) {
            return null;
        }
    }

    /** Thin reflective wrapper around an org.bukkit.boss.BossBar instance. */
    public static final class BossBarHandle {
        private final Object bar;

        BossBarHandle(Object bar) {
            this.bar = bar;
        }

        public void setTitle(String title) {
            try { bar.getClass().getMethod("setTitle", String.class).invoke(bar, title); } catch (Throwable ignored) {}
        }

        public void setProgress(double progress) {
            try { bar.getClass().getMethod("setProgress", double.class).invoke(bar, progress); } catch (Throwable ignored) {}
        }

        public void setColor(String color) {
            try {
                Class<?> ec = Class.forName("org.bukkit.boss.BarColor");
                Object e = ec.getMethod("valueOf", String.class).invoke(null, color.toUpperCase());
                bar.getClass().getMethod("setColor", ec).invoke(bar, e);
            } catch (Throwable ignored) {}
        }

        public void setStyle(String style) {
            try {
                Class<?> ec = Class.forName("org.bukkit.boss.BarStyle");
                Object e = ec.getMethod("valueOf", String.class).invoke(null, style.toUpperCase());
                bar.getClass().getMethod("setStyle", ec).invoke(bar, e);
            } catch (Throwable ignored) {}
        }

        public void addPlayer(Player p) {
            try { bar.getClass().getMethod("addPlayer", Player.class).invoke(bar, p); } catch (Throwable ignored) {}
        }

        public void removePlayer(Player p) {
            try { bar.getClass().getMethod("removePlayer", Player.class).invoke(bar, p); } catch (Throwable ignored) {}
        }

        public void setVisible(boolean visible) {
            try { bar.getClass().getMethod("setVisible", boolean.class).invoke(bar, visible); } catch (Throwable ignored) {}
        }

        public void removeAll() {
            try { bar.getClass().getMethod("removeAll").invoke(bar); } catch (Throwable ignored) {}
        }
    }
}
