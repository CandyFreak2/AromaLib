package com.ovrtechnology.aromalib.compat;

import net.blay09.mods.balm.Balm;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// @TODO ABSTRACT MORE
public final class Accessory {

    public interface Provider {
        ItemStack getEquipped(Player player);
        ItemStack equip(Player player, ItemStack stack);
        boolean hasSlot(Player player);

        EquipmentSlot getEquipmentSlot();
    }

    private Accessory() {}

    public static ItemStack getEquipped(Player player) {
        if (player == null) return ItemStack.EMPTY;
        return player.getItemBySlot(getEquipmentSlot());
    }

    public static ItemStack equip(Player player, ItemStack stack) {
        if (player == null) return stack;
        ItemStack previous = player.getItemBySlot(EquipmentSlot.HEAD);
        player.setItemSlot(EquipmentSlot.HEAD, stack == null ? ItemStack.EMPTY : stack);
        return previous;
    }

    public static boolean hasSlot(Player player) {
        return player != null;
    }

    public static EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }
}