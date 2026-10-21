package com.ovrtechnology.aromalib.definition.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.data.ClasspathDataSource;
import com.ovrtechnology.aromalib.data.DataSource;
import com.ovrtechnology.aromalib.definition.ScentDefinition;
import lombok.Getter;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads scent definitions from JSON files.
 * 
 * <p>This loader handles parsing the scent configuration from
 * {@code data/aromaaffect/scents/scents.json} and provides validation
 * for duplicate IDs and invalid entries.</p>
 * 
 * <p>The JSON format supports both array format and object with "scents" array:</p>
 * <pre>
 * // Array format:
 * [
 *   { "id": "winter", "fallback_name": "Winter" },
 *   { "id": "beach", "fallback_name": "Beach" }
 * ]
 * 
 * // Object format:
 * {
 *   "scents": [
 *     { "id": "winter", "fallback_name": "Winter" },
 *     { "id": "beach", "fallback_name": "Beach" }
 *   ]
 * }
 * </pre>
 */
public class ScentDefinitionLoader extends AbstractDefinitionLoader<ScentDefinition> {
    public static final ScentDefinitionLoader INSTANCE = new ScentDefinitionLoader();

    private ScentDefinitionLoader() {
        super("scents", ScentDefinition.class, "scents");
    }

    @Override
    protected void validate(ScentDefinition def) {
        String id = def.getId();

        // Check fallback name
        if (def.getFallbackName() == null || def.getFallbackName().isEmpty()) {
            AromaLib.LOGGER.warn("[{}] No fallback_name defined, will use auto-generated name", id);
        }
    }
}

