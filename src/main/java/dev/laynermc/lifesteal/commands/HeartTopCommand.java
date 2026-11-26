package dev.laynermc.lifesteal.commands;

import dev.laynermc.lifesteal.LifeStealPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Opens the heart leaderboard GUI.
 */
public final class HeartTopCommand implements CommandExecutor {

    private final LifeStealPlugin plugin;

    public HeartTopCommand(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.messages().send(sender, "general.player-only");
            return true;
        }
        plugin.menus().openLeaderboard((Player) sender, 0);
        return true;
    }
}
