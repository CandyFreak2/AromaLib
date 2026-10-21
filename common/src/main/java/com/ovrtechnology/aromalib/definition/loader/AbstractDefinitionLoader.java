package com.ovrtechnology.aromalib.definition.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.data.ClasspathDataSource;
import com.ovrtechnology.aromalib.data.DataSource;
import com.ovrtechnology.aromalib.definition.Definition;
import com.ovrtechnology.aromalib.definition.StructureDefinition;
import lombok.Getter;
import net.minecraft.resources.Identifier;

import java.util.*;

public abstract class AbstractDefinitionLoader<T extends Definition> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    @Getter
    protected final List<T> loadedDefinitions = new ArrayList<>();
    @Getter
    protected final List<String> validationWarnings = new ArrayList<>();
    protected final Set<String> loadedIds = new HashSet<>();
    @Getter
    private final String directory;
    private final Class<T> definition;
    private final String name;

    protected AbstractDefinitionLoader(String directory, Class<T> definition, String name) {
        this.directory = directory;
        this.definition = definition;
        this.name = name;
    }

    public List<T> loadAll(DataSource source) {
        loadedDefinitions.clear();
        loadedIds.clear();
        validationWarnings.clear();

        Map<Identifier, JsonElement> files = source.listJson(directory);
        for (Map.Entry<Identifier, JsonElement> entry : files.entrySet()) {
            try {
                T def = GSON.fromJson(entry.getValue(), definition);
                process(def);
            } catch (Exception e) {
                AromaLib.LOGGER.error("Failed to parse definition {} in directory {}", entry.getKey(), directory, e);
            }
        }

        AromaLib.LOGGER.info("Loaded {} {} definitions from {} file(s)", loadedDefinitions.size(), name, files.size());

        if (!validationWarnings.isEmpty()) {
            AromaLib.LOGGER.warn("{} loading completed with {} validation warnings", name, validationWarnings.size());
        }

        return Collections.unmodifiableList(loadedDefinitions);
    }

    public List<T> loadAll() {
        return loadAll(ClasspathDataSource.INSTANCE);
    }

    private void process(T def) {
        if (def == null) {
            addWarning("Null {category} definition found, skipping...");
            return;
        }

        if (!def.isValid()) {
            addWarning("Invalid category definition found (missing id), skipping...");
            return;
        }

        String id = def.getId();

        if (loadedIds.contains(id)) {
            addWarning("Duplicate id '" + id + "' found, skipping...");
            return;
        }

        validate(def);

        loadedIds.add(id);
        loadedDefinitions.add(def);
    }

    public List<T> getVanilla() {
        List<T> result = new ArrayList<>();
        for (T def : getLoadedDefinitions()) {
            if (def.isVanilla()) {
                result.add(def);
            }
        }
        return result;
    }

    public List<T> getModded() {
        List<T> result = new ArrayList<>();
        for (T def : getLoadedDefinitions()) {
            if (!def.isVanilla()) {
                result.add(def);
            }
        }
        return result;
    }

    protected boolean isValid(Definition def) {
        return def.isValid();
    }

    protected String getID(Definition def) {
        return def.getId();
    }

    protected abstract void validate(T def);

    protected void addWarning(String warning) {
        validationWarnings.add(warning);
        AromaLib.LOGGER.warn(warning);
    }

    public T getDefinitionByID(String id) {
        for (T def : loadedDefinitions) {
            if (getID(def).equals(id)) return def;
        }
        return null;
    }

    public List<T> reload() {
        AromaLib.LOGGER.info("Reloading {} definitions...", name);
        return loadAll();
    }

    public boolean hasId(String id) {
        return loadedIds.contains(id);
    }

    public boolean hasValidationWarnings() {
        return !validationWarnings.isEmpty();
    }
}
