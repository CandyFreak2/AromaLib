package com.ovrtechnology.aromalib.registry;

import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.definition.AbstractDefinition;
import com.ovrtechnology.aromalib.definition.AbstractTriggerDefinition;
import com.ovrtechnology.aromalib.definition.ScentDefinition;
import com.ovrtechnology.aromalib.definition.ScentedDefinition;
import com.ovrtechnology.aromalib.definition.loader.AbstractDefinitionLoader;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public abstract class AbstractRegistry<T extends AbstractDefinition> {
    @Getter
    protected final Map<String, T> definitions = new LinkedHashMap<>();
    @Getter
    private final String name;
    @Getter
    @Setter
    protected boolean initialized = false;

    public AbstractRegistry(String name) {
        this.name = name;
    }

    public void init() {
        if (initialized) {
            AromaLib.LOGGER.warn("{} Registry has been initialized more than once", name);
            return;
        }

        if (!ScentRegistry.INSTANCE.isInitialized()) {
            AromaLib.LOGGER.warn("{} Registry has been called before ScentRegistry! Scent validation may fail.", name);
        }

        AromaLib.LOGGER.info("Initializing {} Registry...", name);

        List<T> defs = getLoader().loadAll();

        for (T def : defs) {
            register(def);
        }

        initialized = true;
        AromaLib.LOGGER.info("{} Registry initialized with {} structures", name, defs.size());
    }

    protected void register(T def) {
        String id = def.getId();

        if (definitions.containsKey(id)) {
            AromaLib.LOGGER.warn("Duplicate {} ID in registry: {}, skipping...", name, id);
            return;
        }

        definitions.put(id, def);
        AromaLib.LOGGER.debug("Registered {}}: {}", name, id);
    }

    protected abstract AbstractRegistry<T> getInstance();

    protected abstract AbstractDefinitionLoader<T> getLoader();

    public Optional<T> getDefinition(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public T getDefinitionOrThrow(String id) {
        T def = definitions.get(id);
        if (def == null) {
            throw new IllegalArgumentException("Unknown " + name + " ID: " + id);
        }
        return def;
    }

    public <T extends AbstractTriggerDefinition> String getDisplayName(String id) {
        T def = (T) getDefinitionOrThrow(id);
        if (def == null) {
            return "Unknown Scent";
        }
        // TODO: Integrate with Minecraft's localization system
        // For now, return the fallback name
        return def.getFallbackName();
    }

    public boolean exists(String id) {
        return definitions.containsKey(id);
    }

    public Iterable<String> getIds() {
        return Collections.unmodifiableSet(definitions.keySet());
    }

    public Iterable<T> getAllAsIterable() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public List<T> getAllAsList() {
        return new ArrayList<>(definitions.values());
    }

    public int getCount() {
        return definitions.size();
    }

    public List<T> getVanilla() {
        List<T> result = new ArrayList<>();
        for (T def : definitions.values()) {
            if (def.isVanilla()) {
                result.add(def);
            }
        }
        return result;
    }

    public List<T> getModded() {
        List<T> result = new ArrayList<>();
        for (T def : definitions.values()) {
            if (!def.isVanilla()) {
                result.add(def);
            }
        }
        return result;
    }

    public List<T> getDefinitionByNamespace(String namespace) {
        List<T> result = new ArrayList<>();
        for (T def : definitions.values()) {
            if (namespace.equals(def.getNamespace())) {
                result.add(def);
            }
        }
        return result;
    }

    public List<String> validateIds(List<String> ids) {
        List<String> invalid = new ArrayList<>();
        for (String id : ids) {
            if (!exists(id)) {
                invalid.add(id);
            }
        }
        return invalid;
    }

    public Set<String> getAllNamespaces() {
        Set<String> namespaces = new HashSet<>();
        for (T structure : definitions.values()) {
            String namespace = structure.getNamespace();
            if (!namespace.isEmpty()) {
                namespaces.add(namespace);
            }
        }
        return namespaces;
    }

    public void reload() {
        AromaLib.LOGGER.info("Reloading {} Registry...", name);
        definitions.clear();

        List<T> definitions = getLoader().reload();
        for (T definition : definitions) {
            register(definition);
        }

        AromaLib.LOGGER.info("{} Registry reloaded with {} {}", name, definitions.size(), name + "s");
    }


    public void clear() {
        definitions.clear();
        initialized = false;
    }
}
