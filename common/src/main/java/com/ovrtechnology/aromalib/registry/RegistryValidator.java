package com.ovrtechnology.aromalib.registry;

import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.biome.BiomeDefinition;
import com.ovrtechnology.aromalib.biome.BiomeRegistry;
import com.ovrtechnology.aromalib.block.BlockDefinition;
import com.ovrtechnology.aromalib.block.BlockRegistry;
import com.ovrtechnology.aromalib.structure.StructureDefinition;
import com.ovrtechnology.aromalib.structure.StructureRegistry;
import net.blay09.mods.balm.platform.event.callback.ServerLifecycleCallback;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates Aroma Affect registry entries against Minecraft's actual registries.
 * 
 * <p>This class performs post-load validation after the server has started,
 * when all mods have registered their content and the world's registry access
 * is available.</p>
 * 
 * <h2>Why Post-Load Validation?</h2>
 * <p>During mod initialization ({@code AromaAffect.init()}), Minecraft's dynamic
 * registries (structures, biomes) are not yet populated with world-specific data.
 * We can only validate against them after the server/world has loaded.</p>
 * 
 * <h2>Validation Order</h2>
 * <ol>
 *   <li>Blocks - validated against {@code BuiltInRegistries.BLOCK}</li>
 *   <li>Biomes - validated against the world's biome registry</li>
 *   <li>Structures - validated against the world's structure registry</li>
 * </ol>
 * 
 * <h2>Usage</h2>
 * <p>Call {@link #init()} during mod initialization to register the
 * {@code SERVER_STARTED} lifecycle event handler.</p>
 */
public final class RegistryValidator {
    
    /**
     * Set of structure IDs that failed validation (not found in Minecraft registry).
     */
    @Getter
    private static final Set<String> invalidStructureIds = new HashSet<>();
    
    /**
     * Set of biome IDs that failed validation (not found in Minecraft registry).
     */
    @Getter
    private static final Set<String> invalidBiomeIds = new HashSet<>();
    
    /**
     * Set of block IDs that failed validation (not found in Minecraft registry).
     */
    @Getter
    private static final Set<String> invalidBlockIds = new HashSet<>();

    @Getter
    private static final Set<String> invalidCategoryIds = new HashSet<>();
    
    /**
     * Whether validation has been performed.
     */
    @Getter
    private static boolean validated = false;
    
    /**
     * Total count of validation errors.
     */
    @Getter
    private static int errorCount = 0;
    
    private RegistryValidator() {
        throw new UnsupportedOperationException("RegistryValidator is a static utility class");
    }
    
    /**
     * Initialize the registry validator.
     * Registers a SERVER_STARTED event handler to perform validation.
     */
    public static void init() {
        ServerLifecycleCallback.Started.EVENT.register(RegistryValidator::onServerStarted);
        AromaLib.LOGGER.info("RegistryValidator initialized - will validate on server start");
    }
    
    /**
     * Called when the server has fully started.
     * At this point, all mods have registered their content and the world is loaded.
     */
    private static void onServerStarted(MinecraftServer server) {
        AromaLib.LOGGER.info("Validating Aroma Affect registries against Minecraft registries...");
        
        // Clear previous validation state
        invalidStructureIds.clear();
        invalidBiomeIds.clear();
        invalidBlockIds.clear();
        invalidCategoryIds.clear();
        errorCount = 0;
        validated = false;
        
        long startTime = System.currentTimeMillis();
        
        // Get the overworld for registry access (all dimensions share the same registries)
        var level = server.overworld();
        var registryAccess = level.registryAccess();
        
        // Validate blocks (uses BuiltInRegistries, available earlier but we validate here for consistency)
        validateBlocks();
        
        // Validate biomes against the world's biome registry
        validateBiomes(registryAccess.lookupOrThrow(Registries.BIOME));
        
        // Validate structures against the world's structure registry
        validateStructures(registryAccess.lookupOrThrow(Registries.STRUCTURE));
        
        validated = true;
        long elapsed = System.currentTimeMillis() - startTime;
        
        if (errorCount == 0) {
            AromaLib.LOGGER.info("Registry validation complete in {}ms - all entries valid!", elapsed);
        } else {
            AromaLib.LOGGER.warn("Registry validation complete in {}ms - found {} invalid entries", elapsed, errorCount);
            logValidationSummary();
        }
    }

    /**
     * Validates all block definitions against Minecraft's block registry.
     */
    private static void validateBlocks() {
        if (!BlockRegistry.isInitialized()) {
            AromaLib.LOGGER.warn("BlockRegistry not initialized, skipping block validation");
            return;
        }
        
        Registry<Block> blockRegistry = net.minecraft.core.registries.BuiltInRegistries.BLOCK;
        int checked = 0;
        int invalid = 0;
        
        for (BlockDefinition block : BlockRegistry.getAllBlocks()) {
            checked++;
            String blockId = block.getId();
            Identifier resourceLocation = Identifier.tryParse(blockId);
            
            if (resourceLocation == null) {
                AromaLib.LOGGER.error("[BlockRegistry] Invalid Identifier format: '{}'", blockId);
                invalidBlockIds.add(blockId);
                invalid++;
                continue;
            }
            
            if (!blockRegistry.containsKey(resourceLocation)) {
                AromaLib.LOGGER.warn("[BlockRegistry] Block not found in Minecraft registry: '{}' - " +
                        "this block will not be trackable", blockId);
                invalidBlockIds.add(blockId);
                invalid++;
            }
        }
        
        errorCount += invalid;
        AromaLib.LOGGER.info("Validated {} blocks: {} valid, {} invalid",
                checked, checked - invalid, invalid);
    }
    
    /**
     * Validates all biome definitions against Minecraft's biome registry.
     */
    private static void validateBiomes(Registry<Biome> biomeRegistry) {
        if (!BiomeRegistry.isInitialized()) {
            AromaLib.LOGGER.warn("BiomeRegistry not initialized, skipping biome validation");
            return;
        }
        
        int checked = 0;
        int invalid = 0;
        
        for (BiomeDefinition biome : BiomeRegistry.getAllBiomes()) {
            checked++;
            String biomeId = biome.getId();
            Identifier resourceLocation = Identifier.tryParse(biomeId);
            
            if (resourceLocation == null) {
                AromaLib.LOGGER.error("[BiomeRegistry] Invalid Identifier format: '{}'", biomeId);
                invalidBiomeIds.add(biomeId);
                invalid++;
                continue;
            }
            
            if (!biomeRegistry.containsKey(resourceLocation)) {
                // Check if it's a modded biome that might not be loaded
                String namespace = biome.getNamespace();
                if ("minecraft".equals(namespace)) {
                    AromaLib.LOGGER.error("[BiomeRegistry] Vanilla biome not found: '{}' - " +
                            "this may indicate a typo or version mismatch", biomeId);
                } else {
                    AromaLib.LOGGER.warn("[BiomeRegistry] Modded biome not found: '{}' - " +
                            "the mod '{}' may not be installed or the biome ID may be incorrect", 
                            biomeId, namespace);
                }
                invalidBiomeIds.add(biomeId);
                invalid++;
            }
        }
        
        errorCount += invalid;
        AromaLib.LOGGER.info("Validated {} biomes: {} valid, {} invalid",
                checked, checked - invalid, invalid);
    }
    
    /**
     * Validates all structure definitions against Minecraft's structure registry.
     */
    private static void validateStructures(Registry<Structure> structureRegistry) {
        if (!StructureRegistry.isInitialized()) {
            AromaLib.LOGGER.warn("StructureRegistry not initialized, skipping structure validation");
            return;
        }
        
        int checked = 0;
        int invalid = 0;
        
        // Log available structures for debugging
        AromaLib.LOGGER.debug("Available structures in world: {}",
                structureRegistry.keySet().stream().map(Identifier::toString).toList());
        
        for (StructureDefinition structure : StructureRegistry.getAllStructures()) {
            checked++;
            String structureId = structure.getId();
            Identifier resourceLocation = Identifier.tryParse(structureId);
            
            if (resourceLocation == null) {
                AromaLib.LOGGER.error("[StructureRegistry] Invalid Identifier format: '{}'", structureId);
                invalidStructureIds.add(structureId);
                invalid++;
                continue;
            }
            
            if (!structureRegistry.containsKey(resourceLocation)) {
                // Check if it's a modded structure that might not be loaded
                String namespace = structure.getNamespace();
                if ("minecraft".equals(namespace)) {
                    AromaLib.LOGGER.error("[StructureRegistry] Vanilla structure not found: '{}' - " +
                            "this may indicate a typo or version mismatch", structureId);
                } else {
                    AromaLib.LOGGER.warn("[StructureRegistry] Modded structure not found: '{}' - " +
                            "the mod '{}' may not be installed or the structure ID may be incorrect", 
                            structureId, namespace);
                }
                invalidStructureIds.add(structureId);
                invalid++;
            }
        }
        
        errorCount += invalid;
        AromaLib.LOGGER.info("Validated {} structures: {} valid, {} invalid",
                checked, checked - invalid, invalid);
    }
    
    /**
     * Logs a summary of all validation errors.
     */
    private static void logValidationSummary() {
        AromaLib.LOGGER.warn("=== Registry Validation Summary ===");
        
        if (!invalidBlockIds.isEmpty()) {
            AromaLib.LOGGER.warn("Invalid blocks ({}): {}",
                    invalidBlockIds.size(), invalidBlockIds);
        }
        
        if (!invalidBiomeIds.isEmpty()) {
            AromaLib.LOGGER.warn("Invalid biomes ({}): {}",
                    invalidBiomeIds.size(), invalidBiomeIds);
        }
        
        if (!invalidStructureIds.isEmpty()) {
            AromaLib.LOGGER.warn("Invalid structures ({}): {}",
                    invalidStructureIds.size(), invalidStructureIds);
        }
        
        AromaLib.LOGGER.warn("These entries will not be trackable. Check your JSON configuration files.");
        AromaLib.LOGGER.warn("===================================");
    }
    
    /**
     * Check if a structure ID is valid (exists in Minecraft registry).
     * Only meaningful after validation has run.
     * 
     * @param structureId The structure ID to check
     * @return true if valid or validation hasn't run yet
     */
    public static boolean isStructureValid(String structureId) {
        if (!validated) {
            return true; // Assume valid if not yet validated
        }
        return !invalidStructureIds.contains(structureId);
    }
    
    /**
     * Check if a biome ID is valid (exists in Minecraft registry).
     * Only meaningful after validation has run.
     * 
     * @param biomeId The biome ID to check
     * @return true if valid or validation hasn't run yet
     */
    public static boolean isBiomeValid(String biomeId) {
        if (!validated) {
            return true; // Assume valid if not yet validated
        }
        return !invalidBiomeIds.contains(biomeId);
    }
    
    /**
     * Check if a block ID is valid (exists in Minecraft registry).
     * Only meaningful after validation has run.
     * 
     * @param blockId The block ID to check
     * @return true if valid or validation hasn't run yet
     */
    public static boolean isBlockValid(String blockId) {
        if (!validated) {
            return true; // Assume valid if not yet validated
        }
        return !invalidBlockIds.contains(blockId);
    }
    
    /**
     * Get all valid structure definitions (those that exist in Minecraft registry).
     * 
     * @return List of valid structures, or all structures if validation hasn't run
     */
    public static List<StructureDefinition> getValidStructures() {
        if (!validated || invalidStructureIds.isEmpty()) {
            return StructureRegistry.getAllStructuresAsList();
        }
        
        List<StructureDefinition> valid = new ArrayList<>();
        for (StructureDefinition structure : StructureRegistry.getAllStructures()) {
            if (!invalidStructureIds.contains(structure.getId())) {
                valid.add(structure);
            }
        }
        return valid;
    }
    
    /**
     * Get all valid biome definitions (those that exist in Minecraft registry).
     * 
     * @return List of valid biomes, or all biomes if validation hasn't run
     */
    public static List<BiomeDefinition> getValidBiomes() {
        if (!validated || invalidBiomeIds.isEmpty()) {
            return BiomeRegistry.getAllBiomesAsList();
        }
        
        List<BiomeDefinition> valid = new ArrayList<>();
        for (BiomeDefinition biome : BiomeRegistry.getAllBiomes()) {
            if (!invalidBiomeIds.contains(biome.getId())) {
                valid.add(biome);
            }
        }
        return valid;
    }
    
    /**
     * Get all valid block definitions (those that exist in Minecraft registry).
     * 
     * @return List of valid blocks, or all blocks if validation hasn't run
     */
    public static List<BlockDefinition> getValidBlocks() {
        if (!validated || invalidBlockIds.isEmpty()) {
            return BlockRegistry.getAllBlocksAsList();
        }
        
        List<BlockDefinition> valid = new ArrayList<>();
        for (BlockDefinition block : BlockRegistry.getAllBlocks()) {
            if (!invalidBlockIds.contains(block.getId())) {
                valid.add(block);
            }
        }
        return valid;
    }
    
    /**
     * Reset validation state (primarily for testing).
     */
    static void reset() {
        invalidStructureIds.clear();
        invalidBiomeIds.clear();
        invalidBlockIds.clear();
        errorCount = 0;
        validated = false;
    }
}

