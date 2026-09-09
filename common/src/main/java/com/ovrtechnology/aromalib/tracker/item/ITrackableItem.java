package com.ovrtechnology.aromalib.tracker.item;

import com.ovrtechnology.aromalib.tracker.TrackerAbilityResolver;
import com.ovrtechnology.aromalib.definition.TrackerDefinition;
import net.minecraft.world.entity.EquipmentSlot;

public interface ITrackableItem {
    TrackerDefinition getDefinition();

    String getItemID();

    String getRepairItemID();

    int getTier();

    int getEnchantValue();

    boolean canDetectBlock(String id);

    boolean canDetectBiome(String id);

    boolean canDetectStructure(String id);

    boolean canDetectFlower(String id);

    boolean hasAbility(String id);

    ItemUnlock getUnlock();

    TrackerAbilityResolver.ResolvedAbilities getResolvedAbilities();

    EquipmentSlot getSlot();

    float getOffset();
}
