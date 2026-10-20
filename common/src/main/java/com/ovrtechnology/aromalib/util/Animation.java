package com.ovrtechnology.aromalib.util;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Animation {
    @SerializedName("frames")
    private int frames;

    @SerializedName("ticks_per_frame")
    private int ticksPerFrame;

    public int getTicksPerFrame() {
        return ticksPerFrame > 0 ? ticksPerFrame : 4;
    }

    public boolean isAnimated() {
        return frames > 1;
    }
}
