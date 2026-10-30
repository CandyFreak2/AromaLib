package com.ovrtechnology.aromalib.definition.loader;

import com.google.gson.JsonElement;
import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.data.DataSource;
import com.ovrtechnology.aromalib.data.JsonResources;
import com.ovrtechnology.aromalib.definition.TrackerDefinition;
import com.ovrtechnology.aromalib.registry.BiomeRegistry;
import com.ovrtechnology.aromalib.registry.BlockRegistry;
import com.ovrtechnology.aromalib.registry.StructureRegistry;
import com.ovrtechnology.aromalib.util.ItemUnlock;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TrackerDefinitionLoader extends AbstractDefinitionLoader<TrackerDefinition> {

    public static final TrackerDefinitionLoader INSTANCE = new TrackerDefinitionLoader();

    private static final String DEFAULT_TEXTURE = "item/foragers_nose";
    private static final String DEFAULT_MODEL = "minecraft:iron_helmet";

    public TrackerDefinitionLoader() {
        super("items", TrackerDefinition.class, "trackers");
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

    @Override
    public void validate(TrackerDefinition def) {
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
                        AromaLib.LOGGER.warn("[{}] Skipping unregistered block: '{}' - block must be defined in blocks.json", id, invalidBlock);
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
                        AromaLib.LOGGER.warn("[{}] Skipping unregistered structure: '{}' - structure must be defined in structures.json", id, invalidStructure);
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
                        AromaLib.LOGGER.warn("[{}] Skipping unregistered biome: '{}' - biome must be defined in biomes.json", id, invalidBiome);
                    }
                    List<String> validBiomes = new ArrayList<>(unlock.getBiomes());
                    validBiomes.removeAll(invalidBiomes);
                    unlock.setBiomes(validBiomes);
                    AromaLib.LOGGER.info("[{}] Kept {} valid biome references after filtering", id, validBiomes.size());
                }
            }
        }
    }

    public boolean textureExists(String texturePath) {
        String fullPath = texturePath + ".png";

        try (InputStream stream = TrackerDefinitionLoader.class.getClassLoader().getResourceAsStream(fullPath)) {
            return stream != null;
        } catch (Exception e) {
            return false;
        }
    }

    // @TODO replace the above with this one
//    public boolean textureExists(String path) {
//        String[] parts = path.split(":", 2);
//        Identifier file = Identifier.fromNamespaceAndPath(parts[0], parts[1] + ".png");
//        return Minecraft.getInstance().getResourceManager().getResource(file).isPresent();
//    }

    public List<TrackerDefinition> getItemsByTier(int tier) {
        List<TrackerDefinition> result = new ArrayList<>();
        for (TrackerDefinition def : loadedDefinitions) {
            if (def.getTier() == tier) {
                result.add(def);
            }
        }
        return result;
    }

    @Override
    protected void reload(DataSource source) {
        TrackerDefinition[] newDefs = loadFromResource(source, getDirectory());
        int mutated = 0, skipped = 0;
        for (TrackerDefinition src : newDefs) {
            if (src == null || !src.isValid()) continue;
            TrackerDefinition target = getDefinitionByID(src.getId());
            if (target == null) {
                AromaLib.LOGGER.warn("[Tracker Reload] ID '{}' is not built-in. Custom trackers must use the variant system.", src.getId());
                skipped++;
                continue;
            }
            copyFields(src, target);
            validate(target);
            mutated++;
        }
        AromaLib.LOGGER.info("Tracker Item reload: mutated {} definitions in place, skipped {} unknown IDs", mutated, skipped);
    }

    @Override
    protected TrackerDefinition[] loadFromResource(DataSource source, String path) {
        JsonElement element = source.read(path);
        if (element == null) {
            AromaLib.LOGGER.warn("Tracker definitions file not found: {}", path);
            return new TrackerDefinition[0];
        }
        return JsonResources.parseArrayOrWrapped(element, getName(), TrackerDefinition[].class, GSON, path);
    }
}
