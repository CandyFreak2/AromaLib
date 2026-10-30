package com.ovrtechnology.aromalib.util;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class ItemUnlock {
    @SerializedName("blocks")
    private List<String> blocks;

    @SerializedName("biomes")
    private List<String> biomes;

    @SerializedName("structures")
    private List<String> structures;

    @SerializedName("flowers")
    private List<String> flowers;

    @SerializedName("abilities")
    private List<String> abilities;

    @SerializedName("trackers")
    private List<String> trackers;

    public List<String> getBlocks() {
        return blocks != null ? blocks : Collections.emptyList();
    }

    public List<String> getBiomes() {
        return biomes != null ? biomes : Collections.emptyList();
    }

    public List<String> getStructures() {
        return structures != null ? structures : Collections.emptyList();
    }

    public List<String> getFlowers() {
        return flowers != null ? flowers : Collections.emptyList();
    }

    public List<String> getAbilities() {
        return abilities != null ? abilities : Collections.emptyList();
    }

    public List<String> getTrackers() {
        return trackers != null ? trackers : Collections.emptyList();
    }

    public boolean hasBlockUnlocks() {
        return blocks != null && !blocks.isEmpty();
    }

    public boolean hasBiomeUnlocked() {
        return biomes != null && !biomes.isEmpty();
    }

    public boolean hasStructureUnlocked() {
        return structures != null && !structures.isEmpty();
    }

    public boolean hasFlowerUnlocked() {
        return flowers != null && !flowers.isEmpty();
    }

    public boolean hasAbilityUnlocked() {
        return abilities != null && !abilities.isEmpty();
    }

    public boolean isChild() {
        return trackers != null && !trackers.isEmpty();
    }

    public boolean hasAnyUnlocks() {
        return hasBlockUnlocks()
                || hasBiomeUnlocked()
                || hasStructureUnlocked()
                || hasFlowerUnlocked()
                || hasAbilityUnlocked()
                || isChild();
    }
}
