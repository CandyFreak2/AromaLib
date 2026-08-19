package com.ovrtechnology.aromalib.tracker.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.Optional;

public interface CustomITrackableItem extends ITrackableItem {
    Properties getBaseProperties(Identifier id);
    ItemStack stackFor(Item item, Identifier id);
    Component resolveName(Identifier id);
    Optional<Identifier> getVariantId(ItemStack stack);
}
