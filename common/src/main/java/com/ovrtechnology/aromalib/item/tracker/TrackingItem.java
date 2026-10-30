package com.ovrtechnology.aromalib.item.tracker;

import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.definition.TrackerDefinition;
import com.ovrtechnology.aromalib.util.ItemUnlock;
import com.ovrtechnology.aromalib.ability.TrackingAbilityResolver;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

public class TrackingItem extends Item implements ITrackableItem {
    private final TrackerDefinition definition;
    private final String id;
    private final EquipmentSlot slot;

    public TrackingItem(TrackerDefinition definition, String id, EquipmentSlot slot) {
        super(createProperties(definition, id, slot));
        this.definition = definition;
        this.id = id;
        this.slot = slot;
    }

    /**
     * Create item properties from a definition
     */
    private static Properties createProperties(TrackerDefinition def, String id, EquipmentSlot slot) {
        Properties properties = new Properties();

        properties.setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AromaLib.MOD_ID, id)));

        // Set durability
        properties.durability(def.getDurability());

        // Set stack size to 1
        properties.stacksTo(1);

        // Set rarity based on tier
        properties.rarity(getRarityForTier(def.getTier()));

        ResourceKey<EquipmentAsset> equipmentAsset = getEquipmentAssetFromModel(def.getModel());
        Equippable equippable = Equippable.builder(slot)
                .setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER)
                .setAsset(equipmentAsset)
                .setSwappable(true)
                .setDamageOnHurt(true)
            .build();
        properties.component(DataComponents.EQUIPPABLE, equippable);

        // Add Repairable component for anvil repair
        String repairId = def.getRepair();
        if (repairId != null && !repairId.isEmpty()) {
            Identifier repairLoc = Identifier.parse(repairId);
            BuiltInRegistries.ITEM.getOptional(repairLoc).ifPresent(repairItem -> {
                Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(repairItem);
                properties.component(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(holder)));
            });
        }

        return properties;
    }

    private static ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> getEquipmentAssetFromModel(String model) {
        if (model == null || model.isEmpty()) return EquipmentAssets.IRON;
        String lower = model.toLowerCase();
        if (lower.contains("diamond")) return EquipmentAssets.DIAMOND;
        if (lower.contains("gold")) return EquipmentAssets.GOLD;
        if (lower.contains("netherite")) return EquipmentAssets.NETHERITE;
        if (lower.contains("leather")) return EquipmentAssets.LEATHER;
        if (lower.contains("chain")) return EquipmentAssets.CHAINMAIL;
        return EquipmentAssets.IRON;
    }

    private static Rarity getRarityForTier(int tier) {
        return switch (tier) {
            case 1 -> Rarity.COMMON;
            case 2 -> Rarity.UNCOMMON;
            case 3 -> Rarity.RARE;
            default -> Rarity.EPIC;
        };
    }

    @Override
    public TrackerDefinition getDefinition() {
        return definition;
    }

    @Override
    public String getItemID() {
        return id;
    }

    @Override
    public String getRepairItemID() {
        return definition.getRepair();
    }

    @Override
    public int getTier() {
        return definition.getTier();
    }

    @Override
    public int getEnchantValue() {
        return 5 + (definition.getTier() * 5);
    }

    @Override
    public boolean canDetectBlock(String id) {
        return false;
    }

    @Override
    public boolean canDetectBiome(String id) {
        return false;
    }

    @Override
    public boolean canDetectStructure(String id) {
        return false;
    }

    @Override
    public boolean canDetectFlower(String id) {
        return false;
    }

    @Override
    public boolean hasAbility(String id) {
        return false;
    }

    @Override
    public ItemUnlock getUnlock() {
        return definition.getUnlock();
    }

    @Override
    public TrackingAbilityResolver.ResolvedAbilities getResolvedAbilities() {
        return null;
    }

    @Override
    public EquipmentSlot getSlot() {
        return slot;
    }

    @Override
    public float getOffset() {
        return definition.getYOffset();
    }
}
