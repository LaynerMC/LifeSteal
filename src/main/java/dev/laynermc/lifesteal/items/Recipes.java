package dev.laynermc.lifesteal.items;

import dev.laynermc.lifesteal.LifeStealPlugin;
import dev.laynermc.lifesteal.compat.Materials;
import dev.laynermc.lifesteal.compat.Version;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Cross-version crafting-recipe registration. On 1.12+ a keyed
 * {@code ShapedRecipe(NamespacedKey, ItemStack)} is built through reflection; on
 * 1.8-1.11 the legacy no-key constructor is used directly. Removal scans
 * {@link Bukkit#recipeIterator()} so it works on every version.
 */
public final class Recipes {

    private static final char[] LETTERS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};

    private final LifeStealPlugin plugin;

    public Recipes(LifeStealPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        if (!plugin.config().getBoolean("recipes.enabled", true)) return;
        registerOne("heart_item", plugin.items().createHeartItem(plugin.config().getInt("heart-item.success-chance", 100)));
        registerOne("revive_tome", plugin.items().createReviveTome());
    }

    private void registerOne(String key, ItemStack result) {
        if (result == null) return;
        if (!plugin.config().getBoolean("recipes." + key + ".enabled", true)) return;
        List<String> items = plugin.config().getStringList("recipes." + key + ".items");
        if (items == null || items.size() != 9) return;
        if (Version.hasNamespacedKey()) {
            registerKeyed(key, result, items);
        } else {
            registerLegacy(result, items);
        }
    }

    private void registerKeyed(String key, ItemStack result, List<String> items) {
        try {
            Class<?> nskClass = Class.forName("org.bukkit.NamespacedKey");
            Object namespacedKey = nskClass.getConstructor(org.bukkit.plugin.Plugin.class, String.class)
                    .newInstance(plugin, key);
            Constructor<?> c = ShapedRecipe.class.getConstructor(nskClass, ItemStack.class);
            Object recipe = c.newInstance(namespacedKey, result);
            Method shape = recipe.getClass().getMethod("shape", String[].class);
            shape.invoke(recipe, (Object) new String[]{"ABC", "DEF", "GHI"});
            for (int i = 0; i < 9; i++) {
                Materials.Resolved m = Materials.resolve(items.get(i));
                if (m == null) continue;
                setIngredient(recipe, LETTERS[i], m.material);
            }
            Bukkit.addRecipe((Recipe) recipe);
        } catch (Throwable t) {
            plugin.getLogger().warning("Could not register recipe '" + key + "': " + t.getMessage());
        }
    }

    private void setIngredient(Object recipe, char letter, Material material) {
        try {
            Method m = recipe.getClass().getMethod("setIngredient", char.class, Material.class);
            m.invoke(recipe, letter, material);
            return;
        } catch (NoSuchMethodException ignored) {
        } catch (Throwable ignored) {
            return;
        }
        // Fallback for versions where only RecipeChoice exists (1.13+).
        try {
            Class<?> choiceClass = Class.forName("org.bukkit.inventory.RecipeChoice$MaterialChoice");
            Constructor<?> cc = choiceClass.getConstructor(Material.class);
            Object choice = cc.newInstance(material);
            Class<?> recipeChoiceClass = Class.forName("org.bukkit.inventory.RecipeChoice");
            Method m = recipe.getClass().getMethod("setIngredient", char.class, recipeChoiceClass);
            m.invoke(recipe, letter, choice);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Legacy 1.8-1.11 path. This method is only ever invoked when the server
     * predates NamespacedKey, so the deprecated no-key constructor (removed on
     * modern versions) is never resolved on those versions.
     */
    private void registerLegacy(ItemStack result, List<String> items) {
        try {
            ShapedRecipe recipe = new ShapedRecipe(result);
            recipe.shape("ABC", "DEF", "GHI");
            for (int i = 0; i < 9; i++) {
                Materials.Resolved m = Materials.resolve(items.get(i));
                if (m == null) continue;
                recipe.setIngredient(LETTERS[i], m.material);
            }
            Bukkit.addRecipe(recipe);
        } catch (Throwable t) {
            plugin.getLogger().warning("Could not register legacy recipe: " + t.getMessage());
        }
    }

    public void unregister() {
        try {
            Iterator<Recipe> it = Bukkit.recipeIterator();
            while (it.hasNext()) {
                Recipe r = it.next();
                ItemStack res = r.getResult();
                if (plugin.items().isHeartItem(res) || plugin.items().isReviveTome(res)) {
                    it.remove();
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("Could not unregister recipes: " + t.getMessage());
        }
    }
}
