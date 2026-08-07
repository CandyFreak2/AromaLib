package com.ovrtechnology.aromalib.flower;

import com.ovrtechnology.aromalib.block.BlockDefinition;
import com.ovrtechnology.aromalib.trigger.ScentPriority;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Represents a trackable flower definition loaded from JSON.
 *
 * <p>Each flower definition maps a Minecraft flower block to a scent and provides
 * display properties and trigger configuration.</p>
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class FlowerDefinition extends BlockDefinition {
    public FlowerDefinition() {
    }

    @Override
    protected String getDefaultColor() {
        return "#FF69B4";
    }

    @Override
    protected int getDefaultRange() {
        return 3;
    }

    @Override
    protected String getFallbackTitle() {
        return "Flower";
    }

    @Override
    protected ScentPriority getDefaultPriority() {
        return ScentPriority.MEDIUM;
    }
}
