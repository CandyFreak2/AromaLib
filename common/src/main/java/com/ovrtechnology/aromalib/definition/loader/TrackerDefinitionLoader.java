package com.ovrtechnology.aromalib.definition.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.data.ClasspathDataSource;
import com.ovrtechnology.aromalib.data.DataSource;
import com.ovrtechnology.aromalib.definition.StructureDefinition;
import com.ovrtechnology.aromalib.definition.TrackerDefinition;
import com.ovrtechnology.aromalib.registry.BiomeRegistry;
import com.ovrtechnology.aromalib.registry.BlockRegistry;
import com.ovrtechnology.aromalib.registry.StructureRegistry;
import com.ovrtechnology.aromalib.tracker.item.ItemUnlock;
import lombok.Getter;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.util.*;

public class TrackerDefinitionLoader {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static final String DIRECTORY = "items";
    private static final String DEFAULT_TEXTURE = "item/foragers_nose";
    private static final String DEFAULT_MODEL = "minecraft:iron_helmet";

    @Getter
    private static List<TrackerDefinition> loadedItems = new ArrayList<>();

    private static Set<String> loadedIds = new HashSet<>();

    @Getter
    private static List<String> validationWarnings = new ArrayList<>();

    public static List<TrackerDefinition> loadAll() {
        return loadAll(ClasspathDataSource.INSTANCE);
    }

    public static List<TrackerDefinition> loadAll(DataSource dataSource) {
        loadedItems.clear();
        loadedIds.clear();
        validationWarnings.clear();

        Map<Identifier, JsonElement> files = dataSource.listJson(DIRECTORY);
        for (Map.Entry<Identifier, JsonElement> entry : files.entrySet()) {
            try {
                TrackerDefinition def = GSON.fromJson(entry.getValue(), TrackerDefinition.class);
                process(def);
            } catch (Exception e) {
                AromaLib.LOGGER.error("Failed to parse tracker {}: {}", entry.getKey(), e.getMessage());
            }
        }

        AromaLib.LOGGER.info("Loaded {} tracker definitions", loadedItems.size());
        return Collections.unmodifiableList(loadedItems);
    }

    private static void process(TrackerDefinition def) {
        if (def == null) {
            addWarning("Null tracker definition found, skipping...");
            return;
        }

        if (!def.isValid()) {
            addWarning("Invalid tracker definition found, skipping...");
            return;
        }

        String id = def.getId();

        if (loadedIds.contains(id)) {
            addWarning("Duplicate id '" + id + "' found, skipping...");
            return;
        }

        validate(def);

        loadedIds.add(id);
        loadedItems.add(def);
        AromaLib.LOGGER.debug("Loaded {} trackers", loadedItems.size());
    }

    /*
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
        AromaLib.LOGGER.info("Tracker Item reload: mutated {} definitions in place, skipped {} unknown IDs", mutated, skipped);
     */

    private static TrackerDefinition findLoadedById(String id) {
        for (TrackerDefinition d : loadedItems) {
            if (id.equals(d.getId())) return d;
        }
        return null;
    }

    private static void copyFields(TrackerDefinition src, TrackerDefinition dst) {
        dst.setTexture(src.getTexture());
        dst.setModel(src.getModel());
        dst.setUnlock(src.getUnlock());
        dst.setDurability(src.getDurability());
        dst.setRepair(src.getRepair());
        dst.setTier(src.getTier());
        dst.setTrackCost(src.getTrackCost());
    }

    private static void validate(TrackerDefinition def) {
        String id = def.getId();

        String texturePath = def.getTexture();
        if (texturePath == null || texturePath.isEmpty()) {
            AromaLib.LOGGER.warn("[{}] No texture defined, using fallback: {}", id, DEFAULT_TEXTURE);
            def.setTexture(DEFAULT_TEXTURE);
        } else if (!textureExists(texturePath)) {
            AromaLib.LOGGER.warn("[{}] Texture '{}' not found, using fallback: {}", id, texturePath, DEFAULT_TEXTURE);
            def.setTexture(DEFAULT_TEXTURE);
        }

        String model = def.getModel();
        if (model == null || model.isEmpty()) {
            AromaLib.LOGGER.info("[{}] No model defined, using default: {}", id, DEFAULT_MODEL);
        }

        ItemUnlock unlock = def.getUnlock();
        if (unlock != null) {
            if (unlock.isChild()) {
                for (String parent : unlock.getTrackers()) {
                    AromaLib.LOGGER.debug("[{}] Inherits abilities from: {}", id, parent);
                }
            }

            if (unlock.hasBlockUnlocks() && BlockRegistry.isInitialized()) {
                List<String> invalidBlocks = BlockRegistry.validateBlockIds(unlock.getBlocks());
                if (!invalidBlocks.isEmpty()) {
                    for (String invalidBlock : invalidBlocks) {
                        AromaLib.LOGGER.warn("[{}] Skipping unregistered block: '{}' - block must be defined in blocks.json",
                                id, invalidBlock);
                    }
                    List<String> validBlocks = new ArrayList<>(unlock.getBlocks());
                    validBlocks.removeAll(invalidBlocks);
                    unlock.setBlocks(validBlocks);
                    AromaLib.LOGGER.info("[{}] Kept {} valid block references after filtering", id, validBlocks.size());
                }
            }

            if (unlock.hasStructureUnlocked() && StructureRegistry.isInitialized()) {
                List<String> invalidStructures = StructureRegistry.validateStructureIds(unlock.getStructures());
                if (!invalidStructures.isEmpty()) {
                    for (String invalidStructure : invalidStructures) {
                        AromaLib.LOGGER.warn("[{}] Skipping unregistered structure: '{}' - structure must be defined in structures.json",
                                id, invalidStructure);
                    }
                    List<String> validStructures = new ArrayList<>(unlock.getStructures());
                    validStructures.removeAll(invalidStructures);
                    unlock.setStructures(validStructures);
                    AromaLib.LOGGER.info("[{}] Kept {} valid structure references after filtering", id, validStructures.size());
                }
            }

            if (unlock.hasBiomeUnlocked() && BiomeRegistry.isInitialized()) {
                List<String> invalidBiomes = BiomeRegistry.validateBiomeIds(unlock.getBiomes());
                if (!invalidBiomes.isEmpty()) {
                    for (String invalidBiome : invalidBiomes) {
                        AromaLib.LOGGER.warn("[{}] Skipping unregistered biome: '{}' - biome must be defined in biomes.json",
                                id, invalidBiome);
                    }
                    List<String> validBiomes = new ArrayList<>(unlock.getBiomes());
                    validBiomes.removeAll(invalidBiomes);
                    unlock.setBiomes(validBiomes);
                    AromaLib.LOGGER.info("[{}] Kept {} valid biome references after filtering", id, validBiomes.size());
                }
            }
        }
    }

    private static boolean textureExists(String texturePath) {
        String fullPath = texturePath + ".png";

        try (InputStream stream = TrackerDefinitionLoader.class.getClassLoader().getResourceAsStream(fullPath)) {
            return stream != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static TrackerDefinition parseFromJson(String json) {
        try {
            return GSON.fromJson(json, TrackerDefinition.class);
        } catch (Exception e) {
            AromaLib.LOGGER.error("Failed to parse tracker definition from JSON", e);
            return null;
        }
    }

    public static TrackerDefinition getByID(String id) {
        for (TrackerDefinition def : loadedItems) {
            if (def.getId().equals(id)) {
                return def;
            }
        }
        return null;
    }

    public static List<TrackerDefinition> getItemsByTier(int tier) {
        List<TrackerDefinition> result = new ArrayList<>();
        for (TrackerDefinition def : loadedItems) {
            if (def.getTier() == tier) {
                result.add(def);
            }
        }
        return result;
    }

    public static String toJson(TrackerDefinition nose) {
        return GSON.toJson(nose);
    }

    public static Gson getGson() {
        return GSON;
    }

    private static void addWarning(String warning) {
        validationWarnings.add(warning);
        AromaLib.LOGGER.warn(warning);
    }
}
