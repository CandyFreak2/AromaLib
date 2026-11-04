package com.ovrtechnology.aromalib.definition.variant;

import com.google.gson.JsonObject;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class BlockVariant extends Variant {
    private static final Map<Block, Block> STEMS = Map.of(
            Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT,
            Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT,
            Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT,
            Blocks.KELP, Blocks.KELP_PLANT);


    public BlockVariant(JsonObject conditions, String scentID, String perception) {
        super(conditions, scentID, perception);
    }

    public static boolean matches(TagKey<Block> tag, BlockState state) {
        return state.is(tag);
    }

    public static boolean matches(BlockState state, Block target) {
        if (state.is(target)) return true;
        Block stem = STEMS.get(target);
        return stem != null && state.is(stem);
    }
}
