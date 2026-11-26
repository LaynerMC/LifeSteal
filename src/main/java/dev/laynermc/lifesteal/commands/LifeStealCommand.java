package dev.laynermc.lifesteal.commands;

import dev.laynermc.lifesteal.LifeStealPlugin;
import dev.laynermc.lifesteal.storage.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Root /lifesteal (alias /ls) command dispatcher. No CommandAPI dependency.
 */
public final class LifeStealCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = Arrays.asList(
            "help", "hearts", "setheart", "addheart", "removeheart", "setmaxheart",
            "eliminate", "eliminateall", "revive", "reviveall", "reset", "resetall",
            "withdraw", "give", "bans", "reload");

    private final LifeStealPlugin plugin;

    public LifeStealCommand(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            plugin.messages().send(sender, "general.version", "version", plugin.getDescription().getVersion());
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help": return cmdHelp(sender);
            case "hearts": return cmdHearts(sender, args);
            case "setheart": return cmdSet(sender, args);
            case "addheart": return cmdAdd(sender, args);
            case "removeheart": return cmdRemove(sender, args);
            case "setmaxheart": return cmdSetMax(sender, args);
            case "eliminate": return cmdEliminate(sender, args);
            case "eliminateall": return cmdEliminateAll(sender);
            case "revive": return cmdRevive(sender, args);
            case "reviveall": return cmdReviveAll(sender);
            case "reset": return cmdReset(sender, args);
            case "resetall": return cmdResetAll(sender);
            case "withdraw": return cmdWithdraw(sender, args);
            case "give": return cmdGive(sender, args);
            case "bans": return cmdBans(sender, args);
            case "reload": return cmdReload(sender);
            default:
                plugin.messages().send(sender, "general.unknown-subcommand");
                return true;
        }
    }

    private boolean cmdHelp(CommandSender sender) {
        plugin.messages().send(sender, "general.help-header");
        line(sender, "hearts [player]", "Check hearts");
        line(sender, "setheart <player> <amount>", "Set hearts");
        line(sender, "addheart <player> <amount>", "Add hearts");
        line(sender, "removeheart <player> <amount>", "Remove hearts");
        line(sender, "setmaxheart <player> <amount|-1|reset>", "Set per-player max");
        line(sender, "withdraw <amount>", "Withdraw hearts into items");
        line(sender, "eliminate <player>", "Eliminate a player");
        line(sender, "eliminateall", "Eliminate everyone");
        line(sender, "revive <player>", "Revive a player");
        line(sender, "reviveall", "Revive everyone");
        line(sender, "reset <player>", "Reset hearts");
        line(sender, "resetall", "Reset everyone");
        line(sender, "give extra_heart|revive_tome <player> <amount> [chance]", "Give items");
        line(sender, "bans [remove <player>]", "List / unban eliminated");
        line(sender, "reload", "Reload config");
        return true;
    }

    private void line(CommandSender sender, String cmd, String desc) {
        plugin.messages().send(sender, "general.help-line", "cmd", cmd, "desc", desc);
    }

    private boolean cmdHearts(CommandSender sender, String[] args) {
        if (!has(sender, "lifesteal.health.get")) return true;
        if (args.length < 2) {
            if (!(sender instanceof Player)) return playerOnly(sender);
            plugin.messages().send(sender, "heart.check-self", "amount", String.valueOf(plugin.hearts().hearts((Player) sender)));
            return true;
        }
        OfflinePlayer target = resolve(args[1]);
        if (target == null) return notFound(sender);
        PlayerData d = plugin.data().get(target);
        plugin.messages().send(sender, "heart.check", "player", name(d, args[1]), "amount", String.valueOf(d.heartsInt()));
        return true;
    }

    private boolean cmdSet(CommandSender sender, String[] args) {
        if (!has(sender, "lifesteal.health.set")) return true;
        if (args.length < 3) return usage(sender, "/ls setheart <player> <amount>");
        OfflinePlayer target = resolve(args[1]);
        Integer amount = parseInt(args[2]);
        if (target == null) return notFound(sender);
        if (amount == null) return numberExpected(sender);
        PlayerData d = plugin.data().get(target);
        Player online = Bukkit.getPlayer(target.getUniqueId());
        plugin.hearts().setHeartsOffline(target.getUniqueId(), amount);
        if (plugin.hearts().isAtOrBelowMinimum(d)) {
            plugin.elimination().eliminate(target.getUniqueId(), online);
        }
        plugin.messages().send(sender, "heart.set-admin", "player", name(d, args[1]), "amount", String.valueOf(d.heartsInt()));
        if (online != null) plugin.messages().send(online, "heart.set", "amount", String.valueOf(d.heartsInt()));
        return true;
    }

    private boolean cmdAdd(CommandSender sender, String[] args) {
        if (!has(sender, "lifesteal.health.add")) return true;
        if (args.length < 3) return usage(sender, "/ls addheart <player> <amount>");
        OfflinePlayer target = resolve(args[1]);
        Integer amount = parseInt(args[2]);
        if (target == null) return notFound(sender);
        if (amount == null || amount < 1) return numberExpected(sender);
        PlayerData d = plugin.data().get(target);
        Player online = Bukkit.getPlayer(target.getUniqueId());
        double max = plugin.hearts().effectiveMaxHp(d, online);
        double newHp = d.hearts + amount * 2.0;
        if (max >= 0 && newHp > max) newHp = max;
        if (newHp < 0) newHp = 0;
        d.hearts = newHp;
        if (newHp > 0 && online != null) plugin.hearts().applyMaxHealth(online, newHp);
        plugin.messages().send(sender, "heart.added-admin", "player", name(d, args[1]), "amount", String.valueOf(amount));
        if (online != null) plugin.messages().send(online, "heart.added", "amount", String.valueOf(amount));
        return true;
    }

    private boolean cmdRemove(CommandSender sender, String[] args) {
        if (!has(sender, "lifesteal.health.remove")) return true;
        if (args.length < 3) return usage(sender, "/ls removeheart <player> <amount>");
        OfflinePlayer target = resolve(args[1]);
        Integer amount = parseInt(args[2]);
        if (target == null) return notFound(sender);
        if (amount == null || amount < 1) return numberExpected(sender);
        PlayerData d = plugin.data().get(target);
        Player online = Bukkit.getPlayer(target.getUniqueId());
        d.hearts = Math.max(0, d.hearts - amount * 2.0);
        if (d.hearts > 0 && online != null) plugin.hearts().applyMaxHealth(online, d.hearts);
        if (plugin.hearts().isAtOrBelowMinimum(d)) {
            plugin.elimination().eliminate(target.getUniqueId(), online);
        }
        plugin.messages().send(sender, "heart.removed-admin", "player", name(d, args[1]), "amount", String.valueOf(amount));
        if (online != null) plugin.messages().send(online, "heart.removed", "amount", String.valueOf(amount));
        return true;
    }

    private boolean cmdSetMax(CommandSender sender, String[] args) {
        if (!has(sender, "lifesteal.setmaxheart")) return true;
        if (args.length < 3) return usage(sender, "/ls setmaxheart <player> <amount|-1|reset>");
        OfflinePlayer target = resolve(args[1]);
        if (target == null) return notFound(sender);
        PlayerData d = plugin.data().get(target);
        if (args[2].equalsIgnoreCase("reset")) {
            plugin.hearts().clearMaxHearts(target.getUniqueId());
            plugin.messages().send(sender, "max-heart.cleared", "player", name(d, args[1]));
            return true;
        }
        Integer amount = parseInt(args[2]);
        if (amount == null) return numberExpected(sender);
        if (amount < 0) {
            // -1 means "no personal cap, use the global cap".
            plugin.hearts().clearMaxHearts(target.getUniqueId());
            plugin.messages().send(sender, "max-heart.cleared", "player", name(d, args[1]));
            return true;
        }
        plugin.hearts().setMaxHeartsOffline(target.getUniqueId(), amount);
        Player online = Bukkit.getPlayer(target.getUniqueId());
        if (online != null) plugin.hearts().sync(online);
        plugin.messages().send(sender, "max-heart.set", "player", name(d, args[1]), "amount", String.valueOf(amount));
        if (online != null) plugin.messages().send(online, "max-heart.set-self", "amount", String.valueOf(amount));
        return true;
    }

    private boolean cmdEliminate(CommandSender sender, String[] args) {
        if (!has(sender, "lifesteal.eliminate")) return true;
        if (args.length < 2) return usage(sender, "/ls eliminate <player>");
        OfflinePlayer target = resolve(args[1]);
        if (target == null) return notFound(sender);
        PlayerData d = plugin.data().get(target);
        if (d.eliminated) {
            plugin.messages().send(sender, "elimination.already-eliminated", "player", name(d, args[1]));
            return true;
        }
        plugin.elimination().eliminate(target.getUniqueId(), Bukkit.getPlayer(target.getUniqueId()));
        plugin.messages().send(sender, "elimination.you-eliminated", "player", name(d, args[1]));
        return true;
    }

    private boolean cmdEliminateAll(CommandSender sender) {
        if (!has(sender, "lifesteal.eliminateall")) return true;
        int count = plugin.elimination().eliminateAll();
        plugin.messages().send(sender, "elimination.all-eliminated");
        sender.sendMessage("Eliminated " + count + " player(s).");
        return true;
    }

    private boolean cmdRevive(CommandSender sender, String[] args) {
        if (!has(sender, "lifesteal.revive")) return true;
        if (args.length < 2) return usage(sender, "/ls revive <player>");
        OfflinePlayer target = resolve(args[1]);
        if (target == null) return notFound(sender);
        PlayerData d = plugin.data().get(target);
        if (!d.eliminated) {
            plugin.messages().send(sender, "revive.not-needed", "player", name(d, args[1]));
            return true;
        }
        Player reviver = sender instanceof Player ? (Player) sender : null;
        plugin.elimination().revive(target.getUniqueId(), reviver);
        plugin.messages().send(sender, "revive.success", "player", name(d, args[1]));
        return true;
    }

    private boolean cmdReviveAll(CommandSender sender) {
        if (!has(sender, "lifesteal.reviveall")) return true;
        int count = plugin.elimination().reviveAll();
        plugin.messages().send(sender, "revive.all-revived", "amount", String.valueOf(count));
        return true;
    }

    private boolean cmdReset(CommandSender sender, String[] args) {
        if (!has(sender, "lifesteal.reset")) return true;
        if (args.length < 2) return usage(sender, "/ls reset <player>");
        OfflinePlayer target = resolve(args[1]);
        if (target == null) return notFound(sender);
        PlayerData d = plugin.data().get(target);
        plugin.hearts().resetOffline(target.getUniqueId());
        Player online = Bukkit.getPlayer(target.getUniqueId());
        if (online != null) plugin.hearts().sync(online);
        plugin.messages().send(sender, "reset.done", "player", name(d, args[1]));
        return true;
    }

    private boolean cmdResetAll(CommandSender sender) {
        if (!has(sender, "lifesteal.resetall")) return true;
        for (Map.Entry<UUID, PlayerData> e : plugin.data().all()) {
            plugin.hearts().resetOffline(e.getKey());
            Player online = Bukkit.getPlayer(e.getKey());
            if (online != null) plugin.hearts().sync(online);
        }
        plugin.messages().send(sender, "reset.all-done");
        return true;
    }

    private boolean cmdWithdraw(CommandSender sender, String[] args) {
        if (!has(sender, "lifesteal.withdraw")) return true;
        if (!(sender instanceof Player)) return playerOnly(sender);
        if (!plugin.config().getBoolean("heart-item.withdraw-enabled", true)) {
            plugin.messages().send(sender, "withdraw.disabled");
            return true;
        }
        if (args.length < 2) return usage(sender, "/ls withdraw <amount>");
        Integer amount = parseInt(args[1]);
        if (amount == null || amount < 1) return numberExpected(sender);
        Player player = (Player) sender;
        PlayerData d = plugin.data().get(player);
        if (d.heartsInt() - amount < plugin.hearts().minimumHearts()) {
            plugin.messages().send(player, "withdraw.not-enough");
            return true;
        }
        ItemStack heartItem = plugin.items().createHeartItem(100);
        if (heartItem == null) {
            plugin.messages().send(player, "general.feature-disabled");
            return true;
        }
        heartItem.setAmount(amount);
        giveOrDrop(player, heartItem);
        boolean dead = plugin.hearts().removeHearts(player, amount);
        plugin.messages().send(player, "heart.withdrawn", "amount", String.valueOf(amount));
        plugin.effects().play(player, "withdraw");
        if (dead) plugin.elimination().eliminate(player);
        return true;
    }

    private boolean cmdGive(CommandSender sender, String[] args) {
        if (!has(sender, "lifesteal.give")) return true;
        if (args.length < 4) return usage(sender, "/ls give extra_heart|revive_tome <player> <amount> [chance]");
        String type = args[1].toLowerCase();
        OfflinePlayer target = resolve(args[2]);
        Integer amount = parseInt(args[3]);
        if (target == null) return notFound(sender);
        if (amount == null || amount < 1) return numberExpected(sender);
        Player online = Bukkit.getPlayer(target.getUniqueId());
        if (online == null) {
            plugin.messages().send(sender, "general.player-offline", "player", args[2]);
            return true;
        }
        ItemStack template;
        if ("extra_heart".equals(type)) {
            int chance = 100;
            if (args.length >= 5) {
                Integer c = parseInt(args[4]);
                if (c != null) chance = c;
            }
            template = plugin.items().createHeartItem(chance);
        } else if ("revive_tome".equals(type)) {
            template = plugin.items().createReviveTome();
        } else {
            return usage(sender, "/ls give extra_heart|revive_tome <player> <amount> [chance]");
        }
        if (template == null) {
            plugin.messages().send(sender, "general.feature-disabled");
            return true;
        }
        for (int i = 0; i < amount; i++) {
            giveOrDrop(online, template.clone());
        }
        plugin.messages().send(sender, "heart.added-admin", "player", online.getName(), "amount", String.valueOf(amount));
        return true;
    }

    private boolean cmdBans(CommandSender sender, String[] args) {
        if (!has(sender, "lifesteal.bans")) return true;
        if (args.length >= 2 && args[1].equalsIgnoreCase("remove")) {
            if (args.length < 3) return usage(sender, "/ls bans remove <player>");
            OfflinePlayer target = resolve(args[2]);
            if (target == null) return notFound(sender);
            PlayerData d = plugin.data().get(target);
            if (!d.eliminated) {
                plugin.messages().send(sender, "elimination.player-not-eliminated", "player", name(d, args[2]));
                return true;
            }
            Player reviver = sender instanceof Player ? (Player) sender : null;
            plugin.elimination().revive(target.getUniqueId(), reviver);
            plugin.messages().send(sender, "revive.success", "player", name(d, args[2]));
            return true;
        }
        List<UUID> list = plugin.data().eliminatedPlayers();
        if (list.isEmpty()) {
            plugin.messages().send(sender, "elimination.list-empty");
            return true;
        }
        plugin.messages().send(sender, "elimination.list-header");
        for (UUID u : list) {
            PlayerData d = plugin.data().get(u);
            plugin.messages().send(sender, "elimination.list-entry", "player", name(d, u.toString()));
        }
        return true;
    }

    private boolean cmdReload(CommandSender sender) {
        if (!has(sender, "lifesteal.reload")) return true;
        plugin.recipes().unregister();
        plugin.reloadConfigFiles();
        plugin.recipes().register();
        plugin.messages().send(sender, "reload.config");
        plugin.messages().send(sender, "reload.recipes");
        return true;
    }

    // ------------------------------------------------------------------
    private boolean has(CommandSender sender, String perm) {
        if (sender.hasPermission(perm)) return true;
        plugin.messages().send(sender, "general.no-permission");
        return false;
    }

    private boolean usage(CommandSender sender, String u) {
        plugin.messages().send(sender, "general.usage", "usage", u);
        return true;
    }

    private boolean notFound(CommandSender sender) {
        plugin.messages().send(sender, "general.player-not-found");
        return true;
    }

    private boolean numberExpected(CommandSender sender) {
        plugin.messages().send(sender, "general.number-expected");
        return true;
    }

    private boolean playerOnly(CommandSender sender) {
        plugin.messages().send(sender, "general.player-only");
        return true;
    }

    private Integer parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String name(PlayerData d, String fallback) {
        return (d.name == null || d.name.isEmpty()) ? fallback : d.name;
    }

    private OfflinePlayer resolve(String name) {
        if (name == null) return null;
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online;
        for (Map.Entry<UUID, PlayerData> e : plugin.data().all()) {
            if (e.getValue().name != null && e.getValue().name.equalsIgnoreCase(name)) {
                return Bukkit.getOfflinePlayer(e.getKey());
            }
        }
        return null;
    }

    private void giveOrDrop(Player player, ItemStack item) {
        if (item == null) return;
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        for (ItemStack stack : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), stack);
        }
        player.updateInventory();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return filter(SUBS, args[0]);
        String sub = args[0].toLowerCase();
        if (args.length == 2) {
            if ("give".equals(sub)) return filter(Arrays.asList("extra_heart", "revive_tome"), args[1]);
            if ("bans".equals(sub)) return filter(Arrays.asList("remove"), args[1]);
            if (needsPlayer(sub)) return onlineNames(args[1]);
        }
        if (args.length == 3 && "give".equals(sub)) {
            return onlineNames(args[2]);
        }
        return new ArrayList<String>();
    }

    private boolean needsPlayer(String sub) {
        return sub.equals("hearts") || sub.equals("setheart") || sub.equals("addheart")
                || sub.equals("removeheart") || sub.equals("setmaxheart") || sub.equals("eliminate")
                || sub.equals("revive") || sub.equals("reset");
    }

    private List<String> onlineNames(String prefix) {
        List<String> names = new ArrayList<String>();
        String p = prefix == null ? "" : prefix.toLowerCase();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName() != null && player.getName().toLowerCase().startsWith(p)) {
                names.add(player.getName());
            }
        }
        return names;
    }

    private List<String> filter(List<String> options, String prefix) {
        List<String> out = new ArrayList<String>();
        String p = prefix == null ? "" : prefix.toLowerCase();
        for (String o : options) {
            if (o.toLowerCase().startsWith(p)) out.add(o);
        }
        return out;
    }
}
