package com.ovrtechnology.aromalib.registry;

import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.definition.CategoryDefinition;
import com.ovrtechnology.aromalib.definition.loader.CategoryDefinitionLoader;
import lombok.Getter;

import java.util.*;

public final class CategoryRegistry {

    @Getter
    private static final Map<String, CategoryDefinition> categoryDefinitions = new LinkedHashMap<>();

    @Getter
    private static boolean initialized = false;

    private CategoryRegistry() {
        throw new UnsupportedOperationException("CategoryRegistry is a static utility class");
    }

    public static void init() {
        if (initialized) {
            AromaLib.LOGGER.warn("CategoryRegistry.init() called multiple times!");
            return;
        }
        
        // Warn if ScentRegistry is not initialized
        if (!ScentRegistry.isInitialized()) {
            AromaLib.LOGGER.warn("CategoryRegistry.init() called before ScentRegistry! Scent validation may fail.");
        }
        
        AromaLib.LOGGER.info("Initializing CategoryRegistry...");
        
        // Load category definitions from JSON
        List<CategoryDefinition> definitions = CategoryDefinitionLoader.INSTANCE.loadAll();
        
        // Store each category in the map
        for (CategoryDefinition definition : definitions) {
            registerCategory(definition);
        }
        
        initialized = true;
        AromaLib.LOGGER.info("CategoryRegistry initialized with {} categories", categoryDefinitions.size());
    }
    

    private static void registerCategory(CategoryDefinition definition) {
        String id = definition.getId();
        
        if (categoryDefinitions.containsKey(id)) {
            AromaLib.LOGGER.warn("Duplicate category ID in registry: {}, skipping...", id);
            return;
        }
        
        categoryDefinitions.put(id, definition);
        AromaLib.LOGGER.debug("Registered category: {}", id);
    }

    public static Optional<CategoryDefinition> getCategory(String id) {
        return Optional.ofNullable(categoryDefinitions.get(id));
    }

    public static CategoryDefinition getCategoryOrThrow(String id) {
        CategoryDefinition def = categoryDefinitions.get(id);
        if (def == null) {
            throw new IllegalArgumentException("Unknown category ID: " + id);
        }
        return def;
    }

    public static boolean hasCategory(String id) {
        return categoryDefinitions.containsKey(id);
    }

    public static Iterable<String> getAllCategoryIDs() {
        return Collections.unmodifiableSet(categoryDefinitions.keySet());
    }

    public static Iterable<CategoryDefinition> getAllCategories() {
        return Collections.unmodifiableCollection(categoryDefinitions.values());
    }

    public static List<CategoryDefinition> getAllCategoriesAsList() {
        return new ArrayList<>(categoryDefinitions.values());
    }

    public static int getCategorySize() {
        return categoryDefinitions.size();
    }

    public static int getCategoryColor(String id) {
        CategoryDefinition def = categoryDefinitions.get(id);
        return def != null ? def.getColorAsInt() : 0xFFFFFF;
    }

    public static String getCategoryColorHtml(String id) {
        CategoryDefinition def = categoryDefinitions.get(id);
        return def != null ? def.getColorHtml() : CategoryDefinition.getColor(def);
    }

    public static List<String> validateCategoryIds(List<String> ids) {
        List<String> invalid = new ArrayList<>();
        for (String id : ids) {
            if (!hasCategory(id)) {
                invalid.add(id);
            }
        }
        return invalid;
    }

    public static void reload() {
        AromaLib.LOGGER.info("Reloading CategoryRegistry...");
        categoryDefinitions.clear();
        
        List<CategoryDefinition> definitions = CategoryDefinitionLoader.INSTANCE.reload();
        for (CategoryDefinition definition : definitions) {
            registerCategory(definition);
        }
        
        AromaLib.LOGGER.info("CategoryRegistry reloaded with {} categories", categoryDefinitions.size());
    }
    

    static void clear() {
        categoryDefinitions.clear();
        initialized = false;
    }
}

