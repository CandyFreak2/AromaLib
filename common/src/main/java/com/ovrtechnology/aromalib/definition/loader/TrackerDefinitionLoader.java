package com.ovrtechnology.aromalib.definition.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.data.ClasspathDataSource;
import com.ovrtechnology.aromalib.data.DataSource;
import com.ovrtechnology.aromalib.data.JsonResources;
import com.ovrtechnology.aromalib.definition.TrackerDefinition;
import com.ovrtechnology.aromalib.registry.BiomeRegistry;
import com.ovrtechnology.aromalib.registry.BlockRegistry;
import com.ovrtechnology.aromalib.registry.StructureRegistry;
import com.ovrtechnology.aromalib.tracker.item.ItemUnlock;
import lombok.Getter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads nose definitions from JSON files.
 * Parses the data/aromaaffect/noses/ directory for nose definitions.
 * Handles texture validation and fallback to foragers_nose texture.
 * 
 * Note: Recipes are defined separately in data/aromaaffect/recipe/ as standard Minecraft recipe JSON files.
 */
public class TrackerDefinitionLoader {
    
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    
    /**
     * Default texture path for fallback
     */
    private static final String DEFAULT_TEXTURE = "item/foragers_nose";
    
    /**
     * Default model (iron helmet) for fallback
     */
    private static final String DEFAULT_MODEL = "minecraft:iron_helmet";
    
    /**
     * Cached list of loaded nose definitions
     */
    @Getter
    private static List<TrackerDefinition> loadedItems = new ArrayList<>();
    
    /**
     * Load all nose definitions from the noses directory.
     * This scans for JSON files and parses each one.
     */
    public static List<TrackerDefinition> loadAll() {
        return loadAll(ClasspathDataSource.INSTANCE);
    }

    public static List<TrackerDefinition> loadAll(DataSource dataSource) {
        loadedItems.clear();

        // Load the index file that lists all nose definition files
        try {
            TrackerDefinition[] items = loadAllFromResource(dataSource, "data/aromaaffect/noses/noses.json");
            if (items != null) {
                for (TrackerDefinition def : items) {
                    if (def != null && def.isValid()) {
                        validateAndApplyFallbacks(def);
                        
                        loadedItems.add(def);
                        AromaLib.LOGGER.info("Loaded tracker definition: {}", def.getId());
                    } else {
                        AromaLib.LOGGER.warn("Invalid tracker definition found, skipping...");
                    }
                }
            }
        } catch (Exception e) {
            AromaLib.LOGGER.error("Failed to load tracker definitions", e);
        }
        
        AromaLib.LOGGER.info("Loaded {} tracker definitions", loadedItems.size());
        return Collections.unmodifiableList(loadedItems);
    }

    public static void reloadInPlace(DataSource dataSource) {
        TrackerDefinition[] newDefs = loadAllFromResource(dataSource, "data/aromaaffect/noses/noses.json");
        int mutated = 0;
        int skipped = 0;
        for (TrackerDefinition src : newDefs) {
            if (src == null || !src.isValid()) continue;
            TrackerDefinition target = findLoadedById(src.getId());
            if (target == null) {
                AromaLib.LOGGER.warn(
                        "[Nose reload] ID '{}' is not a built-in nose; custom noses must use the aromaaffect:custom_nose variant system.",
                        src.getId());
                skipped++;
                continue;
            }
            copyFields(src, target);
            validateAndApplyFallbacks(target);
            mutated++;
        }
        AromaLib.LOGGER.info("Nose reload: mutated {} definitions in place, skipped {} unknown IDs", mutated, skipped);
    }

    private static TrackerDefinition findLoadedById(String id) {
        for (TrackerDefinition d : loadedItems) {
            if (id.equals(d.getId())) return d;
        }
        return null;
    }

    private static void copyFields(TrackerDefinition src, TrackerDefinition dst) {
        dst.setImage(src.getImage());
        dst.setModel(src.getModel());
        dst.setUnlock(src.getUnlock());
        dst.setDurability(src.getDurability());
        dst.setRepair(src.getRepair());
        dst.setTier(src.getTier());
        dst.setTrackCost(src.getTrackCost());
    }

    /**
     * Validate nose definition and apply fallbacks for missing textures/models
     */
    private static void validateAndApplyFallbacks(TrackerDefinition def) {
        String id = def.getId();
        
        // Check texture
        String texturePath = def.getImage();
        if (texturePath == null || texturePath.isEmpty()) {
            AromaLib.LOGGER.warn("[{}] No texture defined, using fallback: {}", id, DEFAULT_TEXTURE);
            def.setImage(DEFAULT_TEXTURE);
        } else if (!textureExists(texturePath)) {
            AromaLib.LOGGER.warn("[{}] Texture '{}' not found, using fallback: {}", id, texturePath, DEFAULT_TEXTURE);
            def.setImage(DEFAULT_TEXTURE);
        }
        
        // Check model - default to iron_helmet if not specified
        String model = def.getModel();
        if (model == null || model.isEmpty()) {
            AromaLib.LOGGER.info("[{}] No model defined, using default: {}", id, DEFAULT_MODEL);
        }
        
        // Validate unlock references
        ItemUnlock unlock = def.getUnlock();
        if (unlock != null) {
            // Validate nose inheritance
            if (unlock.hasNoseInheritance()) {
                for (String inheritedNoseId : unlock.getNoses()) {
                    AromaLib.LOGGER.debug("[{}] Inherits abilities from: {}", id, inheritedNoseId);
                }
            }
            
            // Validate and filter block references against BlockRegistry
            if (unlock.hasBlockUnlocks() && BlockRegistry.isInitialized()) {
                List<String> invalidBlocks = BlockRegistry.validateBlockIds(unlock.getBlocks());
                if (!invalidBlocks.isEmpty()) {
                    for (String invalidBlock : invalidBlocks) {
                        AromaLib.LOGGER.warn("[{}] Skipping unregistered block: '{}' - block must be defined in blocks.json", 
                                id, invalidBlock);
                    }
                    // Filter out invalid blocks
                    List<String> validBlocks = new ArrayList<>(unlock.getBlocks());
                    validBlocks.removeAll(invalidBlocks);
                    unlock.setBlocks(validBlocks);
                    AromaLib.LOGGER.info("[{}] Kept {} valid block references after filtering", id, validBlocks.size());
                }
            }
            
            // Validate and filter structure references against StructureRegistry
            if (unlock.hasStructureUnlocks() && StructureRegistry.isInitialized()) {
                List<String> invalidStructures = StructureRegistry.validateStructureIds(unlock.getStructures());
                if (!invalidStructures.isEmpty()) {
                    for (String invalidStructure : invalidStructures) {
                        AromaLib.LOGGER.warn("[{}] Skipping unregistered structure: '{}' - structure must be defined in structures.json", 
                                id, invalidStructure);
                    }
                    // Filter out invalid structures
                    List<String> validStructures = new ArrayList<>(unlock.getStructures());
                    validStructures.removeAll(invalidStructures);
                    unlock.setStructures(validStructures);
                    AromaLib.LOGGER.info("[{}] Kept {} valid structure references after filtering", id, validStructures.size());
                }
            }
            
            // Validate and filter biome references against BiomeRegistry
            if (unlock.hasBiomeUnlocks() && BiomeRegistry.isInitialized()) {
                List<String> invalidBiomes = BiomeRegistry.validateBiomeIds(unlock.getBiomes());
                if (!invalidBiomes.isEmpty()) {
                    for (String invalidBiome : invalidBiomes) {
                        AromaLib.LOGGER.warn("[{}] Skipping unregistered biome: '{}' - biome must be defined in biomes.json", 
                                id, invalidBiome);
                    }
                    // Filter out invalid biomes
                    List<String> validBiomes = new ArrayList<>(unlock.getBiomes());
                    validBiomes.removeAll(invalidBiomes);
                    unlock.setBiomes(validBiomes);
                    AromaLib.LOGGER.info("[{}] Kept {} valid biome references after filtering", id, validBiomes.size());
                }
            }
            
            // Validate and filter ability references against AbilityDefinitionLoader
            // @TODO
//            if (unlock.hasAbilityUnlocks() && AbilityDefinitionLoader.isInitialized()) {
//                List<String> invalidAbilities = AbilityDefinitionLoader.validateAbilityIds(unlock.getAbilities());
//                if (!invalidAbilities.isEmpty()) {
//                    for (String invalidAbility : invalidAbilities) {
//                        AromaLib.LOGGER.warn("[{}] Skipping unregistered ability: '{}' - ability must be defined in abilities.json",
//                                id, invalidAbility);
//                    }
//                    // Filter out invalid abilities
//                    List<String> validAbilities = new ArrayList<>(unlock.getAbilities());
//                    validAbilities.removeAll(invalidAbilities);
//                    unlock.setAbilities(validAbilities);
//                    AromaLib.LOGGER.info("[{}] Kept {} valid ability references after filtering", id, validAbilities.size());
//                }
//            }
        }
    }
    
    /**
     * Check if a texture file exists in the resources
     */
    private static boolean textureExists(String texturePath) {
        // Convert texture path to full resource path
        // texturePath is like "item/foragers_nose", full path is "assets/aromaaffect/textures/item/foragers_nose.png"
        String fullPath = "assets/aromaaffect/textures/" + texturePath + ".png";
        
        try (InputStream stream = TrackerDefinitionLoader.class.getClassLoader().getResourceAsStream(fullPath)) {
            return stream != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Load nose definitions from a specific JSON resource file
     */
    private static TrackerDefinition[] loadAllFromResource(DataSource dataSource, String resourcePath) {
        JsonElement element = dataSource.read(resourcePath);
        if (element == null) {
            AromaLib.LOGGER.warn("Nose definitions file not found: {}", resourcePath);
            return new TrackerDefinition[0];
        }
        return JsonResources.parseArrayOrWrapped(element, "noses", TrackerDefinition[].class, GSON, resourcePath);
    }
    
    /**
     * Parse a single definition from a JSON string
     */
    public static TrackerDefinition parseFromJson(String json) {
        try {
            return GSON.fromJson(json, TrackerDefinition.class);
        } catch (Exception e) {
            AromaLib.LOGGER.error("Failed to parse nose definition from JSON", e);
            return null;
        }
    }
    
    /**
     * Get a definition by ID
     */
    public static TrackerDefinition getByID(String id) {
        for (TrackerDefinition def : loadedItems) {
            if (def.getId().equals(id)) {
                return def;
            }
        }
        return null;
    }
    
    /**
     * Get all trackers by a specific tier
     */
    public static List<TrackerDefinition> getItemsByTier(int tier) {
        List<TrackerDefinition> result = new ArrayList<>();
        for (TrackerDefinition def : loadedItems) {
            if (def.getTier() == tier) {
                result.add(def);
            }
        }
        return result;
    }
    
    /**
     * Serialize a nose definition to JSON (useful for debugging/export)
     */
    public static String toJson(TrackerDefinition nose) {
        return GSON.toJson(nose);
    }
    
    /**
     * Get the GSON instance for external use
     */
    public static Gson getGson() {
        return GSON;
    }
}
