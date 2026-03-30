package com.example.itemhunt.util;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

public class ItemDataManager {
    
    private static final ItemDataManager INSTANCE = new ItemDataManager();
    
    // Color keywords to detect in item IDs
    private static final Set<String> COLOR_KEYWORDS = Set.of(
        "red", "green", "blue", "yellow", "purple", "pink", "gray", "brown",
        "black", "white", "orange", "cyan", "lime", "light_blue", "light_gray",
        "magenta", "dark_", "crimson", "warped"
    );
    
    // Color mapping for related terms (single color)
    private static final Map<String, String> COLOR_ALIASES;
    
    // Multi-color mappings (item matches multiple colors)
    private static final Map<String, Set<String>> MULTI_COLOR_ALIASES;
    
    static {
        Map<String, String> map = new HashMap<>();
        
        // Coral colors
        map.put("tube_coral", "blue");
        map.put("brain_coral", "pink");
        map.put("bubble_coral", "purple");
        map.put("fire_coral", "red");
        map.put("horn_coral", "yellow");
        
        // Wood types
        map.put("oak", "brown");
        map.put("spruce", "brown");
        map.put("birch", "white");
        map.put("jungle", "brown");
        map.put("acacia", "orange");
        map.put("dark_oak", "brown");
        map.put("cherry", "pink");
        map.put("pale_oak", "white"); // Also gray - handled separately
        map.put("pale_moss", "gray");
        
        // Bamboo
        map.put("bamboo_block", "green");
        map.put("bamboo_mosaic", "yellow");
        map.put("bamboo_planks", "yellow");
        map.put("bamboo", "yellow"); // Bamboo items (not sugar_cane)
        
        // Stone types
        map.put("tuff", "gray");
        map.put("andesite", "gray");
        map.put("gravel", "gray");
        map.put("granite", "brown");
        map.put("diorite", "white");
        map.put("deepslate", "black");
        
        // Mud and bricks
        map.put("mud", "brown");
        map.put("brick", "red");
        map.put("bricks", "red");
        
        // Prismarine
        map.put("prismarine", "cyan");
        
        // Nether blocks
        map.put("nether_brick", "black");
        map.put("nether_bricks", "black");
        map.put("netherrack", "red");
        map.put("red_nether_brick", "red");
        map.put("blackstone", "black");
        
        // Copper variants
        map.put("copper", "orange");
        map.put("exposed", "brown");
        map.put("waxed", "orange");
        map.put("waxed_exposed", "brown");
        
        // Special blocks
        map.put("crying_obsidian", "purple");
        map.put("dead", "gray");
        map.put("mangrove", "red"); // Also brown - handled separately
        
        // Weathered/Oxidized - multi-color, handled separately
        // map.put("weathered", ...);
        // map.put("oxidized", ...);
        
        // Other common mappings
        map.put("warped", "cyan");
        map.put("nether_wart", "red");
        map.put("blaze", "yellow");
        map.put("gold", "yellow");
        map.put("golden", "yellow");
        map.put("diamond", "cyan");
        map.put("emerald", "green");
        map.put("lapis", "blue");
        map.put("sea", "cyan");
        map.put("ocean", "blue");
        map.put("redstone", "red");
        map.put("slime", "green");
        map.put("lime", "green");
        map.put("grass", "green");
        map.put("vine", "green");
        map.put("leaf", "green");
        map.put("leaves", "green");
        map.put("moss", "green");
        map.put("sculk", "cyan");
        map.put("amethyst", "purple");
        map.put("chorus", "purple");
        map.put("end", "purple");
        map.put("ender", "purple");
        map.put("shulker", "purple");
        map.put("honey", "yellow");
        map.put("honeycomb", "yellow");
        map.put("bee", "yellow");
        map.put("glowstone", "yellow");
        map.put("glow", "yellow");
        map.put("fire", "orange");
        map.put("magma", "orange");
        map.put("lava", "orange");
        map.put("torch", "orange");
        map.put("lantern", "orange");
        map.put("campfire", "orange");
        map.put("pumpkin", "orange");
        map.put("carrot", "orange");
        map.put("melon", "green");
        map.put("cactus", "green");
        map.put("kelp", "green");
        map.put("seagrass", "green");
        map.put("turtle", "cyan");
        map.put("axolotl", "cyan");
        map.put("dolphin", "cyan");
        map.put("guardian", "cyan");
        map.put("obsidian", "black");
        map.put("basalt", "black");
        map.put("coal", "black");
        map.put("charcoal", "black");
        map.put("ink", "black");
        map.put("wither", "black");
        map.put("dragon", "black");
        map.put("iron", "white");
        map.put("snow", "white");
        map.put("ice", "cyan");
        map.put("quartz", "white");
        map.put("bone", "white");
        map.put("skeleton", "white");
        map.put("wood", "brown");
        map.put("log", "brown");
        map.put("plank", "brown");
        map.put("dirt", "brown");
        map.put("sand", "yellow");
        map.put("stone", "gray");
        map.put("cobblestone", "gray");
        map.put("terracotta", "brown");
        map.put("clay", "blue");
        map.put("apple", "red");
        map.put("beef", "red");
        map.put("salmon", "red");
        map.put("tropical_fish", "orange");
        map.put("pufferfish", "orange");
        map.put("cod", "brown");
        map.put("mushroom", "brown");
        map.put("brown_mushroom", "brown");
        map.put("red_mushroom", "red");
        map.put("netherite", "black");
        map.put("ancient_debris", "black");
        map.put("fox", "orange");
        map.put("pig", "pink");
        map.put("cow", "brown");
        map.put("sheep", "white");
        map.put("chicken", "white");
        map.put("rabbit", "brown");
        map.put("wolf", "white");
        map.put("cat", "orange");
        map.put("ocelot", "yellow");
        map.put("horse", "brown");
        map.put("llama", "brown");
        map.put("donkey", "gray");
        map.put("mule", "brown");
        map.put("panda", "black");
        map.put("polar_bear", "white");
        map.put("goat", "white");
        map.put("frog", "green");
        map.put("tadpole", "brown");
        map.put("camel", "brown");
        map.put("sniffer", "green");
        map.put("armadillo", "brown");
        map.put("phantom", "gray");
        map.put("vex", "white");
        map.put("vindicator", "gray");
        map.put("evoker", "gray");
        map.put("pillager", "gray");
        map.put("illusioner", "gray");
        map.put("witch", "green");
        map.put("ravager", "brown");
        map.put("villager", "brown");
        map.put("wandering_trader", "white");
        map.put("zombie", "green");
        map.put("drowned", "cyan");
        map.put("husk", "brown");
        map.put("stray", "cyan");
        map.put("bogged", "brown");
        map.put("wither_skeleton", "black");
        map.put("spider", "black");
        map.put("cave_spider", "blue");
        map.put("enderman", "purple");
        map.put("endermite", "purple");
        map.put("blaze", "yellow");
        map.put("ghast", "white");
        map.put("magma_cube", "red");
        map.put("creeper", "green");
        map.put("silverfish", "gray");
        map.put("ender_dragon", "purple");
        map.put("warden", "cyan");
        map.put("allay", "white");
        map.put("breeze", "white");
        map.put("egg", "white");
        map.put("feather", "white");
        map.put("leather", "brown");
        map.put("wool", "white");
        map.put("string", "white");
        map.put("paper", "white");
        map.put("book", "brown");
        map.put("wheat", "yellow");
        map.put("hay", "yellow");
        map.put("cake", "brown");
        map.put("cookie", "brown");
        map.put("bread", "brown");
        map.put("potato", "brown");
        map.put("beetroot", "red");
        map.put("sweet_berries", "red");
        map.put("glow_berries", "yellow");
        map.put("cocoa", "brown");
        map.put("sugar", "white");
        map.put("wheat_seeds", "green");
        map.put("pumpkin_seeds", "orange");
        map.put("melon_seeds", "brown");
        map.put("beetroot_seeds", "brown");
        map.put("torchflower_seeds", "orange");
        map.put("pitcher_pod", "green");
        map.put("pitcher_plant", "cyan");
        map.put("torchflower", "orange");
        map.put("spore_blossom", "pink");
        map.put("flower", "pink");
        map.put("dandelion", "yellow");
        map.put("poppy", "red");
        map.put("blue_orchid", "blue");
        map.put("allium", "purple");
        map.put("azure_bluet", "white");
        map.put("red_tulip", "red");
        map.put("orange_tulip", "orange");
        map.put("white_tulip", "white");
        map.put("pink_tulip", "pink");
        map.put("oxeye_daisy", "white");
        map.put("cornflower", "blue");
        map.put("lily_of_the_valley", "white");
        map.put("wither_rose", "black");
        map.put("sunflower", "yellow");
        map.put("lilac", "purple");
        map.put("rose_bush", "red");
        map.put("peony", "pink");
        map.put("pink_petals", "pink");
        map.put("coral", "pink");
        map.put("sea_pickle", "green");
        map.put("heart_of_the_sea", "cyan");
        map.put("nautilus_shell", "white");
        map.put("scute", "cyan");
        map.put("phantom_membrane", "gray");
        map.put("rabbit_foot", "brown");
        map.put("rabbit_hide", "brown");
        map.put("gunpowder", "gray");
        map.put("flint", "black");
        map.put("echo_shard", "cyan");
        map.put("recovery_compass", "cyan");
        map.put("compass", "white");
        map.put("clock", "yellow");
        map.put("spyglass", "brown");
        map.put("shears", "white");
        map.put("lead", "brown");
        map.put("name_tag", "yellow");
        map.put("saddle", "brown");
        map.put("experience_bottle", "yellow");
        map.put("dragon_breath", "purple");
        map.put("totem_of_undying", "yellow");
        map.put("nether_star", "yellow");
        map.put("enchanted_golden_apple", "yellow");
        map.put("golden_apple", "yellow");
        map.put("chorus_fruit", "purple");
        map.put("popped_chorus_fruit", "purple");
        map.put("blaze_rod", "yellow");
        map.put("blaze_powder", "yellow");
        map.put("magma_cream", "orange");
        map.put("ghast_tear", "cyan");
        map.put("ender_pearl", "cyan");
        map.put("ender_eye", "cyan");
        map.put("nether_quartz", "white");
        map.put("amethyst_shard", "purple");
        map.put("honeycomb", "yellow");
        map.put("honey_bottle", "yellow");
        map.put("honey_block", "yellow");
        map.put("shulker_shell", "purple");
        map.put("trident", "cyan");
        map.put("breeze_rod", "white");
        map.put("wind_charge", "white");
        map.put("ominous_bottle", "purple");
        map.put("ominous_trial_key", "cyan");
        map.put("trial_key", "cyan");
        map.put("heavy_core", "gray");
        map.put("mace", "gray");
        map.put("wolf_armor", "brown");
        map.put("brush", "brown");
        map.put("pottery_shard", "brown");
        map.put("pottery_sherd", "brown");
        map.put("porkchop", "pink");
        map.put("cooked_porkchop", "white");
        map.put("mutton", "red");
        map.put("cooked_mutton", "brown");
        map.put("chicken" , "white");
        map.put("potion", "blue");
        map.put("sugar_cane", "green");
        map.put("sand", "yellow");
        map.put("grass_block", "green");
        map.put("lily_pad", "green");
        map.put("purpur", "purple");
        map.put("sandstone", "yellow");
        map.put("end_stone", "yellow");
        map.put("basalt", "gray");
        map.put("heavy_weighted", "white");
        map.put("light_weighted", "yellow");
        map.put("bell", "yellow");
        map.put("scaffolding", "yellow");
        map.put("sandstone", "yellow");
        
        
        COLOR_ALIASES = Collections.unmodifiableMap(map);
        
        // Multi color aliases
        Map<String, Set<String>> multiMap = new HashMap<>();
        multiMap.put("mangrove", Set.of("red", "brown"));
        multiMap.put("pale_oak", Set.of("white", "gray"));
        multiMap.put("weathered", Set.of("green", "cyan"));
        multiMap.put("oxidized", Set.of("green", "cyan"));
        multiMap.put("waxed_weathered", Set.of("green", "cyan"));
        multiMap.put("waxed_oxidized", Set.of("green", "cyan"));
        multiMap.put("sea_lantern", Set.of("white", "blue"));
        multiMap.put("crimson", Set.of("red", "purple"));

        MULTI_COLOR_ALIASES = Collections.unmodifiableMap(multiMap);
    }
    
    private ItemDataManager() {
        System.out.println("[ItemHunt] ItemDataManager initialized with dynamic item detection");
    }
    
    public static ItemDataManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Check if an item ID starts with the given letter (English mode)
     * Dynamic: checks the actual item ID from registry
     */
    public boolean matchesEnglishLetter(String itemId, char letter) {
        if (itemId == null || itemId.isEmpty()) return false;
        
        // Extract item name from itemId (e.g., "minecraft:pumpkin" -> "pumpkin")
        String itemName = itemId.toLowerCase();
        if (itemId.contains(":")) {
            itemName = itemId.substring(itemId.indexOf(":") + 1).toLowerCase();
        }
        
        char lowerLetter = Character.toLowerCase(letter);
        
        // Special handling for minecart items
        // Items like hopper_minecart, tnt_minecart, furnace_minecart, chest_minecart
        // have IDs that don't start with 'm' but display names start with "Minecart"
        if (lowerLetter == 'm' && itemName.contains("minecart")) {
            return true;
        }
        
        // Check if item name starts with the letter
        return !itemName.isEmpty() && itemName.charAt(0) == lowerLetter;
    }
    
    /**
     * Check if an item matches the given pinyin (Chinese mode)
     * Uses the item ID as a fallback since we can't easily convert Chinese to pinyin dynamically
     */
    public boolean matchesPinyin(String itemId, String pinyin) {
        if (itemId == null || itemId.isEmpty()) return false;
        
        // Extract item name from itemId
        String itemName = itemId.toLowerCase();
        if (itemId.contains(":")) {
            itemName = itemId.substring(itemId.indexOf(":") + 1).toLowerCase();
        }
        
        // For Chinese mode, we check if the item name starts with the pinyin letter
        // This is a simplified approach - in a full implementation you'd want a Chinese->Pinyin converter
        if (itemName.isEmpty()) return false;
        
        char firstChar = itemName.charAt(0);
        return String.valueOf(firstChar).equalsIgnoreCase(pinyin);
    }
    
    /**
     * Check if an item has the given color (Color mode)
     * Dynamic: checks the item ID for color keywords and aliases
     */
    public boolean matchesColor(String itemId, String targetColor) {
        if (itemId == null || itemId.isEmpty()) return false;
        
        String lowerItemId = itemId.toLowerCase();
        String itemName = lowerItemId;
        if (lowerItemId.contains(":")) {
            itemName = lowerItemId.substring(lowerItemId.indexOf(":") + 1);
        }
        
        String targetLower = targetColor.toLowerCase();
        
        // Direct color name in item ID
        if (itemName.contains(targetLower)) {
            return true;
        }
        
        // Check for color banner, bed, carpet, etc. with the color prefix
        if (itemName.startsWith(targetLower + "_")) {
            return true;
        }
        
        // Check multi-color aliases first (for weathered, oxidized, etc.)
        for (Map.Entry<String, Set<String>> entry : MULTI_COLOR_ALIASES.entrySet()) {
            if (entry.getValue().contains(targetLower) && itemName.contains(entry.getKey())) {
                return true;
            }
        }
        
        // Check single color aliases
        for (Map.Entry<String, String> entry : COLOR_ALIASES.entrySet()) {
            if (entry.getValue().equals(targetLower) && itemName.contains(entry.getKey())) {
                return true;
            }
        }
        
        // Special handling for stained/colored variants
        // e.g., "red_wool", "blue_stained_glass", etc.
        String[] parts = itemName.split("_");
        for (String part : parts) {
            if (part.equals(targetLower)) {
                return true;
            }
            // Check aliases for each part
            String aliasColor = COLOR_ALIASES.get(part);
            if (aliasColor != null && aliasColor.equals(targetLower)) {
                return true;
            }
            // Check multi-color aliases for each part
            Set<String> multiColors = MULTI_COLOR_ALIASES.get(part);
            if (multiColors != null && multiColors.contains(targetLower)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get all item IDs that match a given letter (English mode)
     * Iterates through the item registry dynamically
     */
    public List<String> getEnglishItemsForLetter(char letter) {
        List<String> items = new ArrayList<>();
        char lowerLetter = Character.toLowerCase(letter);
        
        for (Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);
            String itemName = id.getPath();
            
            if (!itemName.isEmpty() && Character.toLowerCase(itemName.charAt(0)) == lowerLetter) {
                items.add(id.toString());
            }
        }
        
        return items;
    }
    
    /**
     * Get all item IDs that match a given color (Color mode)
     * Iterates through the item registry dynamically
     */
    public List<String> getColorItems(String color) {
        List<String> items = new ArrayList<>();
        
        for (Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);
            String itemId = id.toString();
            
            if (matchesColor(itemId, color)) {
                items.add(itemId);
            }
        }
        
        return items;
    }
}
