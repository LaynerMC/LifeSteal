package dev.laynermc.lifesteal;

import dev.laynermc.lifesteal.commands.HeartTopCommand;
import dev.laynermc.lifesteal.commands.LifeStealCommand;
import dev.laynermc.lifesteal.combat.CombatManager;
import dev.laynermc.lifesteal.compat.Version;
import dev.laynermc.lifesteal.config.Messages;
import dev.laynermc.lifesteal.config.PluginConfig;
import dev.laynermc.lifesteal.display.DisplayManager;
import dev.laynermc.lifesteal.hearts.EliminationManager;
import dev.laynermc.lifesteal.hearts.HeartManager;
import dev.laynermc.lifesteal.items.ItemManager;
import dev.laynermc.lifesteal.items.MenuManager;
import dev.laynermc.lifesteal.items.Recipes;
import dev.laynermc.lifesteal.listeners.*;
import dev.laynermc.lifesteal.storage.DataManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * LifeSteal 1.0.0 - by laynermc.
 *
 * A multi-version LifeSteal SMP plugin compiled against the Spigot 1.8.8 API so a
 * single jar runs on every server from 1.8.8 up to 26.3. Every post-1.8 API call
 * goes through {@link dev.laynermc.lifesteal.compat.Compat}.
 */
public final class LifeStealPlugin extends JavaPlugin {

    private PluginConfig config;
    private Messages messages;
    private DataManager data;
    private HeartManager hearts;
    private EliminationManager elimination;
    private Effects effects;
    private ItemManager items;
    private MenuManager menus;
    private Recipes recipes;
    private CombatManager combat;
    private DisplayManager display;

    @Override
    public void onEnable() {
        config = new PluginConfig(this);
        messages = new Messages(this);
        data = new DataManager(this);
        effects = new Effects(this);
        hearts = new HeartManager(this);
        elimination = new EliminationManager(this);
        items = new ItemManager(this);
        menus = new MenuManager(this);
        recipes = new Recipes(this);
        combat = new CombatManager(this);
        display = new DisplayManager(this);

        registerListeners();
        registerCommands();
        recipes.register();
        data.startAutoSave();
        combat.start();
        display.start();

        getLogger().info("LifeSteal v" + getDescription().getVersion() + " by laynermc enabled.");
        getLogger().info("Server: " + Version.get() + " (compat major " + Version.major() + ").");
    }

    @Override
    public void onDisable() {
        if (combat != null) combat.stop();
        if (display != null) display.stop();
        if (recipes != null) recipes.unregister();
        if (data != null) data.shutdown();
        getLogger().info("LifeSteal disabled.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new QuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PreLoginListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);
    }

    private void registerCommands() {
        LifeStealCommand cmd = new LifeStealCommand(this);
        if (getCommand("lifesteal") != null) {
            getCommand("lifesteal").setExecutor(cmd);
            getCommand("lifesteal").setTabCompleter(cmd);
        }
        if (getCommand("hearttop") != null) {
            getCommand("hearttop").setExecutor(new HeartTopCommand(this));
        }
    }

    public void reloadConfigFiles() {
        config.load();
        messages.load();
    }

    public PluginConfig config() { return config; }
    public Messages messages() { return messages; }
    public DataManager data() { return data; }
    public HeartManager hearts() { return hearts; }
    public EliminationManager elimination() { return elimination; }
    public Effects effects() { return effects; }
    public ItemManager items() { return items; }
    public MenuManager menus() { return menus; }
    public Recipes recipes() { return recipes; }
    public CombatManager combat() { return combat; }
    public DisplayManager display() { return display; }
}
