package dev.laynermc.lifesteal.compat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolves material names from the modern (post-1.13 "flattened") scheme to
 * whatever exists on the running server. On 1.8-1.12 the flattened names do not
 * exist, so a legacy mapping (material + data value) is used instead.
 */
public final class Materials {

    private Materials() {}

    /** modern name -> {legacy material name, legacy data} */
    private static final Map<String, String[]> LEGACY = new HashMap<String, String[]>();
    static {
        // dyes (1.8: INK_SACK + data)
        LEGACY.put("RED_DYE", new String[]{"INK_SACK", "1"});
        LEGACY.put("ORANGE_DYE", new String[]{"INK_SACK", "14"});
        LEGACY.put("YELLOW_DYE", new String[]{"INK_SACK", "11"});
        LEGACY.put("LIME_DYE", new String[]{"INK_SACK", "10"});
        LEGACY.put("GREEN_DYE", new String[]{"INK_SACK", "2"});
        LEGACY.put("CYAN_DYE", new String[]{"INK_SACK", "6"});
        LEGACY.put("LIGHT_BLUE_DYE", new String[]{"INK_SACK", "12"});
        LEGACY.put("BLUE_DYE", new String[]{"INK_SACK", "4"});
        LEGACY.put("MAGENTA_DYE", new String[]{"INK_SACK", "13"});
        LEGACY.put("PINK_DYE", new String[]{"INK_SACK", "9"});
        LEGACY.put("GRAY_DYE", new String[]{"INK_SACK", "8"});
        LEGACY.put("LIGHT_GRAY_DYE", new String[]{"INK_SACK", "7"});
        LEGACY.put("BLACK_DYE", new String[]{"INK_SACK", "0"});
        LEGACY.put("BROWN_DYE", new String[]{"INK_SACK", "3"});
        LEGACY.put("WHITE_DYE", new String[]{"INK_SACK", "15"});
        LEGACY.put("PURPLE_DYE", new String[]{"INK_SACK", "5"});
        // heads
        LEGACY.put("PLAYER_HEAD", new String[]{"SKULL_ITEM", "3"});
        LEGACY.put("CREEPER_HEAD", new String[]{"SKULL", "4"});
        LEGACY.put("ZOMBIE_HEAD", new String[]{"SKULL", "2"});
        LEGACY.put("SKELETON_SKULL", new String[]{"SKULL", "0"});
        LEGACY.put("WITHER_SKELETON_SKULL", new String[]{"SKULL", "1"});
        LEGACY.put("DRAGON_HEAD", new String[]{"SKULL", "5"});
        // items added after 1.8 -> fall back to a sensible pre-1.9 material
        LEGACY.put("NETHERITE_INGOT", new String[]{"DIAMOND", "0"});
        LEGACY.put("NETHERITE_BLOCK", new String[]{"DIAMOND_BLOCK", "0"});
        LEGACY.put("NETHERITE_SCRAP", new String[]{"DIAMOND", "0"});
        LEGACY.put("ANCIENT_DEBRIS", new String[]{"DIAMOND_ORE", "0"});
        LEGACY.put("TOTEM_OF_UNDYING", new String[]{"NETHER_STAR", "0"});
        LEGACY.put("SHULKER_SHELL", new String[]{"ENDER_PEARL", "0"});
        LEGACY.put("ELYTRA", new String[]{"DIAMOND_CHESTPLATE", "0"});
        LEGACY.put("CONDUIT", new String[]{"BEACON", "0"});
        LEGACY.put("HEART_OF_THE_SEA", new String[]{"NETHER_STAR", "0"});
        LEGACY.put("NAUTILUS_SHELL", new String[]{"ENDER_PEARL", "0"});
        LEGACY.put("PHANTOM_MEMBRANE", new String[]{"LEATHER", "0"});
        LEGACY.put("TRIDENT", new String[]{"DIAMOND_SWORD", "0"});
        LEGACY.put("TURTLE_HELMET", new String[]{"DIAMOND_HELMET", "0"});
        LEGACY.put("BEEHIVE", new String[]{"HAY_BLOCK", "0"});
        LEGACY.put("HONEY_BLOCK", new String[]{"SLIME_BLOCK", "0"});
        LEGACY.put("HONEY_BOTTLE", new String[]{"POTION", "0"});
        LEGACY.put("CROSSBOW", new String[]{"BOW", "0"});
        LEGACY.put("SWEET_BERRIES", new String[]{"APPLE", "0"});
        LEGACY.put("BAMBOO", new String[]{"SUGAR_CANE", "0"});
        LEGACY.put("CORNFLOWER", new String[]{"INK_SACK", "4"});
        LEGACY.put("LILY_OF_THE_VALLEY", new String[]{"INK_SACK", "15"});
        LEGACY.put("WITHER_ROSE", new String[]{"INK_SACK", "0"});
        LEGACY.put("BLUE_ORCHID", new String[]{"RED_ROSE", "1"});
        LEGACY.put("ALLIUM", new String[]{"RED_ROSE", "2"});
        LEGACY.put("AZURE_BLUET", new String[]{"RED_ROSE", "3"});
        LEGACY.put("RED_TULIP", new String[]{"RED_ROSE", "4"});
        LEGACY.put("ORANGE_TULIP", new String[]{"RED_ROSE", "5"});
        LEGACY.put("WHITE_TULIP", new String[]{"RED_ROSE", "6"});
        LEGACY.put("PINK_TULIP", new String[]{"RED_ROSE", "7"});
        LEGACY.put("OXEYE_DAISY", new String[]{"RED_ROSE", "8"});
        LEGACY.put("SUNFLOWER", new String[]{"DOUBLE_PLANT", "0"});
        LEGACY.put("LILAC", new String[]{"DOUBLE_PLANT", "1"});
        LEGACY.put("ROSE_BUSH", new String[]{"DOUBLE_PLANT", "4"});
        LEGACY.put("PEONY", new String[]{"DOUBLE_PLANT", "5"});
        LEGACY.put("CORAL_BLOCK", new String[]{"SANDSTONE", "0"});
        LEGACY.put("KELP", new String[]{"SUGAR_CANE", "0"});
        LEGACY.put("SEAGRASS", new String[]{"TALL_GRASS", "0"});
        LEGACY.put("SEA_PICKLE", new String[]{"INK_SACK", "5"});
        LEGACY.put("COMPOSTER", new String[]{"HAY_BLOCK", "0"});
        LEGACY.put("BARREL", new String[]{"CHEST", "0"});
        LEGACY.put("LOOM", new String[]{"CRAFTING_TABLE", "0"});
        LEGACY.put("SMOKER", new String[]{"FURNACE", "0"});
        LEGACY.put("BLAST_FURNACE", new String[]{"FURNACE", "0"});
        LEGACY.put("CARTOGRAPHY_TABLE", new String[]{"CRAFTING_TABLE", "0"});
        LEGACY.put("FLETCHING_TABLE", new String[]{"CRAFTING_TABLE", "0"});
        LEGACY.put("SMITHING_TABLE", new String[]{"CRAFTING_TABLE", "0"});
        LEGACY.put("STONECUTTER", new String[]{"CRAFTING_TABLE", "0"});
        LEGACY.put("GRINDSTONE", new String[]{"CRAFTING_TABLE", "0"});
        LEGACY.put("LECTERN", new String[]{"BOOKSHELF", "0"});
        LEGACY.put("BELL", new String[]{"GOLD_BLOCK", "0"});
        LEGACY.put("LANTERN", new String[]{"TORCH", "0"});
        LEGACY.put("SOUL_LANTERN", new String[]{"TORCH", "0"});
        LEGACY.put("CAMPFIRE", new String[]{"FIRE", "0"});
        LEGACY.put("SOUL_CAMPFIRE", new String[]{"FIRE", "0"});
        LEGACY.put("BEE_NEST", new String[]{"HAY_BLOCK", "0"});
        LEGACY.put("HONEYCOMB", new String[]{"SUGAR", "0"});
        LEGACY.put("HONEYCOMB_BLOCK", new String[]{"HAY_BLOCK", "0"});
        LEGACY.put("WARPED_FUNGUS", new String[]{"RED_MUSHROOM", "0"});
        LEGACY.put("CRIMSON_FUNGUS", new String[]{"BROWN_MUSHROOM", "0"});
        LEGACY.put("NETHER_SPROUTS", new String[]{"LONG_GRASS", "0"});
        LEGACY.put("WARPED_ROOTS", new String[]{"LONG_GRASS", "0"});
        LEGACY.put("CRIMSON_ROOTS", new String[]{"LONG_GRASS", "0"});
        LEGACY.put("WEEPING_VINES", new String[]{"VINE", "0"});
        LEGACY.put("TWISTING_VINES", new String[]{"VINE", "0"});
        LEGACY.put("SOUL_SOIL", new String[]{"SOUL_SAND", "0"});
        LEGACY.put("BASALT", new String[]{"OBSIDIAN", "0"});
        LEGACY.put("POLISHED_BASALT", new String[]{"OBSIDIAN", "0"});
        LEGACY.put("BLACKSTONE", new String[]{"OBSIDIAN", "0"});
        LEGACY.put("GILDED_BLACKSTONE", new String[]{"GOLD_ORE", "0"});
        LEGACY.put("CHAIN", new String[]{"IRON_BARS", "0"});
        LEGACY.put("SOUL_TORCH", new String[]{"TORCH", "0"});
        LEGACY.put("RESPAWN_ANCHOR", new String[]{"BEACON", "0"});
        LEGACY.put("CRYING_OBSIDIAN", new String[]{"OBSIDIAN", "0"});
        LEGACY.put("QUARTZ_BRICKS", new String[]{"QUARTZ_BLOCK", "0"});
        LEGACY.put("TARGET", new String[]{"HAY_BLOCK", "0"});
        LEGACY.put("AMETHYST_BLOCK", new String[]{"DIAMOND_BLOCK", "0"});
        LEGACY.put("AMETHYST_SHARD", new String[]{"DIAMOND", "0"});
        LEGACY.put("BUDDING_AMETHYST", new String[]{"DIAMOND_BLOCK", "0"});
        LEGACY.put("SPYGLASS", new String[]{"COMPASS", "0"});
        LEGACY.put("TINTED_GLASS", new String[]{"GLASS", "0"});
        LEGACY.put("POWDER_SNOW", new String[]{"SNOW", "0"});
        LEGACY.put("POWDER_SNOW_BUCKET", new String[]{"SNOW_BALL", "0"});
        LEGACY.put("COPPER_INGOT", new String[]{"IRON_INGOT", "0"});
        LEGACY.put("COPPER_BLOCK", new String[]{"IRON_BLOCK", "0"});
        LEGACY.put("LIGHTNING_ROD", new String[]{"IRON_INGOT", "0"});
        LEGACY.put("POINTED_DRIPSTONE", new String[]{"STONE", "0"});
        LEGACY.put("DRIPSTONE_BLOCK", new String[]{"STONE", "0"});
        LEGACY.put("AZALEA", new String[]{"SAPLING", "0"});
        LEGACY.put("FLOWERING_AZALEA", new String[]{"RED_ROSE", "0"});
        LEGACY.put("SPORE_BLOSSOM", new String[]{"RED_ROSE", "0"});
        LEGACY.put("BIG_DRIPLEAF", new String[]{"LILY_PAD", "0"});
        LEGACY.put("SMALL_DRIPLEAF", new String[]{"LILY_PAD", "0"});
        LEGACY.put("GLOW_BERRIES", new String[]{"APPLE", "0"});
        LEGACY.put("GLOW_LICHEN", new String[]{"VINE", "0"});
        LEGACY.put("HANGING_ROOTS", new String[]{"VINE", "0"});
        LEGACY.put("ROOTED_DIRT", new String[]{"DIRT", "0"});
        LEGACY.put("MOSS_BLOCK", new String[]{"GRASS", "0"});
        LEGACY.put("DEEPSLATE", new String[]{"STONE", "0"});
        LEGACY.put("DEEPSLATE_DIAMOND_ORE", new String[]{"DIAMOND_ORE", "0"});
        LEGACY.put("CALCITE", new String[]{"STONE", "0"});
        LEGACY.put("TUFF", new String[]{"STONE", "0"});
        LEGACY.put("AMETHYST_CLUSTER", new String[]{"DIAMOND", "0"});
        LEGACY.put("MUSIC_DISC_OTHERSIDE", new String[]{"GOLD_RECORD", "0"});
        LEGACY.put("DISC_FRAGMENT_5", new String[]{"GOLD_RECORD", "0"});
        LEGACY.put("GOAT_HORN", new String[]{"DIAMOND", "0"});
        LEGACY.put("OAK_CHEST_BOAT", new String[]{"BOAT", "0"});
        LEGACY.put("MANGROVE_PROPAGULE", new String[]{"SAPLING", "0"});
        LEGACY.put("MANGROVE_ROOTS", new String[]{"LOG", "0"});
        LEGACY.put("MUDDY_MANGROVE_ROOTS", new String[]{"LOG", "0"});
        LEGACY.put("MUD", new String[]{"DIRT", "0"});
        LEGACY.put("MUD_BRICKS", new String[]{"CLAY_BRICK", "0"});
        LEGACY.put("PACKED_MUD", new String[]{"DIRT", "0"});
        LEGACY.put("FROGLIGHT", new String[]{"SEA_LANTERN", "0"});
        LEGACY.put("FROGSPAWN", new String[]{"EGG", "0"});
        LEGACY.put("PEARLESCENT_FROGLIGHT", new String[]{"SEA_LANTERN", "0"});
        LEGACY.put("OCHRE_FROGLIGHT", new String[]{"SEA_LANTERN", "0"});
        LEGACY.put("VERDANT_FROGLIGHT", new String[]{"SEA_LANTERN", "0"});
        LEGACY.put("RECOVERY_COMPASS", new String[]{"COMPASS", "0"});
        LEGACY.put("ECHO_SHARD", new String[]{"DIAMOND", "0"});
        LEGACY.put("DISC_FRAGMENT", new String[]{"GOLD_RECORD", "0"});
        LEGACY.put("OAK_BOAT_WITH_CHEST", new String[]{"BOAT", "0"});
        LEGACY.put("BAMBOO_MOSAIC", new String[]{"WOOD", "0"});
        LEGACY.put("BAMBOO_PLANKS", new String[]{"WOOD", "0"});
        LEGACY.put("BUNDLE", new String[]{"CHEST", "0"});
        LEGACY.put("SPAWN_EGG", new String[]{"MONSTER_EGG", "0"});
        // also keep pre-flattened names working on modern servers by mapping to themselves
        // (they resolve directly on legacy; on modern matchMaterial won't find them so we leave as-is)
    }

    public static final class Resolved {
        public final Material material;
        public final short data;

        public Resolved(Material material, short data) {
            this.material = material;
            this.data = data;
        }
    }

    public static Resolved resolve(String name) {
        if (name == null) return null;
        String upper = name.toUpperCase();
        Material m = matchMaterial(upper);
        if (m != null) return new Resolved(m, (short) 0);
        m = getMaterial(upper);
        if (m != null) return new Resolved(m, (short) 0);
        String[] leg = LEGACY.get(upper);
        if (leg != null) {
            Material lm = getMaterial(leg[0]);
            if (lm != null) {
                short d = 0;
                try { d = Short.parseShort(leg[1]); } catch (NumberFormatException e) { d = 0; }
                return new Resolved(lm, d);
            }
        }
        return null;
    }

    public static ItemStack stack(String name, int amount) {
        Resolved r = resolve(name);
        if (r == null) return null;
        if (r.data == 0 || Version.hasMatchMaterial()) {
            return new ItemStack(r.material, Math.max(1, amount));
        }
        return new ItemStack(r.material, Math.max(1, amount), r.data);
    }

    private static Material matchMaterial(String name) {
        if (!Version.hasMatchMaterial()) return null;
        try {
            Method mm = Material.class.getMethod("matchMaterial", String.class);
            Object r = mm.invoke(null, name);
            return r == null ? null : (Material) r;
        } catch (Throwable e) {
            return null;
        }
    }

    private static Material getMaterial(String name) {
        try {
            Method m = Material.class.getMethod("getMaterial", String.class);
            Object r = m.invoke(null, name);
            return r == null ? null : (Material) r;
        } catch (Throwable e) {
            return null;
        }
    }
}
