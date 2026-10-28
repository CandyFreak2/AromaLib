package com.ovrtechnology.aromalib.util;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public enum AbilityType {
    @SerializedName("passive")
    PASSIVE("passive"),

    @SerializedName("passive")
    BLOCK_INTERACTION("block_interaction")

    ;

    private final String text;

    AbilityType(String text) {
        this.text = text;
    }

    public boolean equals(String type) {
        return text.equals(type);
    }
}
