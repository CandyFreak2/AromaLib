package com.ovrtechnology.aromalib.tags;

import com.ovrtechnology.aromalib.util.Ids;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TagInit {
    public static class Blocks {
        public static final TagKey<Block> STEMS = createTag("stems");


        private static TagKey<Block> createTag(String name) {
            return TagKey.create(Registries.BLOCK, Ids.mod(name));
        }
    }

    public static class Items {
        private static TagKey<Item> createTag(String name) {
            return TagKey.create(Registries.ITEM, Ids.mod(name));
        }
    }
}

/*
 private static final Map<Block, Block> STEMS = Map.of(
Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT,
Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT,
Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT,
Blocks.KELP, Blocks.KELP_PLANT);
 */