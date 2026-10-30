package com.ovrtechnology.aromalib.client.config.util;

import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum PuffMode implements StringRepresentable {
    AUTOMATIC("AUTOMATIC"),
    MANUAL("MANUAL");

    private final String serializedName;

    PuffMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NonNull String getSerializedName() {
        return serializedName;
    }
}
