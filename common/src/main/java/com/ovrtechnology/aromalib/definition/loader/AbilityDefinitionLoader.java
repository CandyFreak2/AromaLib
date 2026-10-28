package com.ovrtechnology.aromalib.definition.loader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.ability.AbilityRegistry;
import com.ovrtechnology.aromalib.data.DataSource;
import com.ovrtechnology.aromalib.data.DataSourceInfo;
import com.ovrtechnology.aromalib.definition.AbilityDefinition;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads ability definitions from JSON files.
 *
 * <p>
 * Parses the data/aromalib/abilities/abilities.json file to load
 * all ability definitions. This provides a central registry of all
 * abilities available in the mod, separate from item definitions.
 * </p>
 *
 * <h2>File Structure:</h2>
 * <pre>{@code
 * {
 *   "abilities": [
 *     {
 *       "id": "precise_sniffer",
 *       "type": "block_interaction",
 *       "description": "...",
 *       "config": { ... }
 *     }
 *   ]
 * }
 * }</pre>
 *
 * @see AbilityDefinition
 * @see AbilityRegistry
 */
public class AbilityDefinitionLoader extends AbstractDefinitionLoader<AbilityDefinition> {

    public static final AbilityDefinitionLoader INSTANCE = new AbilityDefinitionLoader();

    @Getter
    private static boolean initialized = false;

    public AbilityDefinitionLoader() {
        super(DataSourceInfo.ABILITIES_PATH + "abilities.json", AbilityDefinition.class, "abilities");
    }

    private static AbilityDefinition[] loadFromResource(DataSource source, String path) {
        try {
            JsonElement jsonElement = source.read(path);
            if (jsonElement == null) {
                AromaLib.LOGGER.warn("Ability definitions file not found: {}", path);
                return new AbilityDefinition[0];
            }

            if (jsonElement.isJsonArray()) {
                return GSON.fromJson(jsonElement, AbilityDefinition[].class);
            } else if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has("abilities")) {
                    return GSON.fromJson(jsonObject.get("abilities"), AbilityDefinition[].class);
                }
            }

            AromaLib.LOGGER.warn("Invalid JSON format in: {}", path);
            return new AbilityDefinition[0];
        } catch (Exception e) {
            AromaLib.LOGGER.error("Error parsing ability definitions from: {}", path, e);
            return new AbilityDefinition[0];
        }
    }

    public List<AbilityDefinition> loadAll(DataSource dataSource) {
        loadedDefinitions.clear();
        loadedIds.clear();
        validationWarnings.clear();

        try {
            AbilityDefinition[] abilities = loadFromResource(dataSource, getDirectory());
            if (abilities != null) {
                for (AbilityDefinition ability : abilities) {
                    if (ability != null && ability.isValid()) {
                        process(ability);
                        AromaLib.LOGGER.info("Loaded ability definition: {} (type: {})", ability.getId(), ability.getType());
                    } else {
                        AromaLib.LOGGER.warn("Invalid ability definition found, skipping...");
                    }
                }
            }
        } catch (Exception e) {
            AromaLib.LOGGER.error("Failed to load ability definitions", e);
        }

        if (!validationWarnings.isEmpty()) {
            AromaLib.LOGGER.warn("{} loading completed with {} validation warnings", getName(), validationWarnings.size());
        }
        initialized = true;
        return Collections.unmodifiableList(loadedDefinitions);
    }

    public List<AbilityDefinition> getAbilitiesByType(String type) {
        List<AbilityDefinition> result = new ArrayList<>();
        for (AbilityDefinition ability : loadedDefinitions) {
            if (type.equals(ability.getType())) {
                result.add(ability);
            }
        }
        return result;
    }


    @Override
    protected void validate(AbilityDefinition def) {
        String id = def.getId();

        if (!loadedIds.contains(id)) {
            addWarning("[" + id + "] Not defined, cannot validate ability");
        }

        if (!def.isTypeValid()) {
            addWarning("[" + def.getType() + "] Unrecognized type");
        }
    }
}
