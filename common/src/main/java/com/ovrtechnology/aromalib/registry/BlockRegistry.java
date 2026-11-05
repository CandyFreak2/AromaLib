package com.ovrtechnology.aromalib.registry;

import com.ovrtechnology.aromalib.definition.BlockDefinition;
import com.ovrtechnology.aromalib.definition.loader.AbstractDefinitionLoader;
import com.ovrtechnology.aromalib.definition.loader.BlockDefinitionLoader;

/**
 * Central registry for all trackable block definitions in Aroma Lib.
 * 
 * <p>This class provides the main API for other systems to access block information.
 * Blocks define which Minecraft blocks can be tracked by the Nose system, along with
 * their associated scents and display colors.</p>
 * 
 * <h2>Initialization Order</h2>
 * <p>BlockRegistry must be initialized <strong>after</strong> ScentRegistry,
 * as it validates scent references during loading.</p>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * // Get a block by ID
 * Optional&lt;BlockDefinition&gt; block = BlockRegistry.getBlock("minecraft:diamond_ore");
 * 
 * // Check if a block is trackable
 * boolean canTrack = BlockRegistry.hasBlock("minecraft:gold_ore");
 * 
 * // Get the scent for a block
 * Optional&lt;ScentDefinition&gt; scent = BlockRegistry.getScentForBlock("minecraft:lava");
 * 
 * // Get all blocks using a specific scent
 * List&lt;BlockDefinition&gt; fireBlocks = BlockRegistry.getBlocksByScent("smoky");
 * </pre>
 */
public final class BlockRegistry extends AbstractRegistry<BlockDefinition> {
    public static final BlockRegistry INSTANCE = new BlockRegistry();
    public static final BlockDefinitionLoader LOADER = new BlockDefinitionLoader();

    public BlockRegistry() {
        super("block");
    }

    public int getColor(String id) {
        BlockDefinition block = getDefinitionOrThrow(id);
        return block != null ? block.getColorAsInt() : 0xFFFFFF;
    }

    public String getBlockColorHtml(String blockId) {
        BlockDefinition block = getDefinitionOrThrow(blockId);
        return block != null ? block.getColorHtml() : BlockDefinition.getColor(block);
    }

    @Override
    protected AbstractRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    protected AbstractDefinitionLoader<BlockDefinition> getLoader() {
        return LOADER;
    }
}

