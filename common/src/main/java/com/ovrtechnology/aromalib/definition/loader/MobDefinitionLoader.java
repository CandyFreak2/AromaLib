package com.ovrtechnology.aromalib.definition.loader;

import com.ovrtechnology.aromalib.definition.MobDefinition;
import com.ovrtechnology.aromalib.registry.ScentRegistry;

/**
 * Loads mob definitions from JSON files.
 */
public class MobDefinitionLoader extends AbstractDefinitionLoader<MobDefinition> {
    public static final MobDefinitionLoader INSTANCE = new MobDefinitionLoader();

    private MobDefinitionLoader() {
        super("mobs", MobDefinition.class, "mobs");
    }

    @Override
    protected void validate(MobDefinition def) {
        String type = def.getId();

        if (def.hasScentId()) {
            String scentId = def.getScentId();
            if (ScentRegistry.isInitialized() && !ScentRegistry.hasScent(scentId)) {
                addWarning("[" + type + "] Referenced scent_id '" + scentId + "' does not exist in ScentRegistry");
            }
        } else {
            addWarning("[" + type + "] No scent_id defined, mob will have no associated scent");
        }
    }
}
