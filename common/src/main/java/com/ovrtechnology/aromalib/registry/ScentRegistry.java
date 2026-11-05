package com.ovrtechnology.aromalib.registry;

import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.data.DataSource;
import com.ovrtechnology.aromalib.definition.AbstractTriggerDefinition;
import com.ovrtechnology.aromalib.definition.ScentDefinition;
import com.ovrtechnology.aromalib.definition.loader.AbstractDefinitionLoader;
import com.ovrtechnology.aromalib.definition.loader.ScentDefinitionLoader;
import com.ovrtechnology.aromalib.registry.variants.AbstractTriggerDefinitionRegistry;

import java.util.List;
import java.util.Optional;

/**
 * Central registry for all scent definitions in Aroma Lib.
 * 
 * <p>This class provides the main API for other systems to access scent information.
 * It handles:</p>
 * <ul>
 *   <li>Loading scent definitions from JSON</li>
 *   <li>Providing access to scent definitions by ID</li>
 *   <li>Filtering scents by priority</li>
 *   <li>Validation of scent references</li>
 * </ul>
 * 
 * <p>Usage example:</p>
 * <pre>
 * // Initialize during mod startup
 * ScentRegistry.init();
 * 
 * // Get a scent by ID
 * Optional&lt;ScentDefinition&gt; scent = ScentRegistry.getScent("winter");
 * 
 * // Check if a scent exists
 * boolean exists = ScentRegistry.INSTANCE.exists("beach");
 * 
 * // Get all scent IDs
 * Iterable&lt;String&gt; allIds = ScentRegistry.getAllScentIds();
 * </pre>
 */
public final class ScentRegistry extends AbstractTriggerDefinitionRegistry<ScentDefinition> {
    public static final ScentRegistry INSTANCE = new ScentRegistry();
    public static final ScentDefinitionLoader LOADER = new ScentDefinitionLoader();

    public ScentRegistry() {
        super("scent");
    }

    public void reload(DataSource dataSource) {
        AromaLib.LOGGER.info("Reloading ScentRegistry...");
        definitions.clear();

        List<ScentDefinition> definitions = ScentDefinitionLoader.INSTANCE.loadAll(dataSource);
        for (ScentDefinition definition : definitions) {
            register(definition);
        }

        AromaLib.LOGGER.info("ScentRegistry reloaded with {} scents", definitions.size());
    }

    @Override
    protected AbstractRegistry<ScentDefinition> getInstance() {
        return INSTANCE;
    }

    @Override
    protected AbstractDefinitionLoader<ScentDefinition> getLoader() {
        return LOADER;
    }

    public Optional<ScentDefinition> getScentByName(String name) {
        if (name == null) return Optional.empty();
        for (ScentDefinition def : definitions.values()) {
            if (name.equalsIgnoreCase(def.getFallbackName())) {
                return Optional.of(def);
            }
        }
        return Optional.empty();
    }
}

