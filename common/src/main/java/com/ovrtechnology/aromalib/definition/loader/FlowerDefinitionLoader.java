package com.ovrtechnology.aromalib.definition.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.data.ClasspathDataSource;
import com.ovrtechnology.aromalib.data.DataSource;
import com.ovrtechnology.aromalib.definition.FlowerDefinition;
import com.ovrtechnology.aromalib.registry.ScentRegistry;
import lombok.Getter;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads flower definitions from JSON files.
 */
public class FlowerDefinitionLoader {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static final String DIRECTORY = "flowers";

    @Getter
    private static List<FlowerDefinition> loadedFlowers = new ArrayList<>();

    private static Set<String> loadedIds = new HashSet<>();

    @Getter
    private static List<String> validationWarnings = new ArrayList<>();

    public static List<FlowerDefinition> loadAllFlowers() {
        return loadAllFlowers(ClasspathDataSource.INSTANCE);
    }

    public static List<FlowerDefinition> loadAllFlowers(DataSource dataSource) {
        loadedFlowers.clear();
        loadedIds.clear();
        validationWarnings.clear();

        Map<Identifier, JsonElement> files = dataSource.listJson(DIRECTORY);
        for (Map.Entry<Identifier, JsonElement> entry : files.entrySet()) {
            try {
                FlowerDefinition flower = GSON.fromJson(entry.getValue(), FlowerDefinition.class);
                processFlower(flower);
            } catch (Exception e) {
                AromaLib.LOGGER.error("Failed to parse flower {}: {}", entry.getKey(), e.getMessage());
            }
        }

        AromaLib.LOGGER.info("Loaded {} flower definitions from {} file(s)", loadedFlowers.size(), files.size());

        if (!validationWarnings.isEmpty()) {
            AromaLib.LOGGER.warn("Flower loading completed with {} validation warnings", validationWarnings.size());
        }

        return Collections.unmodifiableList(loadedFlowers);
    }

    private static void processFlower(FlowerDefinition flower) {
        if (flower == null) {
            addWarning("Null flower definition found, skipping...");
            return;
        }

        if (!flower.isValid()) {
            addWarning("Invalid flower definition found (missing block_id), skipping...");
            return;
        }

        String blockId = flower.getId();

        if (loadedIds.contains(blockId)) {
            addWarning("Duplicate flower block_id '" + blockId + "' found, skipping...");
            return;
        }

        validateFlower(flower);

        loadedIds.add(blockId);
        loadedFlowers.add(flower);
        AromaLib.LOGGER.debug("Loaded flower definition: {} (scent: {})",
                blockId, flower.getScentId());
    }

    private static void validateFlower(FlowerDefinition flower) {
        String blockId = flower.getId();

        if (flower.hasScentId()) {
            String scentId = flower.getScentId();
            if (ScentRegistry.isInitialized() && !ScentRegistry.hasScent(scentId)) {
                addWarning("[" + blockId + "] Referenced scent_id '" + scentId + "' does not exist in ScentRegistry");
            }
        } else {
            addWarning("[" + blockId + "] No scent_id defined, flower will have no associated scent");
        }
    }

    private static void addWarning(String warning) {
        validationWarnings.add(warning);
        AromaLib.LOGGER.warn(warning);
    }

    public static FlowerDefinition getFlowerById(String blockId) {
        for (FlowerDefinition flower : loadedFlowers) {
            if (flower.getId().equals(blockId)) {
                return flower;
            }
        }
        return null;
    }

    public static boolean hasFlowerId(String blockId) {
        return loadedIds.contains(blockId);
    }

    public static List<FlowerDefinition> reload() {
        AromaLib.LOGGER.info("Reloading flower definitions...");
        return loadAllFlowers();
    }
}
