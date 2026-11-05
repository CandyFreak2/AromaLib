package com.ovrtechnology.aromalib.definition.loader;

import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.definition.BiomeDefinition;
import com.ovrtechnology.aromalib.registry.ScentRegistry;

/**
 * Loads trackable biome definitions from JSON files.
 *
 * <p>This loader handles parsing the biome configuration from
 * {@code data/aromaaffect/biomes/biomes.json} and provides validation
 * for duplicate IDs, HTML color format, and scent references.</p>
 *
 * <p>The JSON format supports both array format and object with "biomes" array:</p>
 * <pre>
 * // Array format:
 * [
 *   { "biome_id": "minecraft:jungle", "color_html": "#537B09", "scent_id": "evergreen" }
 * ]
 *
 * // Object format:
 * {
 *   "biomes": [
 *     { "biome_id": "minecraft:jungle", "color_html": "#537B09", "scent_id": "evergreen" }
 *   ]
 * }
 * </pre>
 *
 * <h2>Validation</h2>
 * <ul>
 *   <li>Duplicate biome_id detection (first occurrence kept)</li>
 *   <li>Biome ID format validation (namespace:path)</li>
 *   <li>HTML color format validation (#RGB, #RRGGBB, #RRGGBBAA)</li>
 *   <li>Scent ID validation against ScentRegistry</li>
 * </ul>
 */
public class BiomeDefinitionLoader extends AbstractDefinitionLoader<BiomeDefinition> {

    public static final BiomeDefinitionLoader INSTANCE = new BiomeDefinitionLoader();

    public BiomeDefinitionLoader() {
        super("biomes", BiomeDefinition.class, "biome");
    }

    @Override
    protected void validate(BiomeDefinition def) {
        String id = def.getId();

        // Validate biome_id format (namespace:path)
        if (!def.hasValidIDFormat()) {
            addWarning("[" + id + "] Invalid biome_id format - should be 'namespace:path' (e.g., 'minecraft:jungle')");
        }

        // Validate HTML color format
        String rawColor = def.getRawColorHtml();
        if (rawColor == null || rawColor.isEmpty()) {
            addWarning("[" + id + "] No color_html defined, using default: " + BiomeDefinition.getColor(def));
        } else if (!BiomeDefinition.isValidHtmlColor(rawColor)) {
            addWarning("[" + id + "] Invalid color_html format '" + rawColor + "', using default: " + BiomeDefinition.getColor(def));
        }

        // Validate scent_id reference
        if (def.hasScentId()) {
            String scentId = def.getScentId();
            if (ScentRegistry.INSTANCE.isInitialized() && !ScentRegistry.INSTANCE.exists(scentId)) {
                addWarning("[" + id + "] Referenced scent_id '" + scentId + "' does not exist in ScentRegistry");
            }
        } else {
            addWarning("[" + id + "] No scent_id defined, biome will have no associated scent");
        }

        // Validate image path
        String rawImage = def.getRawImage();
        if (rawImage == null || rawImage.isEmpty()) {
            AromaLib.LOGGER.debug("[{}] No image defined, using default: {}", id, BiomeDefinition.getDefaultImage(def));
        }
    }
}

