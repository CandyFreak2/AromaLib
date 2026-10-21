package com.ovrtechnology.aromalib.definition;

import com.google.gson.annotations.SerializedName;
import com.ovrtechnology.aromalib.tracker.item.ItemUnlock;
import com.ovrtechnology.aromalib.util.Texts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class TrackerDefinition {
    @SerializedName("id")
    private String id;

    @SerializedName("texture")
    private String texture;

    /**
     * Model type for the tracker (similar to armor models)
     * Can be: "basic", "advanced", "elite", or custom model identifiers
     * Defaults to iron_helmet model if not specified
     */
    @SerializedName("model")
    private String model;

    /**
     * Unlock conditions for this tracker
     */
    @SerializedName("unlock")
    private ItemUnlock unlock;

    /**
     * Durability of the tracker item (number of uses before breaking)
     */
    @SerializedName("durability")
    private int durability;

    /**
     * Item ID used for repairing this tracker (e.g., "minecraft:iron_ingot")
     */
    @SerializedName("repair")
    private String repair;

    /**
     * Tier level of this tracker. Higher tiers unlock more capabilities.
     * Each tier unlocks the next tier level.
     */
    @SerializedName("tier")
    private int tier;

    /**
     * Durability cost per tracking search (defaults to 10)
     */
    @SerializedName("track_cost")
    private int trackCost;

    @SerializedName("slot")
    private String slot;

    @SerializedName("offset")
    private float yOffset;

    public String getModel() {
        return model != null && !model.isEmpty() ? model : "minecraft:iron_helmet";
    }

    public int getDurability() {
        return durability > 0 ? durability : 100;
    }

    public int getTrackCost() {
        return trackCost > 0 ? trackCost : 10;
    }

    public ItemUnlock getUnlock() {
        return unlock != null ? unlock : new ItemUnlock();
    }

    public String getDisplayName() {
        return Texts.tr(getTranslationKey()).toString();
    }

    public Component resolveName() {
        String key = getTranslationKey();
        if (key != null && !key.isEmpty()) {
            return Component.translatable(key);
        }
        return Component.translatable(getTranslationKey());
    }

    public String getTranslationKey() {
        if (id == null || !id.contains(":")) {
            return "tracker.aromalib.unknown";
        }
        String[] parts = id.split(":", 2);
        return "tracker." + parts[0] + "." + parts[1];
    }

    public boolean isValid() {
        return id != null && !id.isEmpty();
    }
}
