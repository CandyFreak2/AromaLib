package com.ovrtechnology.aromalib.definition;

import com.google.gson.annotations.SerializedName;
import com.ovrtechnology.aromalib.tracker.item.ItemUnlock;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class TrackerDefinition {
    /**
     * Unique identifier for this tracker (e.g., "foragers_tracker", "iron_tracker")
     */
    @SerializedName("id")
    private String id;

    /**
     * Path to the texture file relative to assets/aromaaffect/textures/item/
     */
    @SerializedName("image")
    private String image;

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

    // Override getters with defaults

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

    /**
     * Validates the tracker definition has required fields
     */
    public boolean isValid() {
        return id != null && !id.isEmpty();
    }
}
