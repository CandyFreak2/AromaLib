package com.ovrtechnology.aromalib.definition.loader;

import com.ovrtechnology.aromalib.definition.BlockDefinition;
import com.ovrtechnology.aromalib.registry.ScentRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads trackable block definitions from JSON files.
 *
 * <p>This loader handles parsing the block configuration from
 * {@code data/aromalib/blocks/blocks.json} and provides validation
 * for duplicate IDs, HTML color format, and scent references.</p>
 *
 * <p>The JSON format supports both array format and object with "blocks" array:</p>
 * <pre>
 * // Array format:
 * [
 *   { "block_id": "minecraft:diamond_ore", "color_html": "#5DECF5", "scent_id": "terra_silva" }
 * ]
 *
 * // Object format:
 * {
 *   "blocks": [
 *     { "block_id": "minecraft:diamond_ore", "color_html": "#5DECF5", "scent_id": "terra_silva" }
 *   ]
 * }
 * </pre>
 *
 * <h2>Validation</h2>
 * <ul>
 *   <li>Duplicate block_id detection (first occurrence kept)</li>
 *   <li>HTML color format validation (#RGB, #RRGGBB, #RRGGBBAA)</li>
 *   <li>Scent ID validation against ScentRegistry</li>
 * </ul>
 */
public class BlockDefinitionLoader extends AbstractDefinitionLoader<BlockDefinition> {

    public static final BlockDefinitionLoader INSTANCE = new BlockDefinitionLoader();

    public BlockDefinitionLoader() {
        super("blocks", BlockDefinition.class, "blocks");
    }

    public List<BlockDefinition> getBlocksByScentId(String scentId) {
        List<BlockDefinition> result = new ArrayList<>();
        for (BlockDefinition block : getLoadedDefinitions()) {
            if (scentId.equals(block.getScentId())) {
                result.add(block);
            }
        }
        return result;
    }

    @Override
    protected void validate(BlockDefinition def) {
        String blockId = def.getId();

        // Validate HTML color format
        String rawColor = def.getRawColorHtml();
        if (rawColor == null || rawColor.isEmpty()) {
            addWarning("[" + blockId + "] No color_html defined, using default: " + BlockDefinition.getColor(def));
        } else if (!BlockDefinition.isValidHtmlColor(rawColor)) {
            addWarning("[" + blockId + "] Invalid color_html format '" + rawColor + "', using default: " + BlockDefinition.getColor(def));
        }

        // Validate scent_id reference
        if (def.hasScentId()) {
            String scentId = def.getScentId();
            if (ScentRegistry.INSTANCE.isInitialized() && !ScentRegistry.INSTANCE.exists(scentId)) {
                addWarning("[" + blockId + "] Referenced scent_id '" + scentId + "' does not exist in ScentRegistry");
            }
        } else {
            addWarning("[" + blockId + "] No scent_id defined, block will have no associated scent");
        }

        // Validate block_id format (should be namespace:path)
        if (!blockId.contains(":")) {
            addWarning("[" + blockId + "] Block ID should include namespace (e.g., 'minecraft:stone')");
        }
    }
}

