package com.ovrtechnology.aromalib.definition.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.data.DataSource;
import com.ovrtechnology.aromalib.definition.DefinitionRegistry;
import com.ovrtechnology.aromalib.definition.IDefinition;
import lombok.Getter;
import net.minecraft.resources.Identifier;

import java.util.*;

public abstract class DefinitionLoader<T extends IDefinition> {
    protected static final Gson GSON = createGSON().create();
    protected static String DIRECTORY;
    protected final Class<T> DEF;

    @Getter
    protected List<T> loadedDefs = new ArrayList<>();

    protected Set<String> loadedIds = new HashSet<>();

    @Getter
    protected List<String> validationWarnings = new ArrayList<>();

    protected DefinitionLoader(Class<T> def) {
        this.DEF = def;
    }

    protected static GsonBuilder createGSON() {
        return new GsonBuilder().setPrettyPrinting();
    }

    public static <T extends IDefinition> DefinitionRegistry<T> loadAll(DataSource source, String directory, Class<T> defClass) {
        List<T> loadedDefs = new ArrayList<>();
        Map<String, T> idMap = new HashMap<>();

        Map<Identifier, JsonElement> files = source.listJson(directory);
        for (Map.Entry<Identifier, JsonElement> entry : files.entrySet()) {
            try {
                T definition = GSON.fromJson(entry.getValue(), defClass);
                if (definition != null && definition.isValid()) {
                    String id = definition.getDefID();
                    if (!idMap.containsKey(id)) {
                        idMap.put(id, definition);
                        loadedDefs.add(definition);
                    }
                }
            } catch (Exception e) {
                AromaLib.LOGGER.error("Failed to parse definition {} in directory {}", entry.getKey(), directory, e);
            }
        }
        return new DefinitionRegistry<>(loadedDefs, idMap);
    }

    public static String getDirectory() {
        return DIRECTORY;
    }

    private void processDefinition(T def) {
        if (def == null) {
            addWarning("Null definition found, skipping...");
            return;
        }

        if (!def.isValid()) {
            addWarning("Invalid definition found (missing id), skipping...");
            return;
        }

        String id = def.getDefID();

        if (loadedIds.contains(id)) {
            addWarning("Duplicate id '" + id + "' found, skipping...");
            return;
        }

        validateDefinition(def);

        loadedIds.add(id);
        loadedDefs.add(def);
    }

    protected abstract void validateDefinition(T def);

    protected void addWarning(String warning) {
        validationWarnings.add(warning);
        AromaLib.LOGGER.warn(warning);
    }

    public T parseFromJson(String json) {
        try {
            return GSON.fromJson(json, DEF);
        } catch (Exception e) {
            AromaLib.LOGGER.error("Failed to parse definition from JSON", e);
            return null;
        }
    }

    public T getById(String id) {
        for (T def : loadedDefs) {
            if (def.getDefID().equals(id)) {
                return def;
            }
        }
        return null;
    }

    public boolean hasId(String id) {
        return loadedIds.contains(id);
    }

    public String toJson(T definition) {
        return GSON.toJson(definition);
    }

    public List<T> reload() {
        AromaLib.LOGGER.info("Reloading definitions for {}...", DIRECTORY);
        return loadAll();
    }

    public boolean hasValidationWarnings() {
        return !validationWarnings.isEmpty();
    }
}
