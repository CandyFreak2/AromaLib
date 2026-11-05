package com.ovrtechnology.aromalib.registry;

import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.definition.ScentDefinition;
import com.ovrtechnology.aromalib.definition.StructureDefinition;
import com.ovrtechnology.aromalib.definition.loader.AbstractDefinitionLoader;
import com.ovrtechnology.aromalib.definition.loader.ScentDefinitionLoader;
import com.ovrtechnology.aromalib.definition.loader.StructureDefinitionLoader;
import com.ovrtechnology.aromalib.registry.variants.AbstractTriggerDefinitionRegistry;
import lombok.Getter;

import java.util.*;

/**
 * Central registry for all trackable structure definitions in Aroma Lib.
 * 
 * <p>This class provides the main API for other systems to access structure information.
 * Structures define which Minecraft structures can be tracked by the Nose system, along with
 * their associated scents, display colors, and characteristic blocks.</p>
 * 
 * <h2>Initialization Order</h2>
 * <p>StructureRegistry must be initialized <strong>after</strong> ScentRegistry and BlockRegistry,
 * as it validates references during loading.</p>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * // Get a structure by ID
 * Optional&lt;StructureDefinition&gt; structure = StructureRegistry.getStructure("minecraft:stronghold");
 * 
 * // Check if a structure is trackable
 * boolean canTrack = StructureRegistry.hasStructure("minecraft:village_plains");
 * 
 * // Get the scent for a structure
 * Optional&lt;ScentDefinition&gt; scent = StructureRegistry.getScentForStructure("minecraft:ancient_city");
 * 
 * // Get all vanilla structures
 * List&lt;StructureDefinition&gt; vanilla = StructureRegistry.getVanillaStructures();
 * </pre>
 * 
 * <h2>Modded Structure Support</h2>
 * <p>The registry supports structures from any mod. Structure IDs use the standard
 * Minecraft Identifier format (namespace:path), allowing modded structures
 * to be registered alongside vanilla ones.</p>
 */
public final class StructureRegistry extends AbstractTriggerDefinitionRegistry<StructureDefinition> {
    public static final StructureRegistry INSTANCE = new StructureRegistry();
    public static final StructureDefinitionLoader LOADER = new StructureDefinitionLoader();

    public StructureRegistry() {
        super("structure");
    }

    @Override
    protected AbstractRegistry<StructureDefinition> getInstance() {
        return INSTANCE;
    }

    @Override
    protected AbstractDefinitionLoader<StructureDefinition> getLoader() {
        return LOADER;
    }
}

