//package com.ovrtechnology.definition.loader;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonElement;
//import com.ovrtechnology.AromaLib;
//import com.ovrtechnology.block.BlockDefinition;
//import com.ovrtechnology.data.ClasspathDataSource;
//import com.ovrtechnology.data.DataSource;
//import com.ovrtechnology.definition.ScentedDefinition;
//import com.ovrtechnology.scent.ScentRegistry;
//import lombok.Getter;
//import net.minecraft.resources.Identifier;
//
//import java.lang.reflect.ParameterizedType;
//import java.util.*;
//
//public abstract class DefinitionLoader<T extends ScentedDefinition> {
//    private static final Gson GSON = new GsonBuilder()
//            .setPrettyPrinting()
//            .create();
//
//    /**
//     * Cached list of loaded definitions
//     */
//    @Getter
//    private static List<T> loadedDefinitions = new ArrayList<>();
//
//    /**
//     * Set of loaded IDs for duplicate detection
//     */
//    private static Set<String> loadedIds = new HashSet<>();
//
//    /**
//     * List of validation warnings encountered during loading
//     */
//    @Getter
//    private static List<String> validationWarnings = new ArrayList<>();
//
//    /**
//     * Load all block definitions from the blocks directory.
//     * This parses the JSON file and validates each entry.
//     *
//     * <p>Validation includes:</p>
//     * <ul>
//     *   <li>Duplicate block_id detection</li>
//     *   <li>HTML color format validation</li>
//     *   <li>Scent ID reference validation (requires ScentRegistry to be initialized)</li>
//     * </ul>
//     *
//     * @return An unmodifiable list of valid block definitions
//     */
//    public List<T> loadAll() {
//        return loadAll(ClasspathDataSource.INSTANCE);
//    }
//
//    public static List<BlockDefinition> loadAllBlocks(DataSource dataSource) {
//        loadedBlocks.clear();
//        loadedIds.clear();
//        validationWarnings.clear();
//
//        Map<Identifier, JsonElement> files = dataSource.listJson(BLOCKS_DIR);
//        for (Map.Entry<Identifier, JsonElement> entry : files.entrySet()) {
//            try {
//                BlockDefinition block = GSON.fromJson(entry.getValue(), BlockDefinition.class);
//                processBlock(block);
//            } catch (Exception e) {
//                AromaLib.LOGGER.error("Failed to parse block {}: {}", entry.getKey(), e.getMessage());
//            }
//        }
//
//        AromaLib.LOGGER.info("Loaded {} block definitions from {} file(s)", loadedBlocks.size(), files.size());
//
//        if (!validationWarnings.isEmpty()) {
//            AromaLib.LOGGER.warn("Block loading completed with {} validation warnings", validationWarnings.size());
//        }
//
//        return Collections.unmodifiableList(loadedBlocks);
//    }
//
//    /**
//     * Process a single block definition, validating and adding to the loaded list.
//     *
//     * @param block The block definition to process
//     */
//    private static void processBlock(T block) {
//        if (block == null) {
//            addWarning("Null block definition found, skipping...");
//            return;
//        }
//
//        if (!block.isValid()) {
//            addWarning("Invalid block definition found (missing block_id), skipping...");
//            return;
//        }
//
//        String blockId = block.getId();
//
//        // Check for duplicate IDs
//        if (loadedIds.contains(blockId)) {
//            addWarning("Duplicate block_id '" + blockId + "' found, skipping...");
//            return;
//        }
//
//        // Validate the block entry
//        validateBlock(block);
//
//        loadedIds.add(blockId);
//        loadedDefinitions.add(block);
//        AromaLib.LOGGER.debug("Loaded block definition: {} (color: {}, scent: {})",
//                blockId, block.getColorHtml(), block.getScentId());
//    }
//
//    /**
//     * Validate a block definition and log any warnings.
//     *
//     * @param block The block to validate
//     */
//    private static void validateBlock(BlockDefinition block) {
//        String blockId = block.getId();
//
//        // Validate HTML color format
//        String rawColor = block.getRawColorHtml();
//        if (rawColor == null || rawColor.isEmpty()) {
//            addWarning("[" + blockId + "] No color_html defined, using default: " + BlockDefinition.getColor(block));
//        } else if (!BlockDefinition.isValidHtmlColor(rawColor)) {
//            addWarning("[" + blockId + "] Invalid color_html format '" + rawColor + "', using default: " + BlockDefinition.getColor(block));
//        }
//
//        // Validate scent_id reference
//        if (block.hasScentId()) {
//            String scentId = block.getScentId();
//            if (ScentRegistry.isInitialized() && !ScentRegistry.hasScent(scentId)) {
//                addWarning("[" + blockId + "] Referenced scent_id '" + scentId + "' does not exist in ScentRegistry");
//            }
//        } else {
//            addWarning("[" + blockId + "] No scent_id defined, block will have no associated scent");
//        }
//
//        // Validate block_id format (should be namespace:path)
//        if (!blockId.contains(":")) {
//            addWarning("[" + blockId + "] Block ID should include namespace (e.g., 'minecraft:stone')");
//        }
//    }
//
//    /**
//     * Add a validation warning.
//     *
//     * @param warning The warning message
//     */
//    private static void addWarning(String warning) {
//        validationWarnings.add(warning);
//        AromaLib.LOGGER.warn(warning);
//    }
//
//    /**
//     * Parse a single block definition from a JSON string.
//     *
//     * @param json The JSON string to parse
//     * @return The parsed block definition, or null if parsing fails
//     */
////    public T parseDefFromJSON(String json) {
////        try {
////            return GSON.fromJson(json, T.class);
////        } catch (Exception e) {
////            AromaLib.LOGGER.error("Failed to parse " + getGenericName() + " definition from JSON", e);
////            return null;
////        }
////    }
//
//    /**
//     * Get a block definition by block ID from the loaded blocks.
//     *
//     * @param id The block ID to look up
//     * @return The block definition, or null if not found
//     */
//    public T getDefByID(String id) {
//        for (T def : loadedDefinitions) {
//            if (def.getId().equals(id)) {
//                return def;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Check if a block with the given ID has been loaded.
//     *
//     * @param id The block ID to check
//     * @return true if the block exists
//     */
//    public static boolean hasID(String id) {
//        return loadedIds.contains(id);
//    }
//
//    /**
//     * Get all blocks that reference a specific scent.
//     *
//     * @param scentId The scent ID to filter by
//     * @return List of blocks using the specified scent
//     */
//    public List<T> getDefinitionByScentId(String scentId) {
//        List<T> result = new ArrayList<>();
//        for (T def : loadedDefinitions) {
//            if (scentId.equals(def.getScentId())) {
//                result.add(def);
//            }
//        }
//        return result;
//    }
//
//    /**
//     * Serialize a block definition to JSON.
//     *
//     * @param def The block to serialize
//     * @return JSON string representation
//     */
//    public String toJson(T def) {
//        return GSON.toJson(def);
//    }
//
//    /**
//     * Get the GSON instance for external use.
//     *
//     * @return The configured GSON instance
//     */
//    public Gson getGson() {
//        return GSON;
//    }
//
//    /**
//     * Reload all block definitions.
//     * This clears the cache and reloads from the JSON file.
//     *
//     * @return The reloaded list of block definitions
//     */
//    public List<T> reload() {
//        AromaLib.LOGGER.info("Reloading {} ...", getGenericName());
//        return loadAll();
//    }
//
//    /**
//     * Check if any validation warnings occurred during loading.
//     *
//     * @return true if there were validation warnings
//     */
//    public static boolean hasValidationWarnings() {
//        return !validationWarnings.isEmpty();
//    }
//
//    protected String getGenericName() {
//        return ((Class<T>) ((ParameterizedType) getClass()
//                .getGenericSuperclass()).getActualTypeArguments()[0]).getTypeName();
//    }
//}
