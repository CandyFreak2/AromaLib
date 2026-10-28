package com.ovrtechnology.aromalib.tracker;
import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.definition.TrackerDefinition;
import com.ovrtechnology.aromalib.registry.TrackerRegistry;
import com.ovrtechnology.aromalib.tracker.item.ItemUnlock;
import com.ovrtechnology.aromalib.registry.variants.TrackerVariantRegistry;
import lombok.Getter;
import net.minecraft.resources.Identifier;

import java.util.*;

/**
 * Resolves and caches all abilities for each tracker, including inherited abilities from other trackers.
 * Handles circular dependency detection to prevent infinite loops.
 */
public final class TrackingAbilityResolver {
    
    /**
     * Cached resolved abilities for each tracker ID.
     * Key: Tracker ID, Value: set of all ability IDs (including inherited)
     */
    private static final Map<String, ResolvedAbilities> ABILITY_CACHE = new HashMap<>();
    
    /**
     * Whether the resolver has been initialized
     */
    private static boolean initialized = false;
    
    /**
     * Initialize the ability resolver.
     * Must be called after all tracker definitions are loaded.
     */
    public static void init() {
        if (initialized) {
            AromaLib.LOGGER.warn("TrackerAbilityResolver.init() called multiple times!");
            return;
        }

        AromaLib.LOGGER.info("Initializing TrackerAbilityResolver...");
        
        // Resolve abilities for each built-in tracker
        for (String id : TrackerRegistry.getAllIds()) {
            resolveAbilities(id);
        }

        // Resolve abilities for each datapack variant
        for (Identifier id : TrackerVariantRegistry.all().keySet()) {
            resolveAbilities(id.toString());
        }

        initialized = true;
        AromaLib.LOGGER.info("TrackerAbilityResolver initialized with {} cached entries", ABILITY_CACHE.size());
    }

    /**
     * Rebuild the ability cache from scratch. Called after tracker variants are
     * (re)loaded from datapacks so custom noses resolve their abilities.
     */
    public static void rebuild() {
        ABILITY_CACHE.clear();
        initialized = false;
        init();
    }
    
    /**
     * Resolve and cache all abilities for a tracker (including inherited ones)
     */
    private static ResolvedAbilities resolveAbilities(String id) {
        // Check cache first
        if (ABILITY_CACHE.containsKey(id)) {
            return ABILITY_CACHE.get(id);
        }
        
        // Create a new resolution context with visited tracking for circular dependency detection
        Set<String> visited = new HashSet<>();
        Set<String> abilities = new LinkedHashSet<>();
        Set<String> blocks = new LinkedHashSet<>();
        Set<String> biomes = new LinkedHashSet<>();
        Set<String> structures = new LinkedHashSet<>();
        Set<String> flowers = new LinkedHashSet<>();
        List<String> circularDependencies = new ArrayList<>();

        resolveRecursive(id, visited, abilities, blocks, biomes, structures, flowers, circularDependencies);

        // Report any circular dependencies found
        if (!circularDependencies.isEmpty()) {
            AromaLib.LOGGER.warn("Circular dependencies detected for tracker '{}': {}", id, circularDependencies);
        }

        ResolvedAbilities resolved = new ResolvedAbilities(
                Collections.unmodifiableSet(abilities),
                Collections.unmodifiableSet(blocks),
                Collections.unmodifiableSet(biomes),
                Collections.unmodifiableSet(structures),
                Collections.unmodifiableSet(flowers)
        );

        ABILITY_CACHE.put(id, resolved);
        AromaLib.LOGGER.debug("Resolved abilities for '{}': {} abilities, {} blocks, {} biomes, {} structures, {} flowers",
                id, abilities.size(), blocks.size(), biomes.size(), structures.size(), flowers.size());
        
        return resolved;
    }
    
    /**
     * Recursively resolve abilities, detecting circular dependencies
     */
    private static void resolveRecursive(
            String id,
            Set<String> visited,
            Set<String> abilities,
            Set<String> blocks,
            Set<String> biomes,
            Set<String> structures,
            Set<String> flowers,
            List<String> circularDependencies
    ) {
        // Circular dependency check
        if (visited.contains(id)) {
            circularDependencies.add(id);
            AromaLib.LOGGER.warn("Circular dependency detected: tracker '{}' already in resolution chain", id);
            return;
        }

        visited.add(id);

        // Resolve the unlock config from a built-in tracker definition, falling back
        // to a datapack tracker variant if this id is not a built-in tracker.
        ItemUnlock unlock = lookupUnlock(id);
        if (unlock == null) {
            AromaLib.LOGGER.warn("Referenced tracker '{}' not found during ability resolution", id);
            visited.remove(id);
            return;
        }

        // Add direct abilities
        abilities.addAll(unlock.getAbilities());
        blocks.addAll(unlock.getBlocks());
        biomes.addAll(unlock.getBiomes());
        structures.addAll(unlock.getStructures());
        flowers.addAll(unlock.getFlowers());

        // Recursively resolve inherited noses
        for (String inheritedTrackerId : unlock.getTrackers()) {
            resolveRecursive(inheritedTrackerId, visited, abilities, blocks, biomes, structures, flowers, circularDependencies);
        }

        visited.remove(id);
    }
    
    /**
     * Get all resolved abilities for a tracker (including inherited)
     */
    private static ItemUnlock lookupUnlock(String id) {
        Optional<TrackerDefinition> defOpt = TrackerRegistry.getDefinition(id);
        if (defOpt.isPresent()) {
            return defOpt.get().getUnlock();
        }
        // @TODO
//        Identifier src = Identifier.tryParse(id);
//        if (src != null) {
//            Optional<TrackerVariant> variantOpt = TrackerVariantRegistry.get(src);
//            if (variantOpt.isPresent()) {
//                return variantOpt.get().getUnlock();
//            }
//        }
        return null;
    }

    public static ResolvedAbilities getResolvedAbilities(String noseId) {
        if (!initialized) {
            AromaLib.LOGGER.error("TrackerAbilityResolver not initialized! Call init() first.");
            return ResolvedAbilities.EMPTY;
        }
        
        ResolvedAbilities resolved = ABILITY_CACHE.get(noseId);
        if (resolved == null) {
            AromaLib.LOGGER.warn("No resolved abilities found for tracker: {}", noseId);
            return ResolvedAbilities.EMPTY;
        }
        
        return resolved;
    }
    
    /**
     * Check if a tracker has a specific ability (including inherited)
     */
    public static boolean hasAbility(String noseId, String abilityId) {
        return getResolvedAbilities(noseId).getAbilities().contains(abilityId);
    }
    
    /**
     * Check if a tracker can detect a specific block (including inherited)
     */
    public static boolean canDetectBlock(String noseId, String blockId) {
        return getResolvedAbilities(noseId).getBlocks().contains(blockId);
    }
    
    /**
     * Check if a tracker can detect a specific biome (including inherited)
     */
    public static boolean canDetectBiome(String noseId, String biomeId) {
        return getResolvedAbilities(noseId).getBiomes().contains(biomeId);
    }
    
    /**
     * Check if a tracker can detect a specific structure (including inherited)
     */
    public static boolean canDetectStructure(String noseId, String structureId) {
        return getResolvedAbilities(noseId).getStructures().contains(structureId);
    }

    /**
     * Check if a tracker can detect a specific flower (including inherited)
     */
    public static boolean canDetectFlower(String noseId, String flowerId) {
        return getResolvedAbilities(noseId).getFlowers().contains(flowerId);
    }

    /**
     * Clear the ability cache (for reloading)
     */
    public static void clearCache() {
        ABILITY_CACHE.clear();
        initialized = false;
        AromaLib.LOGGER.info("TrackerAbilityResolver cache cleared");
    }
    
    /**
     * Container for resolved abilities
     */
    @Getter
    public static class ResolvedAbilities {
        public static final ResolvedAbilities EMPTY = new ResolvedAbilities(
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet()
        );

        private final Set<String> abilities;
        private final Set<String> blocks;
        private final Set<String> biomes;
        private final Set<String> structures;
        private final Set<String> flowers;

        public ResolvedAbilities(Set<String> abilities, Set<String> blocks, Set<String> biomes, Set<String> structures, Set<String> flowers) {
            this.abilities = abilities;
            this.blocks = blocks;
            this.biomes = biomes;
            this.structures = structures;
            this.flowers = flowers;
        }

        public boolean hasAbility(String abilityId) {
            return abilities.contains(abilityId);
        }

        public boolean canDetectBlock(String blockId) {
            return blocks.contains(blockId);
        }

        public boolean canDetectBiome(String biomeId) {
            return biomes.contains(biomeId);
        }

        public boolean canDetectStructure(String structureId) {
            return structures.contains(structureId);
        }

        public boolean canDetectFlower(String flowerId) {
            return flowers.contains(flowerId);
        }

    }
}
