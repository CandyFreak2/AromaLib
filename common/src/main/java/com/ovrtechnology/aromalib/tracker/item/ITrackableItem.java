package com.ovrtechnology.aromalib.tracker.item;

import com.ovrtechnology.aromalib.tracker.TrackingAbilityResolver;
import com.ovrtechnology.aromalib.definition.TrackerDefinition;
import net.minecraft.world.entity.EquipmentSlot;

public interface ITrackableItem {
    int getTier();
    int getEnchantValue();
    boolean canDetectBlock(String id);
    boolean canDetectBiome(String id);
    boolean canDetectStructure(String id);
    boolean canDetectFlower(String id);
    boolean hasAbility(String id);
    float getOffset();
    TrackerDefinition getDefinition();
    String getItemID();
    String getRepairItemID();
    ItemUnlock getUnlock();
    TrackingAbilityResolver.ResolvedAbilities getResolvedAbilities();
    EquipmentSlot getSlot();
}
