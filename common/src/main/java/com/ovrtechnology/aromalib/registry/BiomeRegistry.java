package com.ovrtechnology.aromalib.registry;

import com.ovrtechnology.aromalib.definition.BiomeDefinition;
import com.ovrtechnology.aromalib.definition.loader.AbstractDefinitionLoader;
import com.ovrtechnology.aromalib.definition.loader.BiomeDefinitionLoader;

/**
 * Central registry for all trackable biome definitions in Aroma Lib.
 * 
 * <p>This class provides the main API for other systems to access biome information.
 * Biomes define which Minecraft biomes can be tracked, along with
 * their associated scents and display colors.</p>
 * 
 * <h2>Initialization Order</h2>
 * <p>BiomeRegistry must be initialized <strong>after</strong> ScentRegistry,
 * as it validates scent references during loading.</p>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * // Get a biome by ID
 * Optional&lt;BiomeDefinition&gt; biome = BiomeRegistry.getBiome("minecraft:jungle");
 * 
 * // Check if a biome is trackable
 * boolean canTrack = BiomeRegistry.hasBiome("minecraft:desert");
 * 
 * // Get the scent for a biome
 * Optional&lt;ScentDefinition&gt; scent = BiomeRegistry.getScentForBiome("minecraft:snowy_plains");
 * 
 * // Get all vanilla biomes
 * List&lt;BiomeDefinition&gt; vanilla = BiomeRegistry.getVanillaBiomes();
 * </pre>
 * 
 * <h2>Modded Biome Support</h2>
 * <p>The registry supports biomes from any mod. Biome IDs use the standard
 * Minecraft Identifier format (namespace:path), allowing modded biomes
 * to be registered alongside vanilla ones.</p>
 */
public final class BiomeRegistry extends AbstractRegistry<BiomeDefinition> {
    public static final BiomeRegistry INSTANCE = new BiomeRegistry();
    public static final BiomeDefinitionLoader LOADER = new BiomeDefinitionLoader();

    public BiomeRegistry() {
        super("Biome");
    }

    @Override
    protected BiomeRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    protected AbstractDefinitionLoader<BiomeDefinition> getLoader() {
        return LOADER;
    }
}

