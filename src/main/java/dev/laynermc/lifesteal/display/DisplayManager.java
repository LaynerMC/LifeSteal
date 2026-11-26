package dev.laynermc.lifesteal.display;

import dev.laynermc.lifesteal.LifeStealPlugin;
import dev.laynermc.lifesteal.compat.Compat;
import dev.laynermc.lifesteal.storage.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Persistent heart information display. ACTIONBAR sends a periodic action-bar
 * line to each player; SCOREBOARD assigns a shared sidebar objective that ranks
 * every online player by heart count. The boss bar is reserved for the combat
 * logger. Default mode is NONE.
 */
public final class DisplayManager {

    private final LifeStealPlugin plugin;
    private final String mode;
    private Scoreboard board;
    private Objective objective;
    private final Set<String> tracked = new HashSet<String>();
    private int taskId = -1;

    public DisplayManager(LifeStealPlugin plugin) {
        this.plugin = plugin;
        String m = plugin.config().getString("display.mode", "NONE");
        this.mode = m == null ? "NONE" : m.toUpperCase();
    }

    public void start() {
        if ("NONE".equals(mode)) return;
        if ("SCOREBOARD".equals(mode)) initScoreboard();
        int interval = plugin.config().getInt("display.interval", 5) * 20;
        if (interval < 20) interval = 20;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                refresh();
            }
        }, interval, interval);
    }

    public void stop() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
    }

    public void show(Player p) {
        if ("SCOREBOARD".equals(mode) && board != null) {
            p.setScoreboard(board);
        }
    }

    public void hide(Player p) {
        // the player's scoreboard is dropped automatically on quit
    }

    private void initScoreboard() {
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        if (sm == null) return;
        board = sm.getNewScoreboard();
        objective = board.registerNewObjective("lifesteal", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(Compat.translateColors(plugin.config().getString("display.scoreboard.title", "&c&lLifeSteal")));
    }

    private void refresh() {
        if ("ACTIONBAR".equals(mode)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                refreshActionBar(p);
            }
        } else if ("SCOREBOARD".equals(mode) && board != null) {
            for (String name : tracked) {
                board.resetScores(name);
            }
            tracked.clear();
            for (Player p : Bukkit.getOnlinePlayers()) {
                String name = p.getName();
                if (name == null) continue;
                PlayerData d = plugin.data().get(p);
                objective.getScore(name).setScore(d.heartsInt());
                tracked.add(name);
                p.setScoreboard(board);
            }
        }
    }

    private void refreshActionBar(Player p) {
        PlayerData d = plugin.data().get(p);
        int hearts = d.heartsInt();
        int max = plugin.hearts().effectiveMaxHearts(d, p);
        String maxStr = max < 0 ? "INF" : String.valueOf(max);
        String text = plugin.config().getString("display.actionbar-text", "&cHearts &f${hearts}&7/&e${max}");
        String filled = text.replace("${hearts}", String.valueOf(hearts))
                .replace("${max}", maxStr)
                .replace("${kills}", String.valueOf(d.kills))
                .replace("${deaths}", String.valueOf(d.deaths));
        Compat.sendActionBar(p, filled);
    }
}
