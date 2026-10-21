package com.ovrtechnology.aromalib.definition.loader;

import com.ovrtechnology.aromalib.definition.FlowerDefinition;
import com.ovrtechnology.aromalib.registry.ScentRegistry;

/**
 * Loads flower definitions from JSON files.
 */
public class FlowerDefinitionLoader extends AbstractDefinitionLoader<FlowerDefinition> {
    public static final FlowerDefinitionLoader INSTANCE = new FlowerDefinitionLoader();

    private FlowerDefinitionLoader() {
        super("flowers", FlowerDefinition.class, "flowers");
    }

    @Override
    protected void validate(FlowerDefinition def) {
        String id = def.getId();

        if (def.hasScentId()) {
            String scentId = def.getScentId();
            if (ScentRegistry.isInitialized() && !ScentRegistry.hasScent(scentId)) {
                addWarning("[" + id + "] Referenced scent_id '" + scentId + "' does not exist in ScentRegistry");
            }
        } else {
            addWarning("[" + id + "] No scent_id defined, flower will have no associated scent");
        }
    }
}
