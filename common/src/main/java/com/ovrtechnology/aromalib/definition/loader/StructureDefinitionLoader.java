package com.ovrtechnology.aromalib.definition.loader;

import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.definition.StructureDefinition;
import com.ovrtechnology.aromalib.registry.BlockRegistry;
import com.ovrtechnology.aromalib.registry.ScentRegistry;

import java.util.List;

public class StructureDefinitionLoader extends AbstractDefinitionLoader<StructureDefinition> {
    public static final StructureDefinitionLoader INSTANCE = new StructureDefinitionLoader();

    private StructureDefinitionLoader() {
        super("structures", StructureDefinition.class, "structures");
    }

    private void validateBlocks(String id, List<String> blockIds) {
        if (!BlockRegistry.isInitialized()) {
            AromaLib.LOGGER.debug("[{}] BlockRegistry not initialized, skipping block validation", id);
            return;
        }

        List<String> invalidBlocks = BlockRegistry.validateBlockIds(blockIds);
        for (String invalidBlock : invalidBlocks) {
            AromaLib.LOGGER.debug("[{}] Block '{}' not found in BlockRegistry (may still be valid Minecraft block)", id, invalidBlock);
        }

        for (String blockId : blockIds) {
            if (!StructureDefinition.isValidIdentifier(blockId)) {
                addWarning("[" + id + "] Block ID '" + blockId + "' has invalid format - should be 'namespace:path'");
            }
        }
    }

    @Override
    protected void validate(StructureDefinition def) {
        String id = def.getId();

        if (!def.hasValidIDFormat()) {
            addWarning("[" + id + "] Invalid structure_id format - should be 'namespace:path' (e.g., 'minecraft:stronghold')");
        }

        String rawColor = def.getRawColorHtml();
        if (rawColor == null || rawColor.isEmpty()) {
            addWarning("[" + id + "] No color_html defined, using default: " + StructureDefinition.getColor(def));
        } else if (!StructureDefinition.isValidHtmlColor(rawColor)) {
            addWarning("[" + id + "] Invalid color_html format '" + rawColor + "', using default: " + StructureDefinition.getColor(def));
        }

        if (def.hasScentId()) {
            String scentId = def.getScentId();
            if (ScentRegistry.isInitialized() && !ScentRegistry.hasScent(scentId)) {
                addWarning("[" + id + "] Referenced scent_id '" + scentId + "' does not exist in ScentRegistry");
            }
        } else {
            addWarning("[" + id + "] No scent_id defined, structure will have no associated scent");
        }

        if (def.hasBlocks()) {
            validateBlocks(id, def.getBlocks());
        }

        String rawImage = def.getRawImage();
        if (rawImage == null || rawImage.isEmpty()) {
            AromaLib.LOGGER.debug("[{}] No image defined, using default: {}", id, StructureDefinition.getDefaultImage(def));
        }
    }
}

