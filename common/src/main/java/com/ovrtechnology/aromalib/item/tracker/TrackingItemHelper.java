package com.ovrtechnology.aromalib.item.tracker;

import com.ovrtechnology.aromalib.definition.TrackerDefinition;
import com.ovrtechnology.aromalib.ability.TrackingAbilityResolver;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class TrackingItemHelper {
    private TrackingItemHelper() {
    }

    public static Optional<ITrackableItem> getEquippedItem(Player player) {
        if (player == null) {
            return Optional.empty();
        }

        ItemStack stack = Accessory.getEquipped(player);
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        if (stack.getItem() instanceof ITrackableItem item) {
            return Optional.of(item);
        }

        return Optional.empty();
    }

    public static Optional<TrackerDefinition> getEquippedDefinition(Player player) {
        return getEquippedItem(player).map(ITrackableItem::getDefinition);
    }

    public static TrackingAbilityResolver.ResolvedAbilities getEquippedAbilities(Player player) {
        if (player == null) {
            return TrackingAbilityResolver.ResolvedAbilities.EMPTY;
        }
        ItemStack stack = Accessory.getEquipped(player);
        if (stack.isEmpty()) {
            return TrackingAbilityResolver.ResolvedAbilities.EMPTY;
        }
        if (stack.getItem() instanceof ITrackableItem item) {
            return item.getResolvedAbilities();
        }
        if (stack.getItem() instanceof CustomITrackableItem item) {
            return item.getVariantId(stack).map(id -> TrackingAbilityResolver.getResolvedAbilities(id.toString())).orElse(TrackingAbilityResolver.ResolvedAbilities.EMPTY);
        }
        return TrackingAbilityResolver.ResolvedAbilities.EMPTY;
    }

    public static boolean hasEquipped(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack stack = Accessory.getEquipped(player);
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() instanceof ITrackableItem) {
            return true;
        }

        if (stack.getItem() instanceof CustomITrackableItem item) {
            return item.getVariantId(stack).isPresent();
        }

        return false;
    }


    public static boolean canDetectBlock(Player player, String id) {
        return getEquippedAbilities(player).canDetectBlock(id);
    }


    public static boolean canDetectBiome(Player player, String id) {
        return getEquippedAbilities(player).canDetectBiome(id);
    }


    public static boolean canDetectStructure(Player player, String id) {
        return getEquippedAbilities(player).canDetectStructure(id);
    }


    public static boolean canDetectFlower(Player player, String id) {
        return getEquippedAbilities(player).canDetectFlower(id);
    }


    public static int getEquippedTier(Player player) {
        return getEquippedItem(player).map(ITrackableItem::getTier).orElse(0);
    }


    public static Optional<String> getEquippedID(Player player) {
        Optional<String> byDefinition = getEquippedDefinition(player).map(TrackerDefinition::getId);
        if (byDefinition.isPresent() || player == null) {
            return byDefinition;
        }
        ItemStack stack = Accessory.getEquipped(player);
        if (stack.getItem() instanceof CustomITrackableItem item) {
            return item.getVariantId(stack).map(Identifier::toString);
        }
        return Optional.empty();
    }

    public static boolean canTrackRespawn(Player player) {
        return getEquippedTier(player) >= 2;
    }
}
